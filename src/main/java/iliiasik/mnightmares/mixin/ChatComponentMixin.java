package iliiasik.mnightmares.mixin;

import iliiasik.mnightmares.client.MidnightNightmaresClient;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void mnightmares$onRenderChat(CallbackInfo ci) {
        MidnightNightmaresClient instance = MidnightNightmaresClient.getInstance();
        if (instance != null && instance.getOverlayRenderer() != null) {
            if (instance.getOverlayRenderer().shouldHideChat()) {
                ci.cancel();
            }
        }
    }
}