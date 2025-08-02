package supersymmetry.client.renderer.textures.custom;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.github.bsideup.jabel.Desugar;
import gregtech.client.renderer.CubeRendererState;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.cclop.ColourOperation;
import gregtech.client.renderer.cclop.LightMapOperation;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.BloomEffectUtil;
import gregtech.common.ConfigHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
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
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import supersymmetry.client.model.QuadWrapper;
import supersymmetry.client.renderer.CRSExtension;
import supersymmetry.client.renderer.textures.ConnectedTextures;
import team.chisel.ctm.client.state.CTMExtendedState;

import javax.annotation.ParametersAreNonnullByDefault;

import static gregtech.api.block.VariantActiveBlock.ACTIVE;

@ParametersAreNonnullByDefault
public class VisualStateRenderer implements ICubeRenderer {

    protected final IBlockState visualState;

    protected final ICubeRenderer delegate;

    protected TextureAtlasSprite particleSprite;

    protected final boolean isActive;

    public VisualStateRenderer(IBlockState visualState, ICubeRenderer delegate) {
        this(visualState, delegate, false);
    }

    public VisualStateRenderer(IBlockState visualState, ICubeRenderer delegate, boolean isActive) {
        this.visualState = visualState;
        this.delegate = delegate;
        this.isActive = isActive;
    }

    public static VisualStateRenderer from(IBlockState visualState, ICubeRenderer delegate) {
        return from(visualState, delegate, false);
    }

    public static VisualStateRenderer from(IBlockState visualState, ICubeRenderer delegate, boolean isActive) {
        return new VisualStateRenderer(visualState, delegate, isActive);
    }

    public IBlockState getVisualState() {
        return visualState;
    }

    @Override
    public TextureAtlasSprite getParticleSprite() {
        if (this.particleSprite == null) {
            this.particleSprite = Minecraft.getMinecraft()
                    .getBlockRendererDispatcher()
                    .getModelForState(visualState)
                    .getParticleTexture();
        }
        return this.particleSprite;
    }

    @Override
    public void renderOrientedState(CCRenderState renderState,
                                    Matrix4 translation,
                                    IVertexOperation[] pipeline,
                                    Cuboid6 bounds,
                                    EnumFacing facing,
                                    boolean isActive,
                                    boolean isWorkingEnabled) {

        CubeRendererState crs = Textures.RENDER_STATE.get();

        if (crs == null || crs.layer == null) {
            // TODO: handle item rendering
            return;
        }

        BlockRenderLayer layer = crs.layer;
        if (!crs.shouldSideBeRendered(facing, bounds) || !canRenderInLayer(layer)) return;

        IBlockState state = visualState;
        IBlockAccess world = crs.world;
        BlockPos pos = CRSExtension.cast(crs).susy$getPos();

        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        try {
            state = state.getActualState(world, pos);
        } catch (Exception ignored) {}

        IBakedModel model = dispatcher.getModelForState(state);

        try {
            state = state.getBlock().getExtendedState(state, world, pos);
        } catch (Exception ignored) {}

        if (this.isActive) try {
            var extendedState = ((IExtendedBlockState) state).withProperty(ACTIVE, true);
            state = new CTMExtendedState(extendedState, world, pos);
        } catch (Exception ignored) {}

        boolean emissive = ConfigHolder.client.machinesEmissiveTextures
                && layer == BloomEffectUtil.getEffectiveBloomLayer();

        var renderPipeline = ArrayUtils.addAll(pipeline, translation);
        if (emissive) renderPipeline = ArrayUtils.addAll(renderPipeline,
                new LightMapOperation(240, 240),
                new ColourOperation(0xFFFFFFFF));

        long rand = MathHelper.getPositionRandom(pos);

        for (var quad : model.getQuads(state, facing, rand)) {
            var quadWrapper = new QuadWrapper(quad);
            renderState.setPipeline(quadWrapper, 0, quadWrapper.getVertices().length, renderPipeline);
            renderState.render();
        }
    }

    @Override
    public void registerIcons(TextureMap textureMap) {
        /* Do Nothing */
    }

    public boolean canRenderInLayer(@NotNull BlockRenderLayer layer) {
        return visualState.getBlock().canRenderInLayer(visualState, layer);
    }

    public void replace(ResourceLocation... mteIds) {
        for (var mteId : mteIds) {
            ConnectedTextures.REPLACEMENTS.put(mteId, any -> this);
        }
    }

    @Desugar
    private record VisualRenderState(BlockRenderLayer renderLayer, EnumFacing facing) { }
}
