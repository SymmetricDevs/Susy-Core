package supersymmetry.common.metatileentities.multi.electric.strand;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.Strand;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.common.blocks.*;

import java.util.List;
import java.util.function.Supplier;

import static supersymmetry.api.blocks.VariantDirectionalRotatableBlock.FACING;

public class MetaTileEntityRollingMill extends MetaTileEntityStrandShaper {
    public MetaTileEntityRollingMill(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public long getVoltage() {
        return 64;
    }

    @Override
    protected boolean consumeInputsAndSetupRecipe() {
        Strand orig = this.input.take();
        if (orig == null) return false;
        progress = (int) Math.ceil(1 / (4.0 * orig.thickness));
        return true;
    }

    @Override
    protected Strand resultingStrand() {
        if (this.input.getStrand() == null) return null;
        Strand str = new Strand(this.input.getStrand());
        // t / (2 - e^(-2t)) is a pretty good function for balancing
        double scaling = 2 - Math.pow(Math.E, -2 * this.progress);
        str.thickness /= scaling;
        str.width *= scaling;
        return str;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("   P   ", "   P   ", "CCCGCCC", "F  P  F", "   P   ", "   P   ", "   P   ")
                .aisle("   P   ", "   h   ", "RRRRRRR", "F  A  F", "   R   ", "   H   ", "   P   ")
                .aisle("   P   ", "   h   ", "RRRRRRR", "I  A  O", "   R   ", "   H   ", "   P   ")
                .aisle("   P   ", "   h   ", "RRRRRRR", "F  A  F", "   R   ", "   H   ", "   P   ")
                .aisle("   P   ", "   P   ", "CCCSCCC", "F  P  F", "   P   ", "   P   ", "   P   ")
                .where('R', rollOrientation())
                .where('H', hydraulicOrientation(RelativeDirection.UP))
                .where('h', hydraulicOrientation(RelativeDirection.DOWN))
                .where('F', frames(Materials.Steel))
                .where('S', selfPredicate())
                .where('I', abilities(SuSyMultiblockAbilities.STRAND_IMPORT))
                .where('O', abilities(SuSyMultiblockAbilities.STRAND_EXPORT))
                .where('C', autoAbilities().or(states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))))
                .where('G', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where(' ', any())
                .where('A', air())
                .build();
    }
    private IBlockState rollState() {
        return SuSyBlocks.METALLURGY_ROLL.getState(BlockMetallurgyRoll.BlockMetallurgyRollType.ROLL);
    }

    private IBlockState hydraulicState() {
        return SuSyBlocks.METALLURGY.getState(BlockMetallurgy.BlockMetallurgyType.HYDRAULIC_CYLINDER);
    }


    protected TraceabilityPredicate hydraulicOrientation(RelativeDirection direction) {
        EnumFacing facing = getRelativeFacing(direction);

        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[]{new BlockInfo(hydraulicState().withProperty(FACING, facing))};
        return new TraceabilityPredicate(blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            if (!(state.getBlock() instanceof BlockMetallurgy)) return false;

            // auto-correct rotor orientation
            if (state != hydraulicState().withProperty(FACING, facing)) {
                getWorld().setBlockState(blockWorldState.getPos(), hydraulicState().withProperty(FACING, facing));
            }
            return true;
        }, supplier);
    }

    protected TraceabilityPredicate rollOrientation() {
        //makes sure rotor's front faces the left side (relative to the player) of controller front
        EnumFacing.Axis axialFacing = getRelativeFacing(RelativeDirection.FRONT).getAxis();

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


    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }


    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityRollingMill(metaTileEntityId);
    }
}
