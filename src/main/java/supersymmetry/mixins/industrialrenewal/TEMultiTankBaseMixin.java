package supersymmetry.mixins.industrialrenewal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cassiokf.industrialrenewal.tileentity.abstracts.TEMultiTankBase;

/**
 * Stacked storage racks, fluid tanks and battery banks share the bottom unit's storage through {@code bottomTE}.
 * After a load, only the top unit sets it: {@code passValueDown} walks down through the units below and returns
 * the bottom one, but never stores it on the units it passes. The middle units of a stack of three or more keep
 * pointing at themselves and serve their own, separate storage. Store the bottom unit on every unit the walk
 * passes.
 */
@Mixin(value = TEMultiTankBase.class, remap = false)
public class TEMultiTankBaseMixin {

    @Shadow
    public TEMultiTankBase bottomTE;

    @Inject(method = "passValueDown", at = @At("RETURN"))
    private void susy$linkBottom(int i, TEMultiTankBase top, CallbackInfoReturnable<TEMultiTankBase> cir) {
        this.bottomTE = cir.getReturnValue();
    }
}
