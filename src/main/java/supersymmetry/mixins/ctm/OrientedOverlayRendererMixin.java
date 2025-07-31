package supersymmetry.mixins.ctm;

import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Transformation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import gregtech.client.renderer.texture.cube.OrientedOverlayRenderer;
import gregtech.client.utils.RenderUtil;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = OrientedOverlayRenderer.class, remap = false)
public abstract class OrientedOverlayRendererMixin {

    @WrapOperation(method = "renderOrientedState",
                   at = @At(value = "INVOKE",
                            target = "Lcodechicken/lib/vec/Matrix4;apply(Lcodechicken/lib/vec/Transformation;)Lcodechicken/lib/vec/Matrix4;"))
    private Matrix4 adjustTransCorrectly(Matrix4 renderTranslation,
                                         Transformation rotation,
                                         Operation<Matrix4> method,
                                         @Local(name = "renderTranslation") LocalRef<Matrix4> translationRef,
                                         @Local(name = "renderSide") EnumFacing renderSide) {

        Matrix4 res = method.call(renderTranslation, rotation);
        translationRef.set(RenderUtil.adjustTrans(res, renderSide, 1));
        return null; // Return value omitted
    }
}
