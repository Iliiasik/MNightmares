package mnightmares.client.model;

import java.util.List;

public class SlideCollection {
    public List<SlideEntry> entries;

    public List<SlideEntry> entries() {
        return entries;
    }

    public static class SlideEntry {
        public String text;
        public float rarity;

        public String text() {
            return text;
        }

        public float rarity() {
            return rarity;
        }
    }
}