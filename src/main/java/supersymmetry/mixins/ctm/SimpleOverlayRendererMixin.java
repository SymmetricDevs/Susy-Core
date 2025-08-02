package supersymmetry.mixins.ctm;

import codechicken.lib.vec.Matrix4;
import com.llamalad7.mixinextras.sugar.Local;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.utils.RenderUtil;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import supersymmetry.client.renderer.textures.ConnectedTextures;

@Mixin(value = SimpleOverlayRenderer.class, remap = false)
public abstract class SimpleOverlayRendererMixin implements ICubeRenderer {

    @ModifyArg(method = "renderOrientedState",
               at = @At(value = "INVOKE",
                        target = "Lgregtech/client/renderer/texture/Textures;renderFace(Lcodechicken/lib/render/CCRenderState;Lcodechicken/lib/vec/Matrix4;[Lcodechicken/lib/render/pipeline/IVertexOperation;Lnet/minecraft/util/EnumFacing;Lcodechicken/lib/vec/Cuboid6;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/util/BlockRenderLayer;)V"),
               require = 3)
    private Matrix4 fixZFighting(Matrix4 renderTranslation, @Local(argsOnly = true) EnumFacing facing) {

        if (ConnectedTextures.shouldOffset(this)) {
            renderTranslation = RenderUtil.adjustTrans(renderTranslation, facing, 4);
        }
        return renderTranslation;
    }
}
