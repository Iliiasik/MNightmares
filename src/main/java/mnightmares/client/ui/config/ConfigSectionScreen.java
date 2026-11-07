package mnightmares.client.ui.config;

import mnightmares.config.NightmaresConfig;
import mnightmares.network.NetworkHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class ConfigSectionScreen extends OptionsSubScreen {

    private static final String OPTION_PREFIX = "option.mnightmares.";
    private static final String TOOLTIP_SUFFIX = ".tooltip";
    private static final String GENERIC_VALUE_KEY = "options.generic_value";

    private static final String PERCENT_KEY = OPTION_PREFIX + "percent";
    private static final String SECONDS_KEY = OPTION_PREFIX + "seconds";
    private static final String MILLIS_KEY = OPTION_PREFIX + "milliseconds";

    private static final int MILLIS_PER_STEP = 100;
    private static final float STEPS_PER_SECOND = 10f;

    private static final int LIST_TOP = 32;
    private static final int LIST_BOTTOM_MARGIN = 32;
    private static final int LIST_ITEM_HEIGHT = 25;
    private static final int DONE_WIDTH = 200;
    private static final int DONE_HEIGHT = 20;
    private static final int DONE_BOTTOM = 27;

    private final ConfigSection section;
    private OptionsList list;

    public ConfigSectionScreen(Screen parent, ConfigSection section) {
        super(parent, Minecraft.getInstance().options, section.title().withStyle(ChatFormatting.BOLD));
        this.section = section;
    }

    @Override
    protected void init() {
        this.list = new OptionsList(Minecraft.getInstance(), this.width, this.height,
                LIST_TOP, this.height - LIST_BOTTOM_MARGIN, LIST_ITEM_HEIGHT);
        NightmaresConfig config = NightmaresConfig.getInstance();
        switch (section) {
            case SLEEP_OVERLAY -> addSleepOverlay(list, config.getSleepOverlay());
            case SERVER -> addServer(list, config.getServer());
        }
        addWidget(this.list);
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(this.width / 2 - DONE_WIDTH / 2, this.height - DONE_BOTTOM, DONE_WIDTH, DONE_HEIGHT)
                .build());
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        this.list.render(graphics, mouseX, mouseY, partialTick);
        int titleY = (LIST_TOP - this.font.lineHeight) / 2;
        graphics.drawCenteredString(this.font, this.title, this.width / 2, titleY, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private static void addSleepOverlay(OptionsList options, NightmaresConfig.SleepOverlaySettings o) {
        options.addSmall(
                flag("enable_image", o.enableImage, value -> o.enableImage = value),
                flag("hide_chat_when_sleeping", o.hideChatWhenSleeping, value -> o.hideChatWhenSleeping = value));
        options.addBig(
                flag("user_content_replaces", o.userContentReplaces, value -> o.userContentReplaces = value));
        options.addSmall(
                seconds("min_slide_time", o.minSlideDisplayTimeMs, value -> o.minSlideDisplayTimeMs = value),
                seconds("max_slide_time", o.maxSlideDisplayTimeMs, value -> o.maxSlideDisplayTimeMs = value));
        options.addSmall(
                millis("fade_in", 0, 3000, o.fadeInDurationMs, value -> o.fadeInDurationMs = value),
                millis("fade_out", 0, 3000, o.fadeOutDurationMs, value -> o.fadeOutDurationMs = value));
        options.addSmall(
                percent("overlay_opacity", 0, 100, o.overlayOpacity, value -> o.overlayOpacity = value),
                percent("text_opacity", 0, 100, o.textOpacity, value -> o.textOpacity = value));
        options.addSmall(
                percent("image_opacity", 0, 100, o.imageOpacity, value -> o.imageOpacity = value),
                millis("eye_animation_speed", 20, 500, o.eyeAnimationSpeedMs,
                        value -> o.eyeAnimationSpeedMs = value));
        options.addSmall(
                percent("text_scale", scalePercent(NightmaresConfig.MIN_SCALE), scalePercent(NightmaresConfig.MAX_SCALE),
                        o.textScale, value -> o.textScale = value),
                percent("image_scale", scalePercent(NightmaresConfig.MIN_SCALE), scalePercent(NightmaresConfig.MAX_SCALE),
                        o.imageScale, value -> o.imageScale = value));
    }

    private static void addServer(OptionsList options, NightmaresConfig.ServerSettings server) {
        options.addBig(flag("reset_phantom_timer", server.resetPhantomTimerForNonSleepers,
                value -> server.resetPhantomTimerForNonSleepers = value));
    }

    private static int scalePercent(float scale) {
        return Math.round(scale * 100f);
    }

    private static <T> OptionInstance.TooltipSupplier<T> tooltip(String key) {
        return OptionInstance.cachedConstantTooltip(Component.translatable(key + TOOLTIP_SUFFIX));
    }

    private static Component valueLabel(Component caption, Component value) {
        return Component.translatable(GENERIC_VALUE_KEY, caption, value);
    }

    private static OptionInstance<Boolean> flag(String name, boolean current, Consumer<Boolean> setter) {
        String key = OPTION_PREFIX + name;
        return OptionInstance.createBoolean(key, tooltip(key), current, setter);
    }

    private static OptionInstance<Integer> millis(String name, int min, int max, int current, IntConsumer setter) {
        String key = OPTION_PREFIX + name;
        return new OptionInstance<>(
                key,
                tooltip(key),
                (caption, value) -> valueLabel(caption, Component.translatable(MILLIS_KEY, value)),
                new OptionInstance.IntRange(min, max),
                Mth.clamp(current, min, max),
                setter::accept);
    }

    private static OptionInstance<Integer> seconds(String name, int currentMs, IntConsumer setter) {
        String key = OPTION_PREFIX + name;
        int min = 5;
        int max = 600;
        return new OptionInstance<>(
                key,
                tooltip(key),
                (caption, value) -> valueLabel(caption, Component.translatable(SECONDS_KEY,
                        String.format(Locale.ROOT, "%.1f", value / STEPS_PER_SECOND))),
                new OptionInstance.IntRange(min, max),
                Mth.clamp(Math.round(currentMs / (float) MILLIS_PER_STEP), min, max),
                value -> setter.accept(value * MILLIS_PER_STEP));
    }

    private static OptionInstance<Integer> percent(String name, int min, int max,
                                                   float current, FloatSetter setter) {
        String key = OPTION_PREFIX + name;
        return new OptionInstance<>(
                key,
                tooltip(key),
                (caption, value) -> valueLabel(caption, Component.translatable(PERCENT_KEY, value)),
                new OptionInstance.IntRange(min, max),
                Mth.clamp(Math.round(current * 100f), min, max),
                value -> setter.set(value / 100f));
    }

    @Override
    public void removed() {
        super.removed();
        NightmaresConfig.getInstance().save();
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null) {
            server.execute(() -> NetworkHandler.sendConfigToAll(server));
        }
    }

    @FunctionalInterface
    private interface FloatSetter {
        void set(float value);
    }
}
