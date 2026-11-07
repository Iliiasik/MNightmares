package mnightmares.client.ui.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public enum ConfigSection {
    SLEEP_OVERLAY("sleep_overlay"),
    SERVER("server");

    private final String key;

    ConfigSection(String key) {
        this.key = key;
    }

    public static List<ConfigSection> shown(boolean editable) {
        return editable ? List.of(values()) : List.of();
    }

    public MutableComponent title() {
        return Component.translatable("screen.mnightmares.config." + key);
    }
}
