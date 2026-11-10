package iliiasik.mnightmares.mixin;

import iliiasik.mnightmares.client.MidnightNightmaresClient;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiOverlayMessageMixin {
    @Inject(
            method = "setOverlayMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mnightmares$onSetOverlayMessage(CallbackInfo ci) {
        MidnightNightmaresClient instance = MidnightNightmaresClient.getInstance();
        if (instance != null && instance.getOverlayRenderer() != null
                && instance.getOverlayRenderer().shouldHideHudMessages()) {
            ci.cancel();
        }
    }
}
