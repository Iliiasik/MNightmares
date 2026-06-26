package mnightmares.client.service;

import mnightmares.config.NightmaresConfig;
import mnightmares.client.model.Slide;
import mnightmares.client.repository.SlideRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SlideService {
    private final SlideRepository slideRepository;
    private final NightmaresConfig config;
    private final UserContentLoader userContentLoader;

    public SlideService(SlideRepository slideRepository, NightmaresConfig config, UserContentLoader userContentLoader) {
        this.slideRepository = slideRepository;
        this.config = config;
        this.userContentLoader = userContentLoader;
    }

    public Slide getNextSlide() {
        String language = getCurrentLanguage();
        List<String> userEntries = userContentLoader.loadEntries(language);
        List<Slide> pool = new ArrayList<>();

        if (config.isUserContentReplaces() && !userEntries.isEmpty()) {
            for (String text : userEntries) pool.add(Slide.of(text));
        } else {
            pool.addAll(slideRepository.getSlides());
            for (String text : userEntries) pool.add(Slide.of(text));
        }

        if (pool.isEmpty()) return null;
        return pickByRarity(pool);
    }

    private Slide pickByRarity(List<Slide> pool) {
        float totalWeight = 0f;
        for (Slide slide : pool) totalWeight += slide.rarity();
        if (totalWeight <= 0f) return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        float random = ThreadLocalRandom.current().nextFloat() * totalWeight;
        float current = 0f;
        for (Slide slide : pool) {
            current += slide.rarity();
            if (random <= current) return slide;
        }
        return pool.get(pool.size() - 1);
    }

    public String getCurrentLanguage() {
        return "en_us";
    }
}
