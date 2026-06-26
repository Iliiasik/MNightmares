package mnightmares.command;

import com.mojang.brigadier.CommandDispatcher;
import mnightmares.config.NightmaresConfig;
import mnightmares.network.NetworkHandler;
import mnightmares.network.packet.SyncConfigPacket;
import mnightmares.network.packet.UserContentPacket;
import mnightmares.server.UserContentInitializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class NightmaresCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("midnightnightmares")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("reload")
                                .executes(context -> reload(context.getSource())))
        );
    }

    private static int reload(CommandSourceStack source) {
        NightmaresConfig.reload();
        NightmaresConfig config = NightmaresConfig.getInstance();
        SyncConfigPacket configPacket = config.toSyncPacket();
        UserContentPacket contentPacket = UserContentInitializer.buildPacket();

        MinecraftServer server = source.getServer();
        int count = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            NetworkHandler.sendConfig(player, configPacket);
            NetworkHandler.sendUserContent(player, contentPacket);
            count++;
        }

        final int synced = count;
        source.sendSuccess(() -> Component.literal("Midnight Nightmares reloaded. Synced to " + synced + " player(s)."), true);
        return synced;
    }
}
