package iliiasik.mnightmares;

import iliiasik.mnightmares.command.NightmaresCommand;
import iliiasik.mnightmares.config.NightmaresConfig;
import iliiasik.mnightmares.network.NightmaresPayloads;
import iliiasik.mnightmares.network.payload.SyncConfigPayload;
import iliiasik.mnightmares.network.payload.UserContentPayload;
import iliiasik.mnightmares.server.UserContentInitializer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MidnightNightmares.MOD_ID)
public class MidnightNightmares {
    public static final String MOD_ID = "mnightmares";
    public static final Logger LOGGER = LoggerFactory.getLogger("MidnightNightmares");

    public MidnightNightmares(IEventBus modEventBus) {
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.register(this);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            iliiasik.mnightmares.client.MidnightNightmaresClient.init(modEventBus);
        }
        LOGGER.info("Midnight Nightmares initialized");
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        NightmaresConfig.getInstance();
        UserContentInitializer.writeDefaultFiles();
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(SyncConfigPayload.TYPE, SyncConfigPayload.STREAM_CODEC, NightmaresPayloads::handleConfig);
        registrar.playToClient(UserContentPayload.TYPE, UserContentPayload.STREAM_CODEC, NightmaresPayloads::handleContent);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        PacketDistributor.sendToPlayer(serverPlayer, UserContentInitializer.buildPayload());
        PacketDistributor.sendToPlayer(serverPlayer, NightmaresConfig.getInstance().toSyncPayload());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        NightmaresCommand.register(event.getDispatcher());
    }
}