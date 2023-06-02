package supersymmetry.client.renderer.textures;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
public class SidedDrumRenderer implements TextureUtils.IIconRegister {
    private final String basePath;
    private final boolean hasFront;
    private final boolean hasBack;
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite[] textures;

    public SidedDrumRenderer(String basePath, boolean hasFront, boolean hasBack) {
        this.basePath = basePath;
        Textures.iconRegisters.add(this);
        this.hasFront = hasFront;
        this.hasBack = hasBack;
    }

    public SidedDrumRenderer(String basePath) {
        this.basePath = basePath;
        Textures.iconRegisters.add(this);
        this.hasFront = false;
        this.hasBack = false;
    }

    //In the case that you don't want to use overlays and rather directly play with the texture
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        String formattedBase = "gregtech:blocks/" + this.basePath;
        this.textures = new TextureAtlasSprite[3 + (hasFront ? 1 : 0) + (hasBack ? 1 : 0)];
        this.textures[0] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/top"));
        this.textures[1] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/side"));
        this.textures[2] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/bottom"));
        if (hasFront) this.textures[3] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/front"));
        if (hasBack) this.textures[3 + (hasFront ? 1 : 0)] = textureMap.registerSprite(new ResourceLocation(formattedBase + "/back"));
    }

    //respects sided-ness in the case that you want that (admittedly useless in the face of overlays, but oh well)
    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, EnumFacing rotation) {
        EnumFacing[] facings = EnumFacing.VALUES;

        for (EnumFacing renderSide : facings) {
            TextureAtlasSprite baseSprite;

            //rotation is this drum's front's facing (direction)
            if (renderSide == EnumFacing.UP) baseSprite = this.textures[0];
            else if (renderSide == EnumFacing.DOWN) baseSprite = this.textures[2];
            else if (hasFront && renderSide == rotation) baseSprite = this.textures[3];
            else if (hasBack && renderSide == rotation.getOpposite())
                baseSprite = this.textures[3 + (hasFront ? 1 : 0)];
            else baseSprite = this.textures[1];

            Textures.renderFace(renderState, translation, pipeline, renderSide, Cuboid6.full, baseSprite, BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        return this.textures[0];
    }

}
