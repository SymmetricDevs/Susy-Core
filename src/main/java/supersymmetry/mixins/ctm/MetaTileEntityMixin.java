package supersymmetry.mixins.ctm;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.api.metatileentity.IConnectable;

@Mixin(value = MetaTileEntity.class, remap = false)
public abstract class MetaTileEntityMixin {

    @ModifyReturnValue(method = "canRenderInLayer", at = @At("TAIL"))
    private boolean injectConnectableLogic(boolean original, @Local(argsOnly = true) BlockRenderLayer renderLayer) {
        // Putting the extra checks after || to decrease call frequency
        return original || (this instanceof IConnectable connectable && connectable.shouldRenderInLayerExtra(renderLayer));
    }
}
