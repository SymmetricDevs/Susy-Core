package supersymmetry.mixins.gregtech;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import gregtech.client.renderer.scene.WorldSceneRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.api.util.BlockRenderManager;

@Mixin(value = WorldSceneRenderer.class, remap = false)
public class WorldSceneRendererMixin {

    @WrapWithCondition(method = "lambda$drawWorld$0",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;canRenderInLayer(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockRenderLayer;)Z"))
    public boolean ignoreBlocked(Block block, IBlockState state, BlockRenderLayer layer, @Local(name = "pos") BlockPos pos) {
        return !BlockRenderManager.modelDisabled.contains(pos);
    }
}
