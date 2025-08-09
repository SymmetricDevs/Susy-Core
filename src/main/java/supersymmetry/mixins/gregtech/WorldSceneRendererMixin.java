package supersymmetry.mixins.gregtech;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import gregtech.client.renderer.scene.WorldSceneRenderer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.api.util.BlockRenderManager;

@Mixin(value = WorldSceneRenderer.class, remap = false)
public class WorldSceneRendererMixin {

    @ModifyExpressionValue(method = "lambda$drawWorld$0",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/block/Block;canRenderInLayer(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockRenderLayer;)Z"))
    public boolean ignoreBlocked(boolean original, @Local(name = "pos") BlockPos pos) {
        return original && !BlockRenderManager.modelDisabled.contains(pos);
    }
}
