package supersymmetry.client.renderer.textures.custom;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.client.renderer.texture.Textures;

public class ExtenderRender implements IIconRegister {

    private final String basePath;

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite[] textures;

    public ExtenderRender(String basePath) {
        this.basePath = basePath;
        Textures.iconRegisters.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        String formattedBase = GTValues.MODID + ":blocks/" + basePath;
        this.textures = new TextureAtlasSprite[3];
        this.textures[0] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/in"));
        this.textures[1] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/side"));
        this.textures[2] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/out"));
    }

    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                       EnumFacing outFace, EnumFacing inFace) {
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            TextureAtlasSprite baseSprite = renderSide == inFace ? textures[0] :
                    renderSide == outFace ? textures[2] : textures[1];
            Textures.renderFace(renderState, translation, pipeline, renderSide, Cuboid6.full, baseSprite,
                    BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        return textures[0];
    }
}
