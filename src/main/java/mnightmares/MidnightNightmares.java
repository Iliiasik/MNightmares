package mnightmares;

import mnightmares.command.NightmaresCommand;
import mnightmares.network.NetworkHandler;
import mnightmares.config.NightmaresConfig;
import mnightmares.server.UserContentInitializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MidnightNightmares.MOD_ID)
public class MidnightNightmares {
    public static final String MOD_ID = "mnightmares";
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightNightmares");

    private final IEventBus modEventBus;

    public MidnightNightmares() {
        this.modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Midnight Nightmares initialized");
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        NightmaresConfig.getInstance();
        NetworkHandler.registerPackets();
        UserContentInitializer.writeDefaultFiles();
        LOGGER.info("Midnight Nightmares common setup complete");
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        mnightmares.client.MidnightNightmaresClient.init(modEventBus);
        LOGGER.info("Midnight Nightmares client setup complete");
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        NetworkHandler.sendUserContent(serverPlayer, UserContentInitializer.buildPacket());
        NetworkHandler.sendConfig(serverPlayer, NightmaresConfig.getInstance().toSyncPacket());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        NightmaresCommand.register(event.getDispatcher());
    }
}
