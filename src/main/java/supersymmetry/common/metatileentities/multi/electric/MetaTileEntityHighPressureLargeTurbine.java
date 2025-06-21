package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.impl.SuSyTurbineRecipeLogic;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockAlternatorCoil;
import supersymmetry.common.blocks.BlockTurbineRotor;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

import static supersymmetry.api.blocks.VariantHorizontalRotatableBlock.FACING;

public class MetaTileEntityHighPressureLargeTurbine extends MetaTileEntitySUSYLargeTurbine {

    public MetaTileEntityHighPressureLargeTurbine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.HIGH_PRESSURE_ADVANCED_STEAM_TURBINE, 4,
                MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.TITANIUM_TURBINE_CASING),
                SuSyBlocks.TURBINE_ROTOR.getState(BlockTurbineRotor.BlockTurbineRotorType.LOW_PRESSURE),
                Textures.STABLE_TITANIUM_CASING, SusyTextures.HIGH_PRESSURE_ADVANCED_STEAM_TURBINE_OVERLAY);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityHighPressureLargeTurbine(metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        // Different characters use common constraints. Copied from GCyM
        TraceabilityPredicate casingPredicate = states(this.casingState).setMinGlobalLimited(52)
                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));
        TraceabilityPredicate maintenance = abilities(MultiblockAbility.MAINTENANCE_HATCH).setMaxGlobalLimited(1);

        return FactoryBlockPattern.start()
                .aisle("GAAAAAAAAAAAO", "GAAAAAAAAAAAO", "G    AA     O")
                .aisle("GAAAAAAAAAAAO", "GHHHALLLLCCCF", "GAAAAAAAAAAAO")
                .aisle("GAAAAAAAAAAAO", "GSAAAAAAAAAAO", "G    AA     O")
                .where('S', selfPredicate())
                .where('A', casingPredicate
                       .or(autoAbilities(false, false, false, false, false, false, false))
                       .or(maintenance))
                .where('O', casingPredicate
                        .or(autoAbilities(false, false, false, false, false, true, false))
                        .or(maintenance))
                .where('C', coilOrientation())
                .where('L', rotorOrientation())
                .where('H', rotorOrientation2())
                .where('F', abilities(MultiblockAbility.OUTPUT_ENERGY))
                .where('G', casingPredicate
                        .or(autoAbilities(false, false, false, false, true, false, false))
                        .or(maintenance))
                .where(' ', any())
                .build();
    }

    protected TraceabilityPredicate rotorOrientation2() {
        //makes sure rotor's front faces the left side (relative to the player) of controller front
        EnumFacing leftFacing = RelativeDirection.RIGHT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped());

        // converting the left facing to positive x or z axis direction
        // this is needed for the following update which converts this rotatable block from horizontal directional into axial directional.
        EnumFacing axialFacing = leftFacing.getIndex() < 4 ? EnumFacing.SOUTH : EnumFacing.WEST;

        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[]{new BlockInfo(this.rotorState2().withProperty(FACING, axialFacing))};
        return new TraceabilityPredicate(blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            if (state.getBlock() != this.rotorState2().getBlock()) return false;

            // auto-correct rotor orientation
            if (state != this.rotorState2().withProperty(FACING, axialFacing))
                getWorld().setBlockState(blockWorldState.getPos(), this.rotorState2().withProperty(FACING, axialFacing));

            return true;
        }, supplier);
    }

    public IBlockState rotorState2() {
        return SuSyBlocks.TURBINE_ROTOR.getState(BlockTurbineRotor.BlockTurbineRotorType.HIGH_PRESSURE);
    }

}
