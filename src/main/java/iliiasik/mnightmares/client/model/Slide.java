package iliiasik.mnightmares.client.model;

public record Slide(String text, float rarity) {
    public static Slide of(String text) { return new Slide(text, 1.0f); }
    public static Slide ofRare(String text, float rarity) { return new Slide(text, rarity); }
}