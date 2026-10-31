package supersymmetry.mixins.ichunutil;

import net.minecraft.entity.Entity;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "me.ichun.mods.ichunutil.client.entity.EntityLatchedRenderer", remap = false)
public class LatchedBrightnessMixin {

    @Shadow
    public Entity latchedEnt;

    @Inject(method = { "getBrightnessForRender", "func_70070_b" }, at = @At("HEAD"), cancellable = true)
    private void orphan(CallbackInfoReturnable<Integer> cir) {
        if (latchedEnt == null) cir.setReturnValue(0);
    }
}
