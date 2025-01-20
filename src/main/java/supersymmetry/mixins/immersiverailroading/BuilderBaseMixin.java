package supersymmetry.mixins.immersiverailroading;

import cam72cam.immersiverailroading.track.BuilderBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supersymmetry.api.SusyLog;

@Mixin(value=BuilderBase.class, remap = false)
public abstract class BuilderBaseMixin {

    @Inject(method="build", at = @At("TAIL"))
    public void build(CallbackInfo ci) {
        SusyLog.logger.info("succesfully injected");
    }
}
