package supersymmetry.mixins.minecraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.api.util.BlockRenderManager;

@Mixin(BlockModelRenderer.class)
public abstract class BlockModelRendererMixin {

    @WrapOperation(method = {"renderModelFlat", "renderModelSmooth"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/state/IBlockState;shouldSideBeRendered(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z"))
    public boolean ignoreBlocked(IBlockState state, IBlockAccess blockAccess,
                                 BlockPos pos, EnumFacing facing, Operation<Boolean> method) {
        return BlockRenderManager.isModelDisabled(pos.offset(facing)) || method.call(state, blockAccess, pos, facing);
    }
}
