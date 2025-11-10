package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.IProgressBarMultiblock;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.*;
import gregtech.client.renderer.ICubeRenderer;

import gregtech.common.blocks.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.common.blocks.*;

import javax.annotation.Nonnull;
import java.util.List;

import static supersymmetry.api.blocks.VariantHorizontalRotatableBlock.FACING;

public class MetaTileEntityInternalCombustionEngine extends RotationGeneratorController implements IProgressBarMultiblock {

    public final int tier;

    public final ICubeRenderer casingRenderer;
    public final ICubeRenderer frontOverlay;

    public MetaTileEntityInternalCombustionEngine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int maxSpeed, int accel, int decel, int tier, ICubeRenderer casingRenderer, ICubeRenderer frontOverlay) {
        super(metaTileEntityId, recipeMap, tier, maxSpeed, accel, decel);
        this.casingRenderer = casingRenderer;
        this.frontOverlay = frontOverlay;
        this.tier = tier;
        this.recipeMapWorkable = new SuSyTurbineRecipeLogic(this);
        this.recipeMapWorkable.setMaximumOverclockVoltage(GTValues.V[tier]);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityInternalCombustionEngine(metaTileEntityId, recipeMap, maxSpeed, accel, decel, tier, casingRenderer, frontOverlay);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        // Different characters use common constraints. Copied from GCyM
        TraceabilityPredicate casingPredicate = states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID));

        return FactoryBlockPattern.start()
                .aisle("C CCCCCCC  F   F ", "C   FEF    F   F ", "C   FCF    F   F ", "C                ", "C                ", "                 ")
                .aisle("CFFFFFFFFFFFFFFF ", "R CCCCCCC CCCCCCC", "R CCCCCCC CCCCCCC", "R CCCCCCC CCCCCCC", "C  F F F         ", "   HHHHH         ")
                .aisle("CPPPPPPPC  F   F ", "R CCCCCCC CCCCCCC", "R CXXXXXXGAAAAAAD", "R CBBBBBC CCCCCCC", "C  IPPPI         ", "   HHHHH         ")
                .aisle("CFFFFFFFFFFFFFFF ", "R CCCCCCC CCCCCCC", "R CCCCCCC CCCCCCC", "R CCCCCCC CCCCCCC", "C  F F F         ", "   HHHHH         ")
                .aisle("C CCCCCCC  F   F ", "C   FSF    F   F ", "C   FMF    F   F ", "C                ", "C                ", "                 ")
                .where('S', selfPredicate())
                .where('C', casingPredicate)
                .where('M', abilities(MultiblockAbility.MAINTENANCE_HATCH))
                .where('E', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where('F', frames(Materials.Steel))
                .where('H', casingPredicate
                        .or(autoAbilities(false, false, false, false, true, false, false)))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('R', states(SuSyBlocks.SERPENTINE.getState(BlockSerpentine.SerpentineType.BASIC)))
                .where('X', SuSyPredicates.horizontalOrientation(this, SuSyBlocks.ENGINE_CASING_2.getState(BlockEngineCasing2.EngineCasingType2.CRANKSHAFT),
                        RelativeDirection.UP, FACING))
                .where('G', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('A', coilOrientation())
                .where('D', abilities(MultiblockAbility.OUTPUT_ENERGY))
                .where('B', states(SuSyBlocks.ENGINE_CASING.getState(BlockEngineCasing.EngineCasingType.PISTON_BLOCK)))
                .where('I', states(SuSyBlocks.ACTIVE_CASING.getState(BlocksActiveCasing.ActiveBlockType.BASIC_INTAKE_CASING)))
                .where(' ', any())
                .build();
    }

    protected TraceabilityPredicate coilOrientation() {
        // makes sure rotor's front faces the left side (relative to the player) of controller front
        return SuSyPredicates.horizontalOrientation(this, copperCoilState(), RelativeDirection.RIGHT, FACING);
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
        return true;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.max_voltage_out", GTValues.V[tier + 2], GTValues.VNF[tier + 2]));
        tooltip.add(I18n.format("susy.multiblock.rotation_generator.tooltip", maxSpeed, accel, decel));
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
                lubricantConsumptionRate = lubricantInfo.amount_required * (2.0 * getRotationSpeed() / 3600);
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
    protected @NotNull Widget getFlexButton(int x, int y, int width, int height) {
        SuSyTurbineRecipeLogic logic = (SuSyTurbineRecipeLogic) this.recipeMapWorkable;

        return new ToggleButtonWidget(x, y, width, height, SusyGuiTextures.BUTTON_ENERGY_VOIDING, logic::getVoidingEnergy, logic::setVoidingEnergy)
                .setTooltipText("susy.gui.toggle_energy_voiding");
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
                    textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "susy.multiblock.rotation_generator.lubricant", lubricantName, lubricantInfo.boost));
                }
            }
            textList.add(new TextComponentTranslation("susy.multiblock.rotation_generator.power", getMaxVoltage(), Math.min(recipeMapWorkable.getEnergyContainer().getOutputVoltage(), GTValues.V[tier] * 16)));
        }

        MultiblockFuelRecipeLogic recipeLogic = (MultiblockFuelRecipeLogic) recipeMapWorkable;

        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .addFuelNeededLine(recipeLogic.getRecipeFluidInputInfo(), recipeLogic.getPreviousRecipeDuration())
                .addWorkingStatusLine();
    }
}
