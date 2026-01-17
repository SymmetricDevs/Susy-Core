package supersymmetry.mixins.minecraft;

import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import supersymmetry.api.util.RenderMaskManager;

@Mixin(BlockModelRenderer.class)
public abstract class BlockModelRendererMixin {

    @ModifyExpressionValue(method = { "renderModelFlat", "renderModelSmooth" },
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/block/state/IBlockState;shouldSideBeRendered(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z"))
    public boolean ignoreBlocked(boolean original, @Local(argsOnly = true) BlockPos pos, @Local EnumFacing facing) {
        return original || RenderMaskManager.isModelDisabled(pos.offset(facing));
    }
}
