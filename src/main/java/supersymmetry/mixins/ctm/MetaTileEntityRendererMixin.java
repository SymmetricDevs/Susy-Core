package supersymmetry.mixins.ctm;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.client.renderer.CubeRendererState;
import gregtech.client.renderer.handler.MetaTileEntityRenderer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.client.renderer.CRSExtension;

@Mixin(value = MetaTileEntityRenderer.class, remap = false)
public class MetaTileEntityRendererMixin {

    @ModifyExpressionValue(method = "renderBlock",
                           at = @At(value = "NEW",
                                    target = "gregtech/client/renderer/CubeRendererState"))
    private CubeRendererState replaceWorld(CubeRendererState original,
                                           @Local(argsOnly = true) BlockPos pos,
                                           @Local(name = "metaTileEntity") MetaTileEntity mte) {

        return CRSExtension.cast(original).susy$withPos(pos);
    }
}
