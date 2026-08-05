package supersymmetry.common.metatileentities.multi.electric;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.TextComponentUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockCrucible;
import supersymmetry.common.blocks.BlockInductionCoilAssembly;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntityInductionFurnace extends RecipeMapMultiblockController implements IProgressBarMultiblock {

    private static final int WATER_AMOUNT = 100;
    private static final int DANGEROUS_HEAT = 500;
    private static final int MELTING_HEAT = 1000;
    private static final int HEAT_UP_RATE = 20;
    private static final int COOLED_HEAT_UP_RATE = 5;
    private static final int BASE_COOL_DOWN_RATE = 5;

    private int heat = 25;
    private int activeTicks;

    public MetaTileEntityInductionFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.INDUCTION_FURNACE);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityInductionFurnace(this.metaTileEntityId);
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote || getOffsetTimer() % 20 != 0) {
            return;
        }

        if (!isStructureFormed()) {
            heat = Math.max(25, heat - BASE_COOL_DOWN_RATE);
            activeTicks = 0;
            return;
        }

        if (activeTicks > 0 || heat > 25) {
            updateCooling(activeTicks);
            activeTicks = 0;
        }
    }

    @Override
    protected void updateFormedValid() {
        super.updateFormedValid();
        if (!getWorld().isRemote && recipeMapWorkable.isActive() && recipeMapWorkable.isWorkingEnabled()) {
            activeTicks++;
        }
    }

    private void updateCooling(int ticksRunning) {
        boolean isRunning = ticksRunning > 0;
        double operationFraction = isRunning ? ticksRunning / 20.0 : 1.0;
        int waterRequired = (int) Math.ceil(WATER_AMOUNT * operationFraction);

        FluidStack desiredWater = Materials.Water.getFluid(waterRequired);
        FluidStack simulatedDrain = inputFluidInventory.drain(desiredWater, false);

        int waterToConsume = simulatedDrain == null ? 0 : simulatedDrain.amount;
        int[] waterAmount = getWaterAmount();

        if (waterToConsume > 0 && heat >= DANGEROUS_HEAT) {
            explodeMultiblock(2.0F + heat / 1000.0F + 4.0F * waterAmount[0] / 10000.0F);
            return;
        }

        if (heat >= MELTING_HEAT) {
            explodeMultiblock(4);
            return;
        }

        if (waterToConsume > 0) {
            FluidStack actualWater = Materials.Water.getFluid(waterToConsume);
            FluidStack actualHeatedWater = Materials.Steam.getFluid(waterToConsume);

            boolean hasOutputSpace = outputFluidInventory.fill(actualHeatedWater, false) >= waterToConsume;

            if (hasOutputSpace) {
                inputFluidInventory.drain(actualWater, true);
                outputFluidInventory.fill(actualHeatedWater, true);

                double waterFraction = waterToConsume / (double) waterRequired;

                if (isRunning) {
                    int activeCooling = (int) Math.round(BASE_COOL_DOWN_RATE * 2 * operationFraction * waterFraction);
                    int operatingHeat = (int) Math.round(COOLED_HEAT_UP_RATE * operationFraction);

                    if (heat <= 75) {
                        heat = Math.min(1000, heat + operatingHeat * 3 - activeCooling);
                    } else {
                        heat = heat + operatingHeat - activeCooling;
                    }
                } else {
                    int activeCooling = (int) Math.round(BASE_COOL_DOWN_RATE * 2 * waterFraction);
                    heat = Math.max(25, heat - BASE_COOL_DOWN_RATE - activeCooling);
                }
                return;
            }
        }

        if (isRunning) {
            heat = Math.min(1000, heat + (int) Math.ceil(HEAT_UP_RATE * operationFraction));
        } else {
            heat = Math.max(25, heat - BASE_COOL_DOWN_RATE);
        }
    }

    @Override
    public void explodeMultiblock(float power) {
        doExplosion(power);
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        super.addWarningText(textList);
        if (isStructureFormed()) {
            double heatPercentage = heat / (double) MELTING_HEAT;
            int[] waterAmount = getWaterAmount();

            if (heatPercentage >= 0.5) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.RED,
                        "susy.multiblock.cooling.dangerous_heat"));
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                        "susy.multiblock.cooling.explosion_risk"));
            } else if (waterAmount[0] == 0) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW,
                        "gregtech.multiblock.large_boiler.no_water"));
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                        "susy.multiblock.cooling.heating_up"));
            }
        }
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" AAA ", " AAA ", " AAA ")
                .aisle("AAAAA", "ACCCA", "AAAAA")
                .aisle("AAAAA", "ACUCA", "AAAAA")
                .aisle("AAAAA", "ACCCA", "AAAAA")
                .aisle(" AAA ", " ASA ", " AAA ")
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setExactLimit(1))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH).setExactLimit(1))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(2))
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.EXPORT_ITEMS)))
                .where('C', states(SuSyBlocks.INDUCTION_COIL_ASSEMBLY
                        .getState(BlockInductionCoilAssembly.InductionCoilAssemblyType.COPPER)))
                .where('U', states(SuSyBlocks.CRUCIBLE
                        .getState(BlockCrucible.CrucibleType.SILICON_CARBIDE)))
                .where(' ', any())
                .where('#', air())
                .build();
    }

    @Override
    public void addInformation(
                               ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(TooltipHelper.BLINKING_RED + I18n.format("susy.machine.induction_furnace.explosion_tooltip"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.PYROLYSE_OVEN_OVERLAY;
    }

    @Override
    public int getNumProgressBars() {
        return 2;
    }

    @Override
    public double getFillPercentage(int index) {
        if (index == 0) {
            int[] waterAmount = getWaterAmount();
            if (waterAmount[1] == 0) return 0;
            return (1.0 * waterAmount[0]) / waterAmount[1];
        } else if (index == 1) {
            return (1.0 * heat / (double) MELTING_HEAT);
        } else {
            return 0;
        }
    }

    @Override
    public TextureArea getProgressBarTexture(int index) {
        if (index == 0) {
            return GuiTextures.PROGRESS_BAR_FLUID_RIG_DEPLETION;

        } else if (index == 1) {
            return SusyGuiTextures.PROGRESS_BAR_HEAT;
        } else {
            return null;
        }
    }

    @Override
    public void addBarHoverText(List<ITextComponent> hoverList, int index) {
        if (index == 0) {
            int[] waterAmount = getWaterAmount();
            ITextComponent waterInfo = TextComponentUtil.translationWithColor(
                    TextFormatting.BLUE,
                    "%s / %s L",
                    waterAmount[0], waterAmount[1]);
            hoverList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "gregtech.multiblock.large_boiler.water_bar_hover",
                    waterInfo));

        } else if (index == 1) {

            ITextComponent heatInfo = TextComponentUtil.translationWithColor(
                    TextFormatting.RED,
                    "%s",
                    heat);

            hoverList.add(TextComponentUtil.translationWithColor(
                    TextFormatting.GRAY,
                    "susy.multiblock.cooling.heat_bar_hover",
                    heatInfo));

        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("CoilHeat", heat);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        heat = data.getInteger("CoilHeat");
        super.readFromNBT(data);
    }

    /**
     * Returns an int[] of {AmountFilled, Capacity} where capacity is the sum of hatches with some water in them.
     * If there is no water in the boiler (or the structure isn't formed, both of these values will be zero.
     */
    private int[] getWaterAmount() {
        if (!isStructureFormed()) return new int[] { 0, 0 };
        List<IFluidTank> tanks = getAbilities(MultiblockAbility.IMPORT_FLUIDS);
        int filled = 0, capacity = 0;
        for (IFluidTank tank : tanks) {
            if (tank == null || tank.getFluid() == null) continue;
            if (CommonFluidFilters.BOILER_FLUID.test(tank.getFluid())) {
                filled += tank.getFluidAmount();
                capacity += tank.getCapacity();
            }
        }
        return new int[] { filled, capacity };
    }
}
