package supersymmetry.common.metatileentities.multi.electric.strand;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.FluidUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.blocks.VariantAxialRotatableBlock;
import supersymmetry.api.capability.Strand;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.*;

import java.util.function.Supplier;

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
                .where('B', rollOrientation())
                .where('A', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('I', abilities(SuSyMultiblockAbilities.STRAND_IMPORT))
                .where('O', abilities(SuSyMultiblockAbilities.STRAND_EXPORT))
                .where('S', selfPredicate())
                .where('F', states(SuSyMetaBlocks.SHEETED_FRAMES.get(Materials.Steel).getBlock(Materials.Steel)
                        .withProperty(BlockSheetedFrame.SHEETED_FRAME_AXIS, BlockSheetedFrame.FrameEnumAxis
                                .fromFacingAxis(getRelativeFacing(RelativeDirection.UP).getAxis())))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH, MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(3)))
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

    protected TraceabilityPredicate rollOrientation() {
        //makes sure rotor's front faces the left side (relative to the player) of controller front
        EnumFacing.Axis axialFacing = getRelativeFacing(RelativeDirection.RIGHT).getAxis();

        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[]{new BlockInfo(rollState().withProperty(BlockMetallurgyRoll.AXIS, axialFacing))};
        return new TraceabilityPredicate(blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            if (!(state.getBlock() instanceof BlockMetallurgyRoll)) return false;

            // auto-correct rotor orientation
            if (state != rollState().withProperty(BlockMetallurgyRoll.AXIS, axialFacing)) {
                getWorld().setBlockState(blockWorldState.getPos(), rollState().withProperty(BlockMetallurgyRoll.AXIS, axialFacing));
            }
            return true;
        }, supplier);
    }

    private IBlockState rollState() {
        return SuSyBlocks.METALLURGY_ROLL.getState(BlockMetallurgyRoll.BlockMetallurgyRollType.ROLL);
    }
}
