package supersymmetry.common.metatileentities.multi.electric;

import static supersymmetry.api.blocks.VariantHorizontalRotatableBlock.FACING;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.metatileentity.multiblock.IProgressBarMultiblock;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import supersymmetry.common.blocks.BlockAlternatorCoil;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntitySUSYLargeTurbine extends RotationGeneratorController implements ITieredMetaTileEntity, IProgressBarMultiblock {

    public final int tier;

    public final IBlockState casingState;
    public final IBlockState rotorState;
    public final ICubeRenderer casingRenderer;
    public final ICubeRenderer frontOverlay;

    public MetaTileEntitySUSYLargeTurbine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier, int maxSpeed, int accel, int decel, IBlockState casingState, IBlockState rotorState, ICubeRenderer casingRenderer, ICubeRenderer frontOverlay) {
        super(metaTileEntityId, recipeMap, tier, maxSpeed, accel, decel);
        this.casingState = casingState;
        this.rotorState = rotorState;
        this.casingRenderer = casingRenderer;
        this.frontOverlay = frontOverlay;
        this.tier = tier;
        this.recipeMapWorkable = new SuSyTurbineRecipeLogic(this);
        this.recipeMapWorkable.setMaximumOverclockVoltage(GTValues.V[tier]);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySUSYLargeTurbine(metaTileEntityId, recipeMap, tier, maxSpeed, accel, decel, casingState, rotorState, casingRenderer, frontOverlay);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        // Different characters use common constraints. Copied from GCyM
        TraceabilityPredicate casingPredicate = states(this.casingState).setMinGlobalLimited(52)
                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));
        TraceabilityPredicate maintenance = abilities(MultiblockAbility.MAINTENANCE_HATCH).setMaxGlobalLimited(1);

        return FactoryBlockPattern.start()
                .aisle("GAAAAAAAO", "GAAAAAAAO", "G   A   O")
                .aisle("GAAAAAAAO", "GDDDDCCCF", "GAAAAAAAO")
                .aisle("GAAAAAAAO", "GSAAAAAAO", "G   A   O")
                .where('S', selfPredicate())
                .where('A', casingPredicate
                        .or(autoAbilities(false, false, false, false, false, false, false))
                        .or(maintenance))
                .where('O', casingPredicate
                        .or(autoAbilities(false, false, false, false, false, true, false))
                        .or(maintenance))
                .where('C', coilOrientation())
                .where('D', rotorOrientation())
                .where('F', abilities(MultiblockAbility.OUTPUT_ENERGY))
                .where('G', casingPredicate
                        .or(autoAbilities(false, false, false, false, true, false, false))
                        .or(maintenance))
                .where(' ', any())
                .build();
    }

    protected TraceabilityPredicate rotorOrientation() {
        // makes sure rotor's front faces the left side (relative to the player) of controller front
        EnumFacing leftFacing = RelativeDirection.RIGHT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                isFlipped());

        // converting the left facing to positive x or z axis direction
        // this is needed for the following update which converts this rotatable block from horizontal directional into
        // axial directional.
        EnumFacing axialFacing = leftFacing.getIndex() < 4 ? EnumFacing.SOUTH : EnumFacing.WEST;

        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[] {
                new BlockInfo(this.rotorState.withProperty(FACING, axialFacing)) };
        return new TraceabilityPredicate(blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            if (state.getBlock() != this.rotorState.getBlock()) return false;

            // auto-correct rotor orientation
            if (state != this.rotorState.withProperty(FACING, axialFacing))
                getWorld().setBlockState(blockWorldState.getPos(), this.rotorState.withProperty(FACING, axialFacing));

            return true;
        }, supplier);
    }

    protected TraceabilityPredicate coilOrientation() {
        // makes sure rotor's front faces the left side (relative to the player) of controller front
        EnumFacing leftFacing = RelativeDirection.RIGHT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                isFlipped());

        // converting the left facing to positive x or z axis direction
        // this is needed for the following update which converts this rotatable block from horizontal directional into
        // axial directional.
        EnumFacing axialFacing = leftFacing.getIndex() < 4 ? EnumFacing.SOUTH : EnumFacing.WEST;

        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[] {
                new BlockInfo(copperCoilState().withProperty(FACING, axialFacing)) };
        return new TraceabilityPredicate(blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            if (!(state.getBlock() instanceof BlockAlternatorCoil)) return false;

            // auto-correct rotor orientation
            if (state != copperCoilState().withProperty(FACING, axialFacing)) {
                getWorld().setBlockState(blockWorldState.getPos(), copperCoilState().withProperty(FACING, axialFacing));
            }
            return true;
        }, supplier);
    }

    protected IBlockState copperCoilState() {
        return SuSyBlocks.ALTERNATOR_COIL.getState(BlockAlternatorCoil.AlternatorCoilType.COPPER);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return casingRenderer;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return frontOverlay;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return false;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public boolean canVoidRecipeItemOutputs() {
        return true;
    }

    @Override
    public boolean canVoidRecipeFluidOutputs() {
        return true;
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    // GUI stuff

    @Override
    public int getNumProgressBars() { return 3; }

    @Override
    public double getFillPercentage(int index) {
        if (index == 0) {
            int[] fuelAmount = new int[2];
            if (getInputFluidInventory() != null) {
                SuSyTurbineRecipeLogic recipeLogic = (SuSyTurbineRecipeLogic) recipeMapWorkable;
                if (recipeLogic.getInputFluidStack() != null) {
                    FluidStack testStack = recipeLogic.getInputFluidStack().copy();
                    testStack.amount = Integer.MAX_VALUE;
                    fuelAmount = getTotalFluidAmount(testStack, getInputFluidInventory());
                }
            }
            return fuelAmount[1] != 0 ? 1.0 * fuelAmount[0] / fuelAmount[1] : 0;
        } else if (index == 1) {
            int[] lubricantAmount = new int[2];
            if (lubricantStack != null) {
                FluidStack testStack = lubricantStack.copy();
                testStack.amount = Integer.MAX_VALUE;
                lubricantAmount = getTotalFluidAmount(testStack, getInputFluidInventory());
            }

            return lubricantAmount[1] != 0 ? 1.0 * lubricantAmount[0] / lubricantAmount[1] : 0;
        } else {
            return 1.0 * getRotationSpeed() / maxSpeed;
        }
    }

    @Override
    public TextureArea getProgressBarTexture(int index) {
        if (index == 0) {
            return GuiTextures.PROGRESS_BAR_LCE_FUEL;
        } else if (index == 1) {
            return GuiTextures.PROGRESS_BAR_LCE_LUBRICANT;
        } else {
            return GuiTextures.PROGRESS_BAR_TURBINE_ROTOR_SPEED;
        }
    }

    @Override
    public void addBarHoverText(List<ITextComponent> hoverList, int index) {
        if (index == 0) {
            addFuelText(hoverList);
        } else if (index == 1) {
            int lubricantStored = 0;
            int lubricantCapacity = 0;
            double lubricantConsumptionRate = 0;

            if (isStructureFormed() && lubricantStack != null) {
                int[] lubricantAmount;
                FluidStack testStack = lubricantStack.copy();
                testStack.amount = Integer.MAX_VALUE;
                lubricantAmount = getTotalFluidAmount(testStack, getInputFluidInventory());
                lubricantStored = lubricantAmount[0];
                lubricantCapacity = lubricantAmount[1];
                lubricantConsumptionRate = lubricantInfo.amount_required * (2.0 * getRotationSpeed() / getMaxRotationSpeed());
            }

            ITextComponent lubricantStorage = TextComponentUtil.stringWithColor(
                    TextFormatting.GOLD,
                    TextFormattingUtil.formatNumbers(lubricantStored) + " / " +
                            TextFormattingUtil.formatNumbers(lubricantCapacity) + " L");

            ITextComponent lubricantConsumption = TextComponentUtil.stringWithColor(
                    TextFormatting.GOLD,
                    TextFormattingUtil.formatNumbers(lubricantConsumptionRate) + " L/min ");

            hoverList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "susy.multiblock.rotation_generator.lubricant_amount",
                    lubricantStorage, lubricantConsumption));
        } else {
            ITextComponent rpmTranslated = TextComponentUtil.translationWithColor(
                    getRotorSpeedColor(getRotationSpeed(), getMaxRotationSpeed()),
                    "gregtech.multiblock.turbine.rotor_rpm_unit_name");
            ITextComponent rotorInfo = TextComponentUtil.translationWithColor(
                    getRotorSpeedColor(getRotationSpeed(), getMaxRotationSpeed()),
                    "%s / %s %s",
                    TextFormattingUtil.formatNumbers(getRotationSpeed()),
                    TextFormattingUtil.formatNumbers(getMaxRotationSpeed()),
                    rpmTranslated);
            hoverList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "gregtech.multiblock.turbine.rotor_speed",
                    rotorInfo));
        }
    }

    private TextFormatting getRotorSpeedColor(int rotorSpeed, int maxRotorSpeed) {
        double speedRatio = 1.0 * rotorSpeed / maxRotorSpeed;
        if (speedRatio < 0.4) {
            return TextFormatting.RED;
        } else if (speedRatio < 0.8) {
            return TextFormatting.YELLOW;
        } else {
            return TextFormatting.GREEN;
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (isStructureFormed()) {
            FluidStack fuelStack = ((SuSyTurbineRecipeLogic) recipeMapWorkable).getInputFluidStack();
            if (fuelStack != null && fuelStack.amount > 0) {
                ITextComponent fuelName = GTUtility.getFluidTranslation(fuelStack.getFluid());
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "susy.multiblock.rotation_generator.fuel_name", fuelName));
                if (lubricantStack != null && lubricantStack.amount > 0) {
                    ITextComponent lubricantName = GTUtility.getFluidTranslation((lubricantStack.getFluid()));
                    textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "susy.multiblock.rotation_generator.lubricant_name", lubricantName));
                }
            }
            textList.add(new TextComponentTranslation("susy.multiblock.rotation_generator.power", getMaxVoltage(), recipeMapWorkable.getEnergyContainer().getOutputVoltage()));
        }

        MultiblockFuelRecipeLogic recipeLogic = (MultiblockFuelRecipeLogic) recipeMapWorkable;

        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addFuelNeededLine(recipeLogic.getRecipeFluidInputInfo(), recipeLogic.getPreviousRecipeDuration())
                .addWorkingStatusLine();
    }
}
