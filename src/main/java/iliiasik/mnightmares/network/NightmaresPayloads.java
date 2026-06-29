package iliiasik.mnightmares.network;

import iliiasik.mnightmares.cache.ServerConfigCache;
import iliiasik.mnightmares.cache.ServerContentCache;
import iliiasik.mnightmares.network.payload.SyncConfigPayload;
import iliiasik.mnightmares.network.payload.UserContentPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class NightmaresPayloads {
    public static void handleConfig(SyncConfigPayload payload, IPayloadContext context) {
        ServerConfigCache.apply(payload);
    }

    public static void handleContent(UserContentPayload payload, IPayloadContext context) {
        ServerContentCache.apply(payload.content());
    }
}