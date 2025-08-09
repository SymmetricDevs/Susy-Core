package supersymmetry.mixins.minecraft;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.api.util.BlockRenderManager;

@Mixin(RenderChunk.class)
public abstract class RenderChunkMixin {

    @WrapOperation(method = "rebuildChunk",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/BlockRendererDispatcher;renderBlock(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/BufferBuilder;)Z"))
    public boolean lockBuilding(BlockRendererDispatcher dispatcher,
                                IBlockState state,
                                BlockPos pos,
                                IBlockAccess blockAccess,
                                BufferBuilder bufferBuilder,
                                Operation<Boolean> method) {
        BlockRenderManager.isBuildingChunk.set(true);
        if (BlockRenderManager.isModelDisabled(pos)) {
            BlockRenderManager.isBuildingChunk.set(false);
            return false;
        }
        boolean rst = method.call(dispatcher, state, pos, blockAccess, bufferBuilder);
        BlockRenderManager.isBuildingChunk.set(false);
        return rst;
    }
}
