package mnightmares.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mnightmares.MidnightNightmares;
import mnightmares.config.NightmaresConfig;
import mnightmares.client.manager.SleepStateManager;
import mnightmares.client.model.Slide;
import mnightmares.client.service.SlideService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class SleepOverlayRenderer {
    private static final ResourceLocation EYE_TEXTURE = new ResourceLocation(MidnightNightmares.MOD_ID, "textures/gui/eye.png");
    private static final int EYE_FRAME_SIZE = 32;
    private static final int EYE_FRAME_COUNT = 22;
    private static final int EYE_TEXTURE_HEIGHT = EYE_FRAME_SIZE * EYE_FRAME_COUNT;
    private static final float EYE_PULSE_AMOUNT = 0.04f;
    private static final double EYE_PULSE_PERIOD_MS = 700.0;

    private static final float BASE_W = 1920.0f;
    private static final float BASE_H = 1080.0f;
    private static final int VIRTUAL_EYE_SIZE = 300;
    private static final int VIRTUAL_GAP = 48;
    private static final int VIRTUAL_TEXT_WIDTH = 780;
    private static final float VIRTUAL_TEXT_SCALE = 2.6f;
    private static final int VIRTUAL_LINE_SPACING = 6;

    private static final int BLOOD_RGB = 0x5A0000;
    private static final float ALPHA_LERP_SPEED = 0.15f;
    private static final long SLEEP_DEBOUNCE_MS = 200;

    private final SleepStateManager sleepStateManager;
    private final SlideService slideService;
    private final NightmaresConfig config;

    private Slide currentSlide;
    private Slide nextSlide;
    private long slideStartTime;
    private long visibleStartTime;
    private long lastSleepEndTime;
    private int currentSlideDuration;
    private float textAlpha = 0f;
    private float targetTextAlpha = 0f;
    private float overlayAlpha = 0f;
    private SlideState slideState = SlideState.HIDDEN;
    private boolean isOverlayVisible = false;

    private enum SlideState {
        HIDDEN, FADING_IN, VISIBLE, FADING_OUT
    }

    public SleepOverlayRenderer(SleepStateManager sleepStateManager, SlideService slideService, NightmaresConfig config) {
        this.sleepStateManager = sleepStateManager;
        this.slideService = slideService;
        this.config = config;
    }

    public void tick() {
        long now = System.currentTimeMillis();
        if (sleepStateManager.justStoppedSleeping()) lastSleepEndTime = now;
        if (sleepStateManager.justStartedSleeping()) {
            boolean wasRecentlySleeping = (now - lastSleepEndTime) < SLEEP_DEBOUNCE_MS;
            if (!isOverlayVisible && !wasRecentlySleeping) onSleepStart();
        }
        if (!sleepStateManager.isSleeping() && isOverlayVisible) {
            if ((now - lastSleepEndTime) >= SLEEP_DEBOUNCE_MS) onSleepEnd();
        }
        if (sleepStateManager.isSleeping() && isOverlayVisible) {
            updateOverlayAlpha();
            updateSlideState();
        }
    }

    private void onSleepStart() {
        isOverlayVisible = true;
        overlayAlpha = 0f;
        textAlpha = 1f;
        targetTextAlpha = 1f;
        currentSlide = slideService.getNextSlide();
        nextSlide = slideService.getNextSlide();
        currentSlideDuration = config.getRandomSlideDisplayTime();
        slideStartTime = System.currentTimeMillis();
        visibleStartTime = System.currentTimeMillis();
        slideState = SlideState.VISIBLE;
    }

    private void onSleepEnd() {
        currentSlide = null;
        nextSlide = null;
        slideState = SlideState.HIDDEN;
        textAlpha = 0f;
        targetTextAlpha = 0f;
        overlayAlpha = 0f;
        isOverlayVisible = false;
    }

    private void updateOverlayAlpha() {
        if (isOverlayVisible && overlayAlpha < 1f) overlayAlpha = Math.min(1f, overlayAlpha + 0.02f);
    }

    private void updateSlideState() {
        long now = System.currentTimeMillis();
        switch (slideState) {
            case FADING_IN -> {
                long elapsedFadeIn = now - slideStartTime;
                float progressFadeIn = (float) elapsedFadeIn / config.getFadeInDurationMs();
                if (progressFadeIn >= 1f && textAlpha > 0.95f) {
                    targetTextAlpha = 1f;
                    slideState = SlideState.VISIBLE;
                    visibleStartTime = now;
                    nextSlide = slideService.getNextSlide();
                } else {
                    targetTextAlpha = easeInOut(Math.min(progressFadeIn, 1f));
                }
            }
            case VISIBLE -> {
                targetTextAlpha = 1f;
                if (now - visibleStartTime >= currentSlideDuration) {
                    slideState = SlideState.FADING_OUT;
                    slideStartTime = now;
                }
            }
            case FADING_OUT -> {
                float progressFadeOut = (float) (now - slideStartTime) / config.getFadeOutDurationMs();
                targetTextAlpha = 1f - easeInOut(Math.min(progressFadeOut, 1f));
                if (progressFadeOut >= 1f && textAlpha < 0.05f) {
                    currentSlide = nextSlide != null ? nextSlide : slideService.getNextSlide();
                    nextSlide = null;
                    currentSlideDuration = config.getRandomSlideDisplayTime();
                    slideStartTime = now;
                    targetTextAlpha = 0f;
                    slideState = SlideState.FADING_IN;
                }
            }
            case HIDDEN -> targetTextAlpha = 0f;
        }
        textAlpha = lerp(textAlpha, targetTextAlpha);
        if (Math.abs(textAlpha - targetTextAlpha) < 0.01f) textAlpha = targetTextAlpha;
    }

    private float lerp(float current, float target) {
        return current + (target - current) * ALPHA_LERP_SPEED;
    }

    private float easeInOut(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t < 0.5f ? 2 * t * t : 1 - (float) Math.pow(-2 * t + 2, 2) / 2;
    }

    public boolean isActive() {
        return sleepStateManager.isSleeping() && isOverlayVisible && overlayAlpha > 0f;
    }

    public void renderBloodOverlay(PoseStack poseStack, int screenWidth, int screenHeight) {
        if (!isActive()) return;
        float strength = config.getOverlayOpacity() * overlayAlpha;
        float pulse = 0.85f + 0.15f * (float) Math.sin(System.currentTimeMillis() / 700.0);
        strength *= pulse;

        int baseAlpha = clampAlpha(strength * 0.45f);
        GuiComponent.fill(poseStack, 0, 0, screenWidth, screenHeight, (baseAlpha << 24) | BLOOD_RGB);

        int edgeAlpha = clampAlpha(strength);
        int bandH = Math.round(screenHeight * 0.36f);
        int bandW = Math.round(screenWidth * 0.26f);

        fillVerticalGradient(poseStack, 0, screenWidth, bandH, edgeAlpha, 0);
        fillVerticalGradient(poseStack, screenHeight - bandH, screenWidth, screenHeight, 0, edgeAlpha);
        fillHorizontalGradient(poseStack, 0, bandW, screenHeight, edgeAlpha, 0);
        fillHorizontalGradient(poseStack, screenWidth - bandW, screenWidth, screenHeight, 0, edgeAlpha);
    }

    private void fillVerticalGradient(PoseStack poseStack, int y1, int x2, int y2, int alphaTop, int alphaBottom) {
        int step = 3;
        int height = y2 - y1;
        if (height <= 0) return;
        for (int y = y1; y < y2; y += step) {
            float t = (float) (y - y1) / height;
            int a = Math.round(alphaTop + (alphaBottom - alphaTop) * t);
            int ye = Math.min(y + step, y2);
            GuiComponent.fill(poseStack, 0, y, x2, ye, (a << 24) | BLOOD_RGB);
        }
    }

    private void fillHorizontalGradient(PoseStack poseStack, int x1, int x2, int y2, int alphaLeft, int alphaRight) {
        int step = 3;
        int width = x2 - x1;
        if (width <= 0) return;
        for (int x = x1; x < x2; x += step) {
            float t = (float) (x - x1) / width;
            int a = Math.round(alphaLeft + (alphaRight - alphaLeft) * t);
            int xe = Math.min(x + step, x2);
            GuiComponent.fill(poseStack, x, 0, xe, y2, (a << 24) | BLOOD_RGB);
        }
    }

    private int clampAlpha(float a) {
        int v = Math.round(a * 255f);
        if (v < 0) return 0;
        return Math.min(v, 255);
    }

    public void renderContent(PoseStack poseStack, int screenWidth, int screenHeight) {
        if (!isActive()) return;
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        float uiScale = Math.min(screenWidth / BASE_W, screenHeight / BASE_H);

        int eyeSize = Math.max(1, Math.round(VIRTUAL_EYE_SIZE * uiScale));
        int gap = Math.round(VIRTUAL_GAP * uiScale);
        int textAreaWidth = Math.round(VIRTUAL_TEXT_WIDTH * uiScale);
        float textScale = Math.max(1.0f, VIRTUAL_TEXT_SCALE * uiScale);

        List<String> lines = currentSlide != null
                ? wrapText(currentSlide.text(), font, Math.round(textAreaWidth / textScale))
                : new ArrayList<>();

        int lineHeight = font.lineHeight + VIRTUAL_LINE_SPACING;
        int textBlockHeight = Math.round(lines.size() * lineHeight * textScale);

        int totalHeight = eyeSize + gap + textBlockHeight;
        int centerX = screenWidth / 2;
        int topY = (screenHeight - totalHeight) / 2;

        if (config.isEnableImage()) {
            int eyeX = centerX - eyeSize / 2;
            renderEye(poseStack, eyeX, topY, eyeSize);
        }

        if (currentSlide != null && textAlpha > 0.01f && !lines.isEmpty()) {
            int textTop = topY + eyeSize + gap;
            renderText(poseStack, font, lines, centerX, textTop, textScale, lineHeight);
        }
    }

    private void renderEye(PoseStack poseStack, int x, int y, int size) {
        int speed = Math.max(1, config.getEyeAnimationSpeedMs());
        int frame = (int) ((System.currentTimeMillis() / speed) % EYE_FRAME_COUNT);
        float vOffset = frame * EYE_FRAME_SIZE;
        float alpha = overlayAlpha * config.getImageOpacity();

        float pulse = 1.0f + EYE_PULSE_AMOUNT * (float) Math.sin(System.currentTimeMillis() / EYE_PULSE_PERIOD_MS);
        int pulsedSize = Math.max(1, Math.round(size * pulse));
        int offset = (pulsedSize - size) / 2;
        int drawX = x - offset;
        int drawY = y - offset;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EYE_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        GuiComponent.blit(poseStack, drawX, drawY, pulsedSize, pulsedSize, 0.0f, vOffset, EYE_FRAME_SIZE, EYE_FRAME_SIZE, EYE_FRAME_SIZE, EYE_TEXTURE_HEIGHT);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    private void renderText(PoseStack poseStack, Font font, List<String> lines, int centerX, int top, float scale, int lineHeight) {
        int alpha = clampAlpha(config.getTextOpacity() * textAlpha);
        if (alpha <= 0) return;
        int textColor = (alpha << 24) | 0xFFFFFF;

        poseStack.pushPose();
        poseStack.translate(centerX, top, 0);
        poseStack.scale(scale, scale, 1.0f);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineWidth = font.width(line);
            font.drawShadow(poseStack, line, -lineWidth / 2.0f, i * lineHeight, textColor);
        }
        poseStack.popPose();
    }

    private List<String> wrapText(String text, Font font, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) return lines;
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (font.width(testLine) <= maxWidth) {
                if (!currentLine.isEmpty()) currentLine.append(" ");
                currentLine.append(word);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            }
        }
        if (!currentLine.isEmpty()) lines.add(currentLine.toString());
        return lines;
    }

    public boolean shouldHideCrosshair() {
        return sleepStateManager.isSleeping() && isOverlayVisible;
    }

    public boolean shouldHideChat() {
        return config.isHideChatWhenSleeping() && sleepStateManager.isSleeping() && isOverlayVisible;
    }
}