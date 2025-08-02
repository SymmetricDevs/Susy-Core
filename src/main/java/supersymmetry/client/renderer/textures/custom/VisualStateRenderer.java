package supersymmetry.client.renderer.textures.custom;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.jetbrains.annotations.NotNull;
import supersymmetry.client.renderer.textures.ConnectedTextures;
import team.chisel.ctm.client.state.CTMExtendedState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class VisualStateRenderer implements ICubeRenderer {

    protected final IBlockState visualState;

    protected final ICubeRenderer delegate;

    protected final boolean isActive;

    public VisualStateRenderer(IBlockState visualState, ICubeRenderer delegate) {
        this(visualState, delegate, false);
    }

    public VisualStateRenderer(IBlockState visualState, ICubeRenderer delegate, boolean isActive) {
        if (visualState instanceof IExtendedBlockState extendedState) {
            visualState = extendedState.getClean();
        }
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
        return delegate.getParticleSprite();
    }

    @Override
    public void renderOrientedState(CCRenderState renderState,
                                    Matrix4 translation,
                                    IVertexOperation[] pipeline,
                                    Cuboid6 bounds,
                                    EnumFacing frontFacing,
                                    boolean isActive,
                                    boolean isWorkingEnabled) {

        delegate.renderOrientedState(renderState, translation, pipeline,
                bounds, frontFacing, isActive, isWorkingEnabled);
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

    // TODO: coloring
    public void renderVisualState(CCRenderState renderState,
                                  IBlockAccess world,
                                  BlockPos pos, int color) {

        BlockRenderLayer layer = Textures.RENDER_STATE.get().layer;
        if (layer != null && !canRenderInLayer(layer)) return;
        IBlockState state = visualState;

        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        BufferBuilder buffer = renderState.getBuffer();

//        if (isActive) {
            // Workaround for VABlocks
            IBakedModel model = dispatcher.getModelForState(state);
//            state = new CTMExtendedState(((IExtendedBlockState) state).withProperty(ACTIVE, true), world, pos);
            state = new CTMExtendedState(state, world, pos);
            dispatcher.getBlockModelRenderer().renderModel(world, model, state, pos, buffer, true);
//        } else {
//            dispatcher.renderBlock(state, pos, world, buffer);
//        }
    }
}
