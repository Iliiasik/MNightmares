package iliiasik.mnightmares.client;

import iliiasik.mnightmares.cache.ServerConfigCache;
import iliiasik.mnightmares.cache.ServerContentCache;
import iliiasik.mnightmares.config.NightmaresConfig;
import iliiasik.mnightmares.client.manager.SleepStateManager;
import iliiasik.mnightmares.client.render.SleepOverlayRenderer;
import iliiasik.mnightmares.client.repository.SlideRepository;
import iliiasik.mnightmares.client.service.SlideService;
import iliiasik.mnightmares.client.service.UserContentLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
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
            NeoForge.EVENT_BUS.register(new ClientEvents());
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

    public static MidnightNightmaresClient getInstance() { return instance; }
    public SleepOverlayRenderer getOverlayRenderer() { return overlayRenderer; }

    private static class ClientEvents {
        @SubscribeEvent
        public void onClientTick(ClientTickEvent.Post event) {
            Minecraft client = Minecraft.getInstance();
            MidnightNightmaresClient inst = getInstance();
            if (inst == null) return;
            inst.sleepStateManager.tick(client.player);
            inst.overlayRenderer.tick();
        }

        @SubscribeEvent
        public void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ServerConfigCache.clear();
            ServerContentCache.clear();
        }
    }
}