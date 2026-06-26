package mnightmares.mixin;

import mnightmares.client.MidnightNightmaresClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;III)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mnightmares$onRenderChat(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, CallbackInfo ci) {
        MidnightNightmaresClient instance = MidnightNightmaresClient.getInstance();
        if (instance != null && instance.getOverlayRenderer() != null) {
            if (instance.getOverlayRenderer().shouldHideChat()) {
                ci.cancel();
            }
        }
    }
}
