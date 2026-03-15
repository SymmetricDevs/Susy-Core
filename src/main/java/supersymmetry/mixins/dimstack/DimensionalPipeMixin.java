package supersymmetry.mixins.dimstack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cd4017be.dimstack.tileentity.DimensionalPipe;
import cd4017be.lib.tileentity.BaseTileEntity;

@Mixin(value = DimensionalPipe.class, remap = false)
public abstract class DimensionalPipeMixin extends BaseTileEntity {

    @Inject(method = "link", at = @At("TAIL"))
    void link(DimensionalPipe tile, CallbackInfo ci) {
        world.notifyNeighborsOfStateChange(pos, blockType, true);
    }
}
