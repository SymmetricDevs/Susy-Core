package supersymmetry.client.renderer.pipe;

import java.util.Arrays;
import java.util.List;

import codechicken.lib.lighting.LightMatrix;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Cuboid6;
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
import lombok.val;
import supersymmetry.Supersymmetry;
import supersymmetry.api.pipelike.CustomContext;
import supersymmetry.api.unification.material.info.SuSyMaterialIconType;
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
        if (material == null || !(iPipeType instanceof TanklessFluidPipeType pipeType) || !(pipeRenderContext instanceof RenderContext renderContext)) {
            return;
        }

        renderContext
                .flangeVisibility(iPipeTile instanceof TileEntityTanklessFluidPipe tanklessPipe ? tanklessPipe.getFlangeVisibility() : ITEM_FLANGE_VISIBILITY)
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
        val cuboid = getFlangeBox(side, renderContext.getPipeThickness(), hasCover);
        for (val renderedSide : EnumFacing.VALUES) {
            renderOpenFace(renderState, renderContext, renderedSide, cuboid);
        }
    }

    public static Cuboid6 getFlangeBox(@Nullable EnumFacing side, float thickness, boolean hasCover) {
        // Frame render box is offset by 0.001d inside, so here we offset by 0.0011d to make it rendered correctly
        double min = Math.max((thickness > 0.3 ? 0.375d - thickness / 2.0d : 0.4375d - thickness / 2.0d), 0) + 0.0011d;
        double max = 1 - min;
        // the flange front face sits right at the base pipe cube's face when there is no cover, but is recessed
        // (behind the cover box, which sits at 0.001 / 0.999) when a cover occupies this side
        double faceMin = hasCover ? 0.0011d : -0.0001d;
        double faceMax = 1 - faceMin;
        double flangeMin = 0.1249d;
        double flangeMax = 1 - flangeMin;

        return switch (side) {
            case WEST -> new Cuboid6(faceMin, min, min, flangeMin, max, max);
            case EAST -> new Cuboid6(flangeMax, min, min, faceMax, max, max);
            case NORTH -> new Cuboid6(min, min, faceMin, max, max, flangeMin);
            case SOUTH -> new Cuboid6(min, min, flangeMax, max, max, faceMax);
            case UP -> new Cuboid6(min, flangeMax, min, max, faceMax, max);
            case DOWN -> new Cuboid6(min, faceMin, min, max, flangeMin, max);
            case null -> new Cuboid6(min, min, min, max, max, max);
        };
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
    public PipeRenderContext createRenderContext(@org.jspecify.annotations.Nullable BlockPos pos, @org.jspecify.annotations.Nullable LightMatrix lightMatrix, int connections, int blockedConnections, float thickness) {
        return new RenderContext(pos, lightMatrix, connections, blockedConnections, thickness);
    }

    private static final class RenderContext extends PipeRenderContext {

        @Accessors(fluent = true)
        @Getter @Setter
        private int flangeVisibility;

        public RenderContext(@Nullable BlockPos pos, @Nullable LightMatrix lightMatrix, int connections, int blockedConnections, float thickness) {
            super(pos, lightMatrix, connections, blockedConnections, thickness);
        }
    }
}
