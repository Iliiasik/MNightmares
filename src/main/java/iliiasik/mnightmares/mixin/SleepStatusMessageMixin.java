package iliiasik.mnightmares.mixin;

import iliiasik.mnightmares.config.NightmaresConfig;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class SleepStatusMessageMixin {
    @Inject(method = "announceSleepStatus", at = @At("HEAD"), cancellable = true)
    private void mnightmares$suppressSleepStatus(CallbackInfo ci) {
        if (NightmaresConfig.getInstance().getServer().suppressVanillaSleepMessages) {
            ci.cancel();
        }
    }
}
