package iliiasik.mnightmares.mixin;

import iliiasik.mnightmares.client.MidnightNightmaresClient;
import iliiasik.mnightmares.client.render.SleepOverlayRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiHudMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void mnightmares$onRenderHud(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MidnightNightmaresClient inst = MidnightNightmaresClient.getInstance();
        if (inst == null) return;
        SleepOverlayRenderer renderer = inst.getOverlayRenderer();
        if (renderer != null && renderer.isActive()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            renderer.renderBloodOverlay(guiGraphics, width, height);
            renderer.renderContent(guiGraphics, width, height);
        }
    }
}