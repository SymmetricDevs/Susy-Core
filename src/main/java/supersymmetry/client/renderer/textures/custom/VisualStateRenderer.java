package supersymmetry.client.renderer.textures.custom;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.client.renderer.CubeRendererState;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.cclop.ColourOperation;
import gregtech.client.renderer.cclop.LightMapOperation;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import supersymmetry.client.model.QuadWrapper;
import supersymmetry.client.renderer.CRSExtension;
import supersymmetry.client.renderer.textures.ConnectedTextures;
import team.chisel.ctm.client.state.CTMExtendedState;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

import static gregtech.api.block.VariantActiveBlock.ACTIVE;

@ParametersAreNonnullByDefault
public class VisualStateRenderer implements ICubeRenderer {

    protected final IBlockState visualState;
    protected final boolean isActive;

    protected TextureAtlasSprite particleSprite;

    public VisualStateRenderer(IBlockState visualState) {
        this(visualState, false);
    }

    public VisualStateRenderer(IBlockState visualState, boolean isActive) {
        this.visualState = visualState;
        this.isActive = isActive;
    }

    public static VisualStateRenderer from(IBlockState visualState) {
        return from(visualState, false);
    }

    public static VisualStateRenderer from(IBlockState visualState, boolean isActive) {
        return new VisualStateRenderer(visualState, isActive);
    }

    public IBlockState getVisualState() {
        return visualState;
    }

    @Override
    public TextureAtlasSprite getParticleSprite() {
        if (this.particleSprite == null) {
            // Lazy init
            this.particleSprite = Minecraft.getMinecraft()
                    .getBlockRendererDispatcher()
                    .getModelForState(visualState)
                    .getParticleTexture();
        }
        return this.particleSprite;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {

        CubeRendererState crs = Textures.RENDER_STATE.get();

        var brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
        var renderPipeline = ArrayUtils.addAll(pipeline, translation);
        IBlockState state = visualState;

        List<BakedQuad> quads = new ArrayList<>();

        if (crs == null || crs.layer == null) { // For items rendering

            IBakedModel model = brd.getModelForState(state);

            if (this.isActive) try {
                state = ((IExtendedBlockState) state).withProperty(ACTIVE, true);
            } catch (Exception ignored) { }

            quads.addAll(model.getQuads(state, null, 0));

            for (var facing : EnumFacing.values()) {
                quads.addAll(model.getQuads(state, facing, 0));
            }

        } else { // For blocks rendering

            BlockRenderLayer layer = crs.layer;
            if (!canRenderInLayer(layer)) return;

            IBlockAccess world = crs.world;
            BlockPos pos = CRSExtension.cast(crs).susy$getPos();

            try {
                state = state.getActualState(world, pos);
            } catch (Exception ignored) { }

            IBakedModel model = brd.getModelForState(state);

            try {
                state = state.getBlock().getExtendedState(state, world, pos);
            } catch (Exception ignored) { }

            if (this.isActive) try {
                var extendedState = ((IExtendedBlockState) state).withProperty(ACTIVE, true);
                state = new CTMExtendedState(extendedState, world, pos);
            } catch (Exception ignored) { }

            boolean emissive = ConfigHolder.client.machinesEmissiveTextures
                    && layer == BloomEffectUtil.getEffectiveBloomLayer();

            if (emissive) renderPipeline = ArrayUtils.addAll(renderPipeline,
                    new LightMapOperation(240, 240),
                    new ColourOperation(0xFFFFFFFF));

            long rand = MathHelper.getPositionRandom(pos);
            quads.addAll(model.getQuads(state, null, rand));

            for (var facing : EnumFacing.values()) {
                if (!crs.shouldSideBeRendered(facing, Cuboid6.full)) continue;
                quads.addAll(model.getQuads(state, facing, rand));
            }
        }

        for (var quad : quads) {
            var quadWrapper = new QuadWrapper(quad);
            renderState.setPipeline(quadWrapper, 0, quadWrapper.getVertices().length, renderPipeline);
            renderState.render();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderOrientedState(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline,
                                    Cuboid6 bounds, EnumFacing facing, boolean isActive, boolean isWorkingEnabled) {

        throw new UnsupportedOperationException("Call VisualStateRenderer#render(CCRenderState, Matrix4, IVertexOperation[]) instead!");
    }

    @Override
    public void registerIcons(TextureMap textureMap) {
        /* Do Nothing */
    }

    public boolean canRenderInLayer(@NotNull BlockRenderLayer layer) {
        return visualState.getBlock().canRenderInLayer(visualState, layer);
    }

    public void override(ResourceLocation... mteIds) {
        for (var mteId : mteIds) {
            ConnectedTextures.REPLACEMENTS.put(mteId, any -> this);
        }
    }
}
