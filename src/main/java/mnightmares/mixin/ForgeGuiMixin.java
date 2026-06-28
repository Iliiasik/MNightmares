package mnightmares.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import mnightmares.client.MidnightNightmaresClient;
import mnightmares.client.render.SleepOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeGui.class)
public abstract class ForgeGuiMixin {
    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;F)V",
            at = @At("RETURN")
    )
    private void mnightmares$onRenderHud(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        MidnightNightmaresClient instance = MidnightNightmaresClient.getInstance();
        if (instance == null) return;
        SleepOverlayRenderer renderer = instance.getOverlayRenderer();
        if (renderer != null && renderer.isActive()) {
            Minecraft mc = Minecraft.getInstance();
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            renderer.renderBloodOverlay(poseStack, width, height);
            renderer.renderContent(poseStack, width, height);
        }
    }
}