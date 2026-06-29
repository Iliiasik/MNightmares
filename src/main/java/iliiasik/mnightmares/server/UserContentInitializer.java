package iliiasik.mnightmares.server;

import iliiasik.mnightmares.config.NightmaresConfig;
import iliiasik.mnightmares.network.payload.UserContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserContentInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightNightmares");
    private static final String[] ALL_LANGUAGES = {"en_us"};
    private static final String EMPTY_CONTENT = "{\n  \"entries\": []\n}\n";

    public static void writeDefaultFiles() {
        for (String language : ALL_LANGUAGES) {
            Path dir = getNightmaresDir().resolve(language);
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                LOGGER.warn("Failed to create nightmares directory {}: {}", dir, e.getMessage());
                continue;
            }
            Path file = dir.resolve("nightmares.json");
            if (!Files.exists(file)) {
                try {
                    Files.writeString(file, EMPTY_CONTENT);
                } catch (IOException e) {
                    LOGGER.warn("Failed to write file {}: {}", file, e.getMessage());
                }
            }
        }
    }

    public static UserContentPayload buildPayload() {
        Map<String, List<String>> content = new HashMap<>();
        for (String language : ALL_LANGUAGES) {
            List<String> entries = readEntries(language);
            if (!entries.isEmpty()) content.put(language + ":nightmares", entries);
        }
        return new UserContentPayload(content);
    }

    private static List<String> readEntries(String language) {
        Path file = getNightmaresDir().resolve(language).resolve("nightmares.json");
        if (!Files.exists(file)) return List.of();
        try {
            String json = Files.readString(file);
            com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            if (!obj.has("entries")) return List.of();
            List<String> result = new ArrayList<>();
            for (com.google.gson.JsonElement el : obj.getAsJsonArray("entries")) {
                if (el.isJsonObject()) {
                    com.google.gson.JsonObject entry = el.getAsJsonObject();
                    if (entry.has("text")) {
                        String text = entry.get("text").getAsString();
                        if (!text.isBlank()) result.add(text);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            LOGGER.warn("Failed to read entries {}: {}", language, e.getMessage());
            return List.of();
        }
    }

    private static Path getNightmaresDir() {
        return NightmaresConfig.getConfigDir().resolve("nightmares");
    }
}