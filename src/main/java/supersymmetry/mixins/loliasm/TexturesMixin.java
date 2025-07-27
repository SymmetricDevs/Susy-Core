package supersymmetry.mixins.loliasm;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.util.Mods;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.client.sprite.ondemand.IAnimatedSpritePrimer;
import zone.rong.loliasm.client.sprite.ondemand.ICompiledChunkExpander;
import zone.rong.loliasm.config.LoliConfig;

@Mixin(value = Textures.class, remap = false)
public abstract class TexturesMixin {

    @Inject(method = "renderFace",
            at = @At(value = "INVOKE",
                    target = "Lcodechicken/lib/render/CCRenderState;render()V"))
    private static void sendAnimatedSprites(CCRenderState renderState,
                                            Matrix4 i,
                                            IVertexOperation[] only,
                                            EnumFacing need,
                                            Cuboid6 the,
                                            TextureAtlasSprite sprite,
                                            BlockRenderLayer right,
                                            CallbackInfo now) {
        if (!LoliConfig.instance.onDemandAnimatedTextures
                || Mods.Optifine.isModLoaded()
                // To prevent adding animated sprites when rendering items
                || renderState.getVertexFormat() == DefaultVertexFormats.ITEM) {
            return;
        }
        if (sprite.hasAnimationMetadata()) {
            CompiledChunk chunk = IAnimatedSpritePrimer.CURRENT_COMPILED_CHUNK.get();
            if (chunk instanceof ICompiledChunkExpander expander) {
                expander.resolve(sprite);
            }
        }
    }
}
