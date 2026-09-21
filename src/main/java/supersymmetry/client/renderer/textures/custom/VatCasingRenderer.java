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
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;

/**
 * Renders a full cube like the multiblock tank casing blocks: the vat texture
 * always on the top face (looking up) and the given casing texture on the sides
 * and bottom. The fluid itself is rendered on top of the block separately.
 */
public class VatCasingRenderer implements ICubeRenderer {

    private final String topTexture;
    private final String sideTexture;

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite topSprite;
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite sideSprite;

    public VatCasingRenderer(String topTexture, String sideTexture) {
        this.topTexture = topTexture;
        this.sideTexture = sideTexture;
        Textures.iconRegisters.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        this.topSprite = textureMap.registerSprite(new ResourceLocation(topTexture));
        this.sideSprite = textureMap.registerSprite(new ResourceLocation(sideTexture));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleSprite() {
        return topSprite;
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getTopSprite() {
        return topSprite;
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getSideSprite() {
        return sideSprite;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        render(renderState, translation, pipeline, Cuboid6.full);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            TextureAtlasSprite sprite = facing == EnumFacing.UP ? topSprite : sideSprite;
            Textures.renderFace(renderState, translation, pipeline, facing, bounds, sprite,
                    BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                    Cuboid6 bounds, EnumFacing frontFacing, boolean isActive,
                                    boolean isWorkingEnabled) {
        render(renderState, translation, pipeline, bounds);
    }
}
