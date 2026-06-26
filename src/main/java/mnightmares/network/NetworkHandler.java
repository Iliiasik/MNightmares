package mnightmares.network;

import mnightmares.MidnightNightmares;
import mnightmares.cache.ServerConfigCache;
import mnightmares.client.MidnightNightmaresClient;
import mnightmares.network.packet.SyncConfigPacket;
import mnightmares.network.packet.UserContentPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MidnightNightmares.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void registerPackets() {
        CHANNEL.registerMessage(packetId++, SyncConfigPacket.class,
                SyncConfigPacket::encode, SyncConfigPacket::decode,
                NetworkHandler::handleSyncConfig,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(packetId++, UserContentPacket.class,
                UserContentPacket::encode, UserContentPacket::decode,
                NetworkHandler::handleUserContent,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendConfig(ServerPlayer player, SyncConfigPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendUserContent(ServerPlayer player, UserContentPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    private static void handleSyncConfig(SyncConfigPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> ServerConfigCache.apply(packet));
        context.setPacketHandled(true);
    }

    private static void handleUserContent(UserContentPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> applyClientUserContent(packet)));
        context.setPacketHandled(true);
    }

    private static void applyClientUserContent(UserContentPacket packet) {
        MidnightNightmaresClient inst = MidnightNightmaresClient.getInstance();
        if (inst != null && inst.getUserContentLoader() != null) {
            inst.getUserContentLoader().applyServerContent(packet.content());
        }
    }
}
