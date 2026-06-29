package iliiasik.mnightmares.cache;

import iliiasik.mnightmares.network.payload.SyncConfigPayload;

public class ServerConfigCache {
    private static SyncConfigPayload cached = null;
    public static void apply(SyncConfigPayload payload) { cached = payload; }
    public static boolean has() { return cached != null; }
    public static SyncConfigPayload get() { return cached; }
    public static void clear() { cached = null; }
}