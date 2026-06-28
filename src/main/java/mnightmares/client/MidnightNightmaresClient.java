package mnightmares.client;

import mnightmares.cache.ServerConfigCache;
import mnightmares.config.NightmaresConfig;
import mnightmares.client.manager.SleepStateManager;
import mnightmares.client.render.SleepOverlayRenderer;
import mnightmares.client.repository.SlideRepository;
import mnightmares.client.service.SlideService;
import mnightmares.client.service.UserContentLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MidnightNightmaresClient {
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightNightmares");

    private static MidnightNightmaresClient instance;

    private SlideRepository slideRepository;
    private SleepStateManager sleepStateManager;
    private SleepOverlayRenderer overlayRenderer;
    private UserContentLoader userContentLoader;

    public static void init(IEventBus modEventBus) {
        if (instance == null) {
            instance = new MidnightNightmaresClient();
            MinecraftForge.EVENT_BUS.register(new ForgeClientEvents());
            modEventBus.addListener(MidnightNightmaresClient::onRegisterReloadListeners);
            LOGGER.info("[MidnightNightmaresClient] initialized");
        }
    }

    private MidnightNightmaresClient() {
        initializeComponents();
    }

    private void initializeComponents() {
        NightmaresConfig config = NightmaresConfig.getInstance();
        slideRepository = new SlideRepository();
        userContentLoader = new UserContentLoader();
        Minecraft mc = Minecraft.getInstance();
        slideRepository.loadAll(mc.getResourceManager());
        SlideService slideService = new SlideService(slideRepository, config, userContentLoader);
        sleepStateManager = new SleepStateManager();
        overlayRenderer = new SleepOverlayRenderer(sleepStateManager, slideService, config);
    }

    private static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<ResourceManager>() {
            @Override
            protected @NotNull ResourceManager prepare(@NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
                return manager;
            }

            @Override
            protected void apply(@NotNull ResourceManager manager, @NotNull ResourceManager unused, @NotNull ProfilerFiller profiler) {
                MidnightNightmaresClient inst = getInstance();
                if (inst != null) {
                    inst.slideRepository.clear();
                    inst.slideRepository.loadAll(manager);
                }
            }
        });
    }

    public static MidnightNightmaresClient getInstance() {
        return instance;
    }

    public SleepOverlayRenderer getOverlayRenderer() {
        return overlayRenderer;
    }

    public UserContentLoader getUserContentLoader() {
        return userContentLoader;
    }

    private static class ForgeClientEvents {
        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft client = Minecraft.getInstance();
            MidnightNightmaresClient inst = getInstance();
            if (inst == null) return;
            inst.sleepStateManager.tick(client.player);
            inst.overlayRenderer.tick();
        }

        @SubscribeEvent
        public void onClientDisconnect(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
            if (!(event.getEntity() instanceof net.minecraft.client.player.LocalPlayer)) return;
            ServerConfigCache.clear();
            MidnightNightmaresClient inst = getInstance();
            if (inst != null && inst.userContentLoader != null) {
                inst.userContentLoader.clearServerContent();
            }
        }
    }
}