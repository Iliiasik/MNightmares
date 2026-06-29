package iliiasik.mnightmares.command;

import com.mojang.brigadier.CommandDispatcher;
import iliiasik.mnightmares.config.NightmaresConfig;
import iliiasik.mnightmares.network.payload.SyncConfigPayload;
import iliiasik.mnightmares.network.payload.UserContentPayload;
import iliiasik.mnightmares.server.UserContentInitializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

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
        SyncConfigPayload configPayload = NightmaresConfig.getInstance().toSyncPayload();
        UserContentPayload contentPayload = UserContentInitializer.buildPayload();

        MinecraftServer server = source.getServer();
        int count = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PacketDistributor.sendToPlayer(player, configPayload);
            PacketDistributor.sendToPlayer(player, contentPayload);
            count++;
        }

        final int synced = count;
        source.sendSuccess(() -> Component.literal("Midnight Nightmares reloaded. Synced to " + synced + " player(s)."), true);
        return synced;
    }
}