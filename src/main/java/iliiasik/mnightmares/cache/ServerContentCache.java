package iliiasik.mnightmares.cache;

import java.util.List;
import java.util.Map;

public class ServerContentCache {
    private static Map<String, List<String>> content = null;
    public static void apply(Map<String, List<String>> c) { content = c; }
    public static boolean has() { return content != null; }
    public static Map<String, List<String>> get() { return content; }
    public static void clear() { content = null; }
}