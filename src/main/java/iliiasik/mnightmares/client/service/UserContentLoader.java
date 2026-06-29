package iliiasik.mnightmares.client.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import iliiasik.mnightmares.cache.ServerContentCache;
import iliiasik.mnightmares.config.NightmaresConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class UserContentLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightNightmares");
    private static final Gson GSON = new GsonBuilder().create();

    public List<String> loadEntries(String language) {
        if (ServerContentCache.has()) {
            return ServerContentCache.get().getOrDefault(language + ":nightmares", Collections.emptyList());
        }
        Path file = getNightmaresDir().resolve(language).resolve("nightmares.json");
        if (!Files.exists(file)) return Collections.emptyList();
        try {
            String json = Files.readString(file);
            EntriesFile parsed = GSON.fromJson(json, EntriesFile.class);
            if (parsed != null && parsed.entries != null) {
                return parsed.entries.stream()
                        .filter(e -> e != null && e.text != null && !e.text.isBlank())
                        .map(e -> e.text)
                        .toList();
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load user content {}: {}", language, e.getMessage());
        }
        return Collections.emptyList();
    }

    private Path getNightmaresDir() {
        return NightmaresConfig.getConfigDir().resolve("nightmares");
    }

    private static class EntriesFile {
        @SerializedName("entries")
        List<Entry> entries;
    }

    private static class Entry {
        @SerializedName("text")
        String text;
    }
}