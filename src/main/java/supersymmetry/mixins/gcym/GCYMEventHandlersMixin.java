package supersymmetry.mixins.gcym;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregicality.multiblocks.common.GCYMEventHandlers;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.event.PostMaterialEvent;

@Mixin(value = GCYMEventHandlers.class, remap = false)
public abstract class GCYMEventHandlersMixin {

    @Inject(method = "registerMaterials", at = @At("HEAD"), cancellable = true)
    private static void susy$disableGCYMMaterials(MaterialEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "registerMaterialsPost", at = @At("HEAD"), cancellable = true)
    private static void susy$disableGCYMPostMaterials(PostMaterialEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
