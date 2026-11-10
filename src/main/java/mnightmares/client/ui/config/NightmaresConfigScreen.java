package mnightmares.client.ui.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NightmaresConfigScreen extends Screen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 23;
    private static final int FIRST_BUTTON_TOP = 34;
    private static final int DONE_BOTTOM = 32;
    private static final int TITLE_TOP = 14;
    private static final int NOTE_GAP = 8;
    private static final int ICON_GAP = 8;
    private static final int NOTE_COLOR = 0xA0A0A0;

    private final Screen parent;
    private List<ConfigSection> sections = List.of();

    public NightmaresConfigScreen(Screen parent) {
        super(Component.translatable(ConfigScreenAssets.TITLE_KEY).withStyle(ChatFormatting.BOLD));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - BUTTON_WIDTH / 2;
        sections = ConfigSection.shown(ConfigScreenAssets.editable());
        for (int i = 0; i < sections.size(); i++) {
            ConfigSection section = sections.get(i);
            addRenderableWidget(new Button(x, FIRST_BUTTON_TOP + i * BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT,
                    section.title(),
                    button -> Minecraft.getInstance().setScreen(new ConfigSectionScreen(this, section))));
        }
        addRenderableWidget(new Button(x, this.height - DONE_BOTTOM, BUTTON_WIDTH, BUTTON_HEIGHT,
                CommonComponents.GUI_DONE, button -> onClose()));
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, TITLE_TOP, 0xFFFFFF);

        int note = this.height - DONE_BOTTOM - NOTE_GAP - this.font.lineHeight;
        drawCenteredString(poseStack, this.font, Component.translatable(ConfigScreenAssets.NOTE_SERVER_FILE_KEY),
                this.width / 2, note, NOTE_COLOR);

        renderIconAboveNote(poseStack, note);
    }

    private void renderIconAboveNote(PoseStack poseStack, int note) {
        int contentBottom = sections.isEmpty()
                ? TITLE_TOP + this.font.lineHeight
                : FIRST_BUTTON_TOP + (sections.size() - 1) * BUTTON_SPACING + BUTTON_HEIGHT;
        int y = note - ICON_GAP - ConfigScreenAssets.ICON_SIZE;
        if (y < contentBottom + ICON_GAP) return;
        ConfigScreenAssets.drawIcon(poseStack, this.width / 2 - ConfigScreenAssets.ICON_SIZE / 2, y);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
