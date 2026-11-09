package supersymmetry.mixins.gregtech;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.cover.CoverRayTracer;

@Mixin(value = CoverRayTracer.class, remap = false)
public class WrenchHitMixin {

    @Inject(method = "determineGridSideHit", at = @At("HEAD"), cancellable = true)
    private static void missingHit(RayTraceResult hit, CallbackInfoReturnable<EnumFacing> cir) {
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK || hit.sideHit == null || hit.hitVec == null ||
                hit.getBlockPos() == null)
            cir.setReturnValue(null);
    }
}
