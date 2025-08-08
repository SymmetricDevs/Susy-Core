package supersymmetry.mixins.gregtech;

import gregtech.api.pipenet.tile.PipeCoverableImplementation;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Deprecated
@ScheduledForRemoval(inVersion = "Next CEu update")
@Mixin(value = TileEntityPipeBase.class, remap = false)
public abstract class TileEntityPipeBaseMixin {

    @Redirect(method = "transferDataFrom",
            at = @At(value = "INVOKE",
                    target = "Lgregtech/api/pipenet/tile/PipeCoverableImplementation;transferDataTo(Lgregtech/api/pipenet/tile/PipeCoverableImplementation;)V"))
    private void reverseTransferTarget(PipeCoverableImplementation self, PipeCoverableImplementation other) {
        other.transferDataTo(self);
    }

    @Inject(method = "isFaceBlocked(Lnet/minecraft/util/EnumFacing;)Z",
            at = @At(value = "HEAD"),
            cancellable = true)
    private void checkNotNull(EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        if (side == null) cir.setReturnValue(false);
    }
}
