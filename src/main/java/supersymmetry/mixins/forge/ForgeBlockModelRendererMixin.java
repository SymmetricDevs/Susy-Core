package supersymmetry.mixins.forge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.api.util.RenderMaskManager;

@Mixin(value = ForgeBlockModelRenderer.class, remap = false)
public abstract class ForgeBlockModelRendererMixin {

    @WrapOperation(method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/state/IBlockState;shouldSideBeRendered(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z",
                    remap = true))
    private static boolean ignoreBlocked(
            IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing facing, Operation<Boolean> method) {
        return RenderMaskManager.isModelDisabled(pos.offset(facing)) || method.call(state, blockAccess, pos, facing);
    }
}
