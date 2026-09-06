package supersymmetry.client.renderer.pipe;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.Nullable;

import codechicken.lib.lighting.LightMatrix;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.uv.IconTransformation;
import dev.tianmi.sussypatches.api.core.mixin.extension.IconTypeExtension;
import dev.tianmi.sussypatches.api.util.SusUtil;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.client.renderer.pipe.PipeRenderer;
import gregtech.client.renderer.texture.Textures;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import supersymmetry.Supersymmetry;
import supersymmetry.api.pipelike.CustomContext;
import supersymmetry.api.unification.material.info.SuSyMaterialIconType;
import supersymmetry.common.pipelike.tanklessfluid.BlockTanklessFluidPipe;
import supersymmetry.common.pipelike.tanklessfluid.TanklessFluidPipeType;
import supersymmetry.common.pipelike.tanklessfluid.tile.TileEntityTanklessFluidPipe;

public final class TanklessFluidPipeRenderer extends PipeRenderer implements IconTypeExtension, CustomContext {

    public static final TanklessFluidPipeRenderer INSTANCE = new TanklessFluidPipeRenderer();

    private static final List<MaterialIconType> PIPE_ICON_TYPES = Arrays.asList(
            SuSyMaterialIconType.pipeTinyTanklessFluid,
            SuSyMaterialIconType.pipeSmallTanklessFluid,
            SuSyMaterialIconType.pipeNormalTanklessFluid,
            SuSyMaterialIconType.pipeLargeTanklessFluid,
            SuSyMaterialIconType.pipeHugeTanklessFluid,
            SuSyMaterialIconType.pipeSideTanklessFluid);

    private static final int ITEM_FLANGE_VISIBILITY = 0b1100;

    private TanklessFluidPipeRenderer() {
        super("tankless_fluid_pipe", new ResourceLocation(Supersymmetry.MODID, "tankless_fluid_pipe"));
    }

    @Override
    public void onIconRegister(TextureMap textureMap) {
        for (val iconType : PIPE_ICON_TYPES) {
            for (val iconSet : MaterialIconSet.ICON_SETS.values()) {
                textureMap.registerSprite(iconType.getBlockTexturePath(iconSet));
            }
        }
    }

    @Override
    public void registerIcons(TextureMap textureMap) {
        // no-op
    }

    @Override
    public void buildRenderer(PipeRenderContext pipeRenderContext, BlockPipe<?, ?, ?> blockPipe,
                              @Nullable IPipeTile<?, ?> iPipeTile, IPipeType<?> iPipeType,
                              @Nullable Material material) {
        if (material == null || !(iPipeType instanceof TanklessFluidPipeType pipeType) ||
                !(pipeRenderContext instanceof RenderContext renderContext)) {
            return;
        }

        renderContext
                .flangeVisibility(iPipeTile instanceof TileEntityTanklessFluidPipe tanklessPipe ?
                        tanklessPipe.getFlangeVisibility() : ITEM_FLANGE_VISIBILITY)
                .addOpenFaceRender(new IconTransformation(SusUtil.getBlockSprite(getIconType(pipeType), material)))
                .addSideRender(new IconTransformation(
                        SusUtil.getBlockSprite(SuSyMaterialIconType.pipeSideTanklessFluid, material)));
        if (pipeType.isRestrictive()) {
            renderContext.addOpenFaceRender(false, new IconTransformation(Textures.RESTRICTIVE_OVERLAY));
            renderContext.addSideRender(false, new IconTransformation(Textures.RESTRICTIVE_OVERLAY));
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IPipeType<?> iPipeType, @Nullable Material material) {
        return material == null ? Textures.PIPE_SIDE :
                SusUtil.getBlockSprite(SuSyMaterialIconType.pipeSideTanklessFluid, material);
    }

    @Override
    protected void renderPipeCube(CCRenderState renderState, PipeRenderContext renderContext, EnumFacing side) {
        super.renderPipeCube(renderState, renderContext, side);
        if ((((RenderContext) renderContext).flangeVisibility() & (1 << side.getIndex())) == 0) return;
        renderFlange(renderState, renderContext, side);
    }

    private void renderFlange(CCRenderState renderState, PipeRenderContext renderContext, EnumFacing side) {
        // bit 12 + side index = a cover sits on this side; recess the flange front face so it renders behind it
        boolean hasCover = (renderContext.getConnections() & 1 << (12 + side.getIndex())) > 0;
        val cuboid = BlockTanklessFluidPipe.getFlangeBox(side, renderContext.getPipeThickness(), hasCover);
        for (val renderedSide : EnumFacing.VALUES) {
            renderOpenFace(renderState, renderContext, renderedSide, cuboid);
        }
    }

    private static MaterialIconType getIconType(TanklessFluidPipeType pipeType) {
        return switch (pipeType) {
            case TINY, RESTRICTIVE_TINY -> SuSyMaterialIconType.pipeTinyTanklessFluid;
            case SMALL, RESTRICTIVE_SMALL -> SuSyMaterialIconType.pipeSmallTanklessFluid;
            case LARGE, RESTRICTIVE_LARGE -> SuSyMaterialIconType.pipeLargeTanklessFluid;
            case HUGE, RESTRICTIVE_HUGE -> SuSyMaterialIconType.pipeHugeTanklessFluid;
            default -> SuSyMaterialIconType.pipeNormalTanklessFluid;
        };
    }

    @Override
    public PipeRenderContext createRenderContext(@org.jspecify.annotations.Nullable BlockPos pos,
                                                 @org.jspecify.annotations.Nullable LightMatrix lightMatrix,
                                                 int connections, int blockedConnections, float thickness) {
        return new RenderContext(pos, lightMatrix, connections, blockedConnections, thickness);
    }

    private static final class RenderContext extends PipeRenderContext {

        @Accessors(fluent = true)
        @Getter
        @Setter
        private int flangeVisibility;

        public RenderContext(@Nullable BlockPos pos, @Nullable LightMatrix lightMatrix, int connections,
                             int blockedConnections, float thickness) {
            super(pos, lightMatrix, connections, blockedConnections, thickness);
        }
    }
}
