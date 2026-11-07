package mnightmares.client.ui.config;

import mnightmares.MidnightNightmares;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

final class ConfigScreenAssets {
    static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(MidnightNightmares.MOD_ID, "icon.png");

    static final String TITLE_KEY = "screen.mnightmares.config";
    static final String NOTE_SERVER_FILE_KEY = "screen.mnightmares.config.note.server_file";

    static final int ICON_SIZE = 36;

    private static final int ICON_TEXTURE_SIZE = 36;

    private ConfigScreenAssets() {}

    static boolean editable() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null || mc.hasSingleplayerServer();
    }

    static void drawIcon(GuiGraphics graphics, int x, int y) {
        graphics.blit(ICON, x, y, ICON_SIZE, ICON_SIZE,
                0f, 0f, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE);
    }
}
