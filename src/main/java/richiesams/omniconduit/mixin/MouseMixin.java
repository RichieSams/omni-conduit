package richiesams.omniconduit.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import richiesams.omniconduit.events.MouseEvents;

@Environment(EnvType.CLIENT)
@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(at = @At("HEAD"), method = "onMouseScroll", cancellable = true)
    void onMouseScrolled(long window, double dx, double dy, CallbackInfo ci) {
        if (MouseEvents.MOUSE_WHEEL_SCROLLED.invoker().onMouseScrolled(dx, dy)) {
            ci.cancel();
        }
    }
}