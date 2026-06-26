package mnightmares.client.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mnightmares.MidnightNightmares;
import mnightmares.client.model.Slide;
import mnightmares.client.model.SlideCollection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SlideRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightNightmares");
    private static final Gson GSON = new GsonBuilder().create();

    private final List<Slide> slides = new ArrayList<>();

    public void loadAll(ResourceManager manager) {
        slides.clear();
        slides.addAll(loadLanguage(manager, "en_us"));
        LOGGER.info("Loaded {} nightmare slides", slides.size());
    }

    private List<Slide> loadLanguage(ResourceManager manager, String language) {
        List<Slide> result = new ArrayList<>();
        ResourceLocation resourceId = new ResourceLocation(MidnightNightmares.MOD_ID, "nightmares/" + language + "/nightmares.json");
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

    public List<Slide> getSlides() {
        return slides;
    }

    public void clear() {
        slides.clear();
    }
}
