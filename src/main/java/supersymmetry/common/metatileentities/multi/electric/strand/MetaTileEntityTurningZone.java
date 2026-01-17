package supersymmetry.common.metatileentities.multi.electric.strand;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.capability.Strand;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.*;

public class MetaTileEntityTurningZone extends MetaTileEntityStrandShaper {

    public MetaTileEntityTurningZone(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected boolean consumeInputsAndSetupRecipe() {
        if (this.input.getStrand() == null) {
            return false;
        }
        this.input.take();
        this.maxProgress = 20;
        return true;
    }

    @Override
    protected Strand resultingStrand() {
        return input.getStrand();
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RelativeDirection.RIGHT, RelativeDirection.BACK, RelativeDirection.UP)
                .aisle("ABBBA",
                        "ABBBA",
                        "ABBBA",
                        "ABBBA",
                        "     ",
                        "     ",
                        "     ",
                        "     ",
                        "F F F")
                .aisle("  O  ",
                        "     ",
                        "     ",
                        "     ",
                        "ABBBA",
                        "ABBBA",
                        "     ",
                        "     ",
                        "F S F")
                .aisle("ABBBA",
                        "ABBBA",
                        "ABBBA",
                        "     ",
                        "     ",
                        "     ",
                        "ABBBA",
                        "     ",
                        "F F F")
                .aisle("     ",
                        "     ",
                        "     ",
                        "ABBBA",
                        "ABBBA",
                        "     ",
                        "     ",
                        "ABBBA",
                        "F F F")
                .aisle("     ",
                        "     ",
                        "     ",
                        "     ",
                        "     ",
                        "ABBBA",
                        "     ",
                        "ABBBA",
                        "F F F")
                .aisle("     ",
                        "     ",
                        "     ",
                        "     ",
                        "     ",
                        "ABBBA",
                        "     ",
                        "     ",
                        "ABBBA")
                .aisle("     ",
                        "     ",
                        "     ",
                        "     ",
                        "     ",
                        "     ",
                        "ABBBA",
                        "     ",
                        "ABBBA")
                .aisle("     ",
                        "     ",
                        "     ",
                        "     ",
                        "     ",
                        "     ",
                        "ABBBA",
                        "  I  ",
                        "ABBBA")
                .where('B', rollOrientation(RelativeDirection.RIGHT))
                .where('A',
                        states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('I', abilities(SuSyMultiblockAbilities.STRAND_IMPORT))
                .where('O', abilities(SuSyMultiblockAbilities.STRAND_EXPORT))
                .where('S', selfPredicate())
                .where('F', states(SuSyMetaBlocks.SHEETED_FRAMES.get(Materials.Steel).getBlock(Materials.Steel)
                        .withProperty(BlockSheetedFrame.SHEETED_FRAME_AXIS, BlockSheetedFrame.FrameEnumAxis
                                .fromFacingAxis(getRelativeFacing(RelativeDirection.UP).getAxis())))
                                        .or(autoAbilities(true, true, false)))
                .where(' ', any())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.TURNING_ZONE_OVERLAY;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityTurningZone(this.metaTileEntityId);
    }

    private IBlockState rollState() {
        return SuSyBlocks.METALLURGY_ROLL.getState(BlockMetallurgyRoll.BlockMetallurgyRollType.ROLL);
    }
}
