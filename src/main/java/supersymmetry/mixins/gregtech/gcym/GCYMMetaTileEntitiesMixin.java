package supersymmetry.mixins.gregtech.gcym;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregicality.multiblocks.common.metatileentities.GCYMMetaTileEntities;

@Mixin(value = GCYMMetaTileEntities.class, remap = false)
public abstract class GCYMMetaTileEntitiesMixin {

    @Inject(
            method = "init",
            at = @At("HEAD"),
            cancellable = true)
    private static void susy$disableGCYMMetaTileEntities(CallbackInfo ci) {
        ci.cancel();
    }
}
