package iliiasik.mnightmares.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import iliiasik.mnightmares.cache.ServerConfigCache;
import iliiasik.mnightmares.network.payload.SyncConfigPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class NightmaresConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightNightmares");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static NightmaresConfig instance;

    private SleepOverlaySettings sleepOverlay = new SleepOverlaySettings();
    private ServerSettings server = new ServerSettings();

    private NightmaresConfig() {}

    public static NightmaresConfig getInstance() {
        if (instance == null) instance = load();
        return instance;
    }

    public static NightmaresConfig load() {
        Path configPath = getConfigPath();
        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                NightmaresConfig config = GSON.fromJson(json, NightmaresConfig.class);
                if (config != null) {
                    config.validate();
                    return config;
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load configuration: {}", e.getMessage());
            }
        }
        NightmaresConfig config = new NightmaresConfig();
        config.save();
        return config;
    }

    public void save() {
        Path configPath = getConfigPath();
        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(this));
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration: {}", e.getMessage());
        }
    }

    private void validate() {
        if (sleepOverlay == null) sleepOverlay = new SleepOverlaySettings();
        if (server == null) server = new ServerSettings();
    }

    public static Path getConfigDir() {
        return Paths.get(System.getProperty("user.dir"), "config", "mnightmares");
    }

    private static Path getConfigPath() {
        return getConfigDir().resolve("mnightmares.json");
    }

    public ServerSettings getServer() { return server; }

    public SyncConfigPayload toSyncPayload() {
        return new SyncConfigPayload(
                sleepOverlay.minSlideDisplayTimeMs,
                sleepOverlay.maxSlideDisplayTimeMs,
                sleepOverlay.fadeInDurationMs,
                sleepOverlay.fadeOutDurationMs,
                sleepOverlay.overlayOpacity,
                sleepOverlay.textOpacity,
                sleepOverlay.imageOpacity,
                sleepOverlay.enableImage,
                sleepOverlay.userContentReplaces,
                sleepOverlay.hideChatWhenSleeping,
                sleepOverlay.eyeAnimationSpeedMs
        );
    }

    public int getFadeInDurationMs() {
        if (ServerConfigCache.has()) return ServerConfigCache.get().fadeInDurationMs();
        return sleepOverlay.fadeInDurationMs;
    }

    public int getFadeOutDurationMs() {
        if (ServerConfigCache.has()) return ServerConfigCache.get().fadeOutDurationMs();
        return sleepOverlay.fadeOutDurationMs;
    }

    public float getOverlayOpacity() {
        if (ServerConfigCache.has()) return ServerConfigCache.get().overlayOpacity();
        return sleepOverlay.overlayOpacity;
    }

    public float getTextOpacity() {
        if (ServerConfigCache.has()) return ServerConfigCache.get().textOpacity();
        return sleepOverlay.textOpacity;
    }

    public float getImageOpacity() {
        if (ServerConfigCache.has()) return ServerConfigCache.get().imageOpacity();
        return sleepOverlay.imageOpacity;
    }

    public boolean isEnableImage() {
        if (ServerConfigCache.has()) return ServerConfigCache.get().enableImage();
        return sleepOverlay.enableImage;
    }

    public boolean isUserContentReplaces() {
        if (ServerConfigCache.has()) return ServerConfigCache.get().userContentReplaces();
        return sleepOverlay.userContentReplaces;
    }

    public boolean isHideChatWhenSleeping() {
        if (ServerConfigCache.has()) return ServerConfigCache.get().hideChatWhenSleeping();
        return sleepOverlay.hideChatWhenSleeping;
    }

    public int getEyeAnimationSpeedMs() {
        if (ServerConfigCache.has()) return ServerConfigCache.get().eyeAnimationSpeedMs();
        return sleepOverlay.eyeAnimationSpeedMs;
    }

    public int getRandomSlideDisplayTime() {
        int min = ServerConfigCache.has() ? ServerConfigCache.get().minSlideDisplayTimeMs() : sleepOverlay.minSlideDisplayTimeMs;
        int max = ServerConfigCache.has() ? ServerConfigCache.get().maxSlideDisplayTimeMs() : sleepOverlay.maxSlideDisplayTimeMs;
        if (min >= max) return min;
        return min + (int) (Math.random() * (max - min));
    }

    public static class SleepOverlaySettings {
        public int minSlideDisplayTimeMs = 2500;
        public int maxSlideDisplayTimeMs = 4000;
        public int fadeInDurationMs = 300;
        public int fadeOutDurationMs = 300;
        public float overlayOpacity = 0.4f;
        public float textOpacity = 1.0f;
        public float imageOpacity = 0.6f;
        public boolean enableImage = true;
        public boolean userContentReplaces = false;
        public boolean hideChatWhenSleeping = true;
        public int eyeAnimationSpeedMs = 100;
    }

    public static class ServerSettings {
        public boolean resetPhantomTimerForNonSleepers = true;
    }

    public static void reload() {
        NightmaresConfig fresh = load();
        if (instance == null) {
            instance = fresh;
        } else {
            instance.sleepOverlay = fresh.sleepOverlay;
            instance.server = fresh.server;
        }
    }
}