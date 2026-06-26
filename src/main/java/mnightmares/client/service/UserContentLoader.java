package mnightmares.client.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import mnightmares.config.NightmaresConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UserContentLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightNightmares");
    private static final Gson GSON = new GsonBuilder().create();

    private Map<String, List<String>> serverContent = null;

    public void applyServerContent(Map<String, List<String>> content) {
        this.serverContent = content;
        LOGGER.info("Received {} user content entries from server", content.size());
    }

    public void clearServerContent() {
        this.serverContent = null;
    }

    public List<String> loadEntries(String language) {
        if (serverContent != null) {
            return serverContent.getOrDefault(language + ":nightmares", Collections.emptyList());
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
