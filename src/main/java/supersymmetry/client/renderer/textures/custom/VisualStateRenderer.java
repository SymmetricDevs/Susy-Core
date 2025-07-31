package supersymmetry.client.renderer.textures.custom;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.github.bsideup.jabel.Desugar;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.client.renderer.textures.ConnectedTextures;
import team.chisel.ctm.client.state.CTMExtendedState;

import javax.annotation.ParametersAreNonnullByDefault;

import static gregtech.api.block.VariantActiveBlock.ACTIVE;

@ParametersAreNonnullByDefault
public class VisualStateRenderer implements ICubeRenderer {

    protected final IBlockState visualState;

    protected final ICubeRenderer delegate;

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
            ConnectedTextures.replacements.put(mteId, any -> this);
        }
    }

    // TODO: coloring
    public void renderVisualState(CCRenderState renderState,
                                  IBlockAccess world,
                                  BlockPos pos, int color) {

        BlockRenderLayer layer = Textures.RENDER_STATE.get().layer;
        IBlockState state = visualState;
        if (layer != null && !canRenderInLayer(layer)) return;

        IBlockAccess visualAccess = new VisualBlockAccess(world, pos, state);

        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        if (isActive) {
            // Workaround for VABlocks
            IBakedModel model = dispatcher.getModelForState(state);
            state = new CTMExtendedState(((IExtendedBlockState) state).withProperty(ACTIVE, true), visualAccess, pos);
            dispatcher.getBlockModelRenderer().renderModel(visualAccess, model, state, pos, renderState.getBuffer(), true);
        } else {
            dispatcher.renderBlock(state, pos, visualAccess, renderState.getBuffer());
        }
    }

    @Desugar
    private record VisualBlockAccess(IBlockAccess delegate,
                                     BlockPos origin,
                                     IBlockState visualState) implements IBlockAccess {

        @Nullable
        @Override
        public TileEntity getTileEntity(BlockPos pos) {
            return pos == origin ? null : delegate.getTileEntity(pos);
        }

        @Override
        public int getCombinedLight(BlockPos pos, int lightValue) {
            return delegate.getCombinedLight(pos, lightValue);
        }

        @NotNull
        @Override
        public IBlockState getBlockState(BlockPos pos) {
            return pos == origin ? visualState : delegate.getBlockState(pos);
        }

        @Override
        public boolean isAirBlock(BlockPos pos) {
            return delegate.isAirBlock(pos);
        }

        @NotNull
        @Override
        public Biome getBiome(BlockPos pos) {
            return delegate.getBiome(pos);
        }

        @Override
        public int getStrongPower(BlockPos pos, EnumFacing direction) {
            return delegate.getStrongPower(pos, direction);
        }

        @NotNull
        @Override
        public WorldType getWorldType() {
            return delegate.getWorldType();
        }

        @Override
        public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
            return pos == origin ? visualState.isSideSolid(this, origin, side) : delegate.isSideSolid(pos, side, _default);
        }
    }
}
