package supersymmetry.client.renderer.textures.custom;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.github.bsideup.jabel.Desugar;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
public class VisualStateRenderer implements ICubeRenderer {

    protected final IBlockState visualState;

    @SideOnly(Side.CLIENT)
    protected TextureAtlasSprite particleSprite;

    @SideOnly(Side.CLIENT)
    protected Map<EnumFacing, TextureAtlasSprite> sprites;

    public VisualStateRenderer(IBlockState visualState) {
        this.visualState = visualState;
    }

    public VisualStateRenderer(Block block) {
        this(block.getDefaultState());
    }

    public IBlockState getVisualState() {
        return visualState;
    }

    @Override
    public TextureAtlasSprite getParticleSprite() {
        return this.particleSprite;
    }

    @Override
    public void renderOrientedState(CCRenderState renderState,
                                    Matrix4 translation,
                                    IVertexOperation[] pipeline,
                                    Cuboid6 bounds,
                                    EnumFacing frontFacing,
                                    boolean isActive,
                                    boolean isWorkingEnabled) {

        for (var facing : EnumFacing.values()) {
            Textures.renderFace(renderState, translation, pipeline, facing, bounds,
                    sprites.get(facing), BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

    @Override
    public void registerIcons(TextureMap textureMap) {
        var dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBakedModel model = dispatcher.getModelForState(visualState);

        this.particleSprite = model.getParticleTexture();
        this.sprites = new EnumMap<>(EnumFacing.class);

        for (int i = 0; i < EnumFacing.values().length; i++) {
            EnumFacing facing = EnumFacing.byIndex(i);
            List<BakedQuad> quads = model.getQuads(visualState, facing, 0);
            if (quads.isEmpty()) {
                sprites.put(facing, textureMap.getMissingSprite());
                continue;
            }
            sprites.put(facing, quads.get(0).getSprite());
        }
    }

    public boolean canRenderInLayer(@NotNull BlockRenderLayer layer) {
        return visualState.getBlock().canRenderInLayer(visualState, layer);
    }

    // TODO: coloring
    public void renderVisualState(CCRenderState renderState, IBlockAccess world,
                                  BlockPos pos, @Nullable Integer color) {
        BlockRenderLayer layer = Textures.RENDER_STATE.get().layer;
        IBlockState state = visualState;
        if (layer != null && !canRenderInLayer(layer)) return;

        IBlockAccess visualAccess = new VisualBlockAccess(world, pos, state);

        try {
            state = state.getActualState(visualAccess, pos);
        } catch (Exception ignored) {
            /* Do Nothing */
        }

        try {
            state = state.getBlock().getExtendedState(state, visualAccess, pos);
        } catch (Exception ignored) {
            /* Do Nothing */
        }

        Minecraft.getMinecraft().getBlockRendererDispatcher()
                .renderBlock(state, pos, world, renderState.getBuffer());
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
