package mnightmares.client.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mnightmares.MidnightNightmares;
import mnightmares.client.model.Slide;
import mnightmares.client.model.SlideCollection;
import mnightmares.common.SupportedLanguages;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SlideRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightNightmares");
    private static final Gson GSON = new GsonBuilder().create();

    private final Map<String, List<Slide>> slides = new HashMap<>();

    public void loadAll(ResourceManager manager) {
        slides.clear();
        int total = 0;
        for (String language : SupportedLanguages.codes()) {
            List<Slide> loaded = loadLanguage(manager, language);
            if (loaded.isEmpty()) continue;
            slides.put(language, loaded);
            total += loaded.size();
        }
        LOGGER.info("Loaded {} nightmare slides for {} languages", total, slides.size());
    }

    private List<Slide> loadLanguage(ResourceManager manager, String language) {
        List<Slide> result = new ArrayList<>();
        ResourceLocation resourceId = ResourceLocation.fromNamespaceAndPath(MidnightNightmares.MOD_ID, "nightmares/" + language + "/nightmares.json");
        try {
            Optional<Resource> resourceOpt = manager.getResource(resourceId);
            if (resourceOpt.isEmpty()) return result;
            try (InputStreamReader reader = new InputStreamReader(resourceOpt.get().open(), StandardCharsets.UTF_8)) {
                SlideCollection collection = GSON.fromJson(reader, SlideCollection.class);
                if (collection != null && collection.entries() != null) {
                    for (SlideCollection.SlideEntry entry : collection.entries()) {
                        result.add(Slide.ofRare(entry.text(), entry.rarity()));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load nightmares from {}: {}", resourceId, e.getMessage());
        }
        return result;
    }

    public List<Slide> getSlides(String language) {
        List<Slide> forLanguage = slides.get(language);
        if (forLanguage != null) return forLanguage;
        return slides.getOrDefault(SupportedLanguages.DEFAULT, List.of());
    }

    public void clear() {
        slides.clear();
    }
}
