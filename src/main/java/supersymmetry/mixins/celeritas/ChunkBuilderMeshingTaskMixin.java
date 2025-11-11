package supersymmetry.mixins.celeritas;

import net.minecraft.util.math.BlockPos.MutableBlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.taumc.celeritas.impl.render.terrain.compile.task.ChunkBuilderMeshingTask;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import supersymmetry.api.util.RenderMaskManager;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
public class ChunkBuilderMeshingTaskMixin {

    @ModifyExpressionValue(method = "execute(Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildContext;Lorg/embeddedt/embeddium/impl/util/task/CancellationToken;)Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildOutput;",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/block/Block;canRenderInLayer(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockRenderLayer;)Z"))
    private boolean noIfHidden(boolean original, @Local(name = "blockPos") MutableBlockPos blockPos) {
        return original && !RenderMaskManager.isModelDisabledRaw(blockPos);
    }
}
