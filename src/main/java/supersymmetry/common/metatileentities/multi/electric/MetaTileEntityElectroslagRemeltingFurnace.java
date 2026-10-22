package supersymmetry.common.metatileentities.multi.electric;

import java.util.*;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.pattern.*;
import gregtech.api.recipes.Recipe;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.capability.SuSyDataCodes;
import supersymmetry.api.capability.impl.WatercooledMultiRecipeLogic;
import supersymmetry.api.capability.impl.WatercooledMultiRecipeLogic.IWatercooledMultiblock;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.properties.InductionCrucibleMaterialProperty;
import supersymmetry.common.blocks.*;
import supersymmetry.common.materials.SusyMaterials;

public class MetaTileEntityElectroslagRemeltingFurnace extends RecipeMapMultiblockController
                                                       implements IProgressBarMultiblock, IWatercooledMultiblock {

    private final String material = BlockInductionCrucible.InductionCrucibleType.COPPER.material;
    private static final int WATER_AMOUNT = 100;
    private static final int DANGEROUS_HEAT = 500;
    private static final int MELTING_HEAT = 1000;
    private static final int HEAT_UP_RATE = 20;
    private static final int COOLED_HEAT_UP_RATE = 5;
    private static final int BASE_COOL_DOWN_RATE = 5;

    private int heat = 25;
    private int activeTicks;
    private FluidStack pendingCoolant;
    private boolean coolingBlocked;

    public MetaTileEntityElectroslagRemeltingFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.ESR);
        this.recipeMapWorkable = new WatercooledMultiRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityElectroslagRemeltingFurnace(this.metaTileEntityId);
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

        if (activeTicks > 0 || heat > 25 || pendingCoolant != null) {
            updateCooling(activeTicks);
            activeTicks = 0;
            markDirty();
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
            explodeMultiblock(heat / 1000.0F + waterAmount[0] / 10000.0F);
            return;
        }

        if (heat >= MELTING_HEAT) {
            explodeMultiblock(4);
            return;
        }

        if (pendingCoolant != null) {
            if (((WatercooledMultiRecipeLogic) recipeMapWorkable).canOutputCoolant(pendingCoolant)) {
                int accepted = outputFluidInventory.fill(pendingCoolant.copy(), true);
                pendingCoolant.amount -= accepted;
                if (pendingCoolant.amount <= 0) pendingCoolant = null;
            }
            if (pendingCoolant != null) {
                setCoolingBlocked(true);
                updateUncooledHeat(isRunning, operationFraction);
                return;
            }
            if (!isRunning && heat == 25) {
                setCoolingBlocked(false);
                return;
            }
        }

        if (waterToConsume > 0) {
            FluidStack actualWater = Materials.Water.getFluid(waterToConsume);
            FluidStack actualHeatedWater = SusyMaterials.HotSoftenedWater.getFluid(waterToConsume);

            boolean hasOutputSpace = ((WatercooledMultiRecipeLogic) recipeMapWorkable)
                    .canOutputCoolant(actualHeatedWater);

            setCoolingBlocked(!hasOutputSpace);

            if (hasOutputSpace) {
                FluidStack drained = inputFluidInventory.drain(actualWater, true);
                if (drained == null || drained.amount == 0) {
                    updateUncooledHeat(isRunning, operationFraction);
                    return;
                }
                actualHeatedWater.amount = drained.amount;
                int accepted = outputFluidInventory.fill(actualHeatedWater.copy(), true);
                if (accepted < drained.amount) {
                    pendingCoolant = actualHeatedWater.copy();
                    pendingCoolant.amount -= accepted;
                    setCoolingBlocked(true);
                }
                markDirty();

                double waterFraction = accepted / (double) waterRequired;

                if (accepted == 0) {
                    updateUncooledHeat(isRunning, operationFraction);
                    return;
                }

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

        if (waterToConsume == 0) setCoolingBlocked(false);
        updateUncooledHeat(isRunning, operationFraction);
    }

    private void updateUncooledHeat(boolean isRunning, double operationFraction) {
        if (isRunning) {
            heat = Math.min(1000, heat + (int) Math.ceil(HEAT_UP_RATE * operationFraction));
        } else {
            heat = Math.max(25, heat - BASE_COOL_DOWN_RATE);
        }
    }

    public boolean hasPendingCoolant() {
        return pendingCoolant != null;
    }

    private void setCoolingBlocked(boolean blocked) {
        if (coolingBlocked != blocked) {
            coolingBlocked = blocked;
            writeCustomData(SuSyDataCodes.UPDATE_FURNACE_COOLING, buf -> buf.writeBoolean(blocked));
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
            if (((WatercooledMultiRecipeLogic) recipeMapWorkable).isWaitingForOutputs()) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW,
                        "susy.multiblock.induction_furnace.waiting_outputs"));
            }
            if (coolingBlocked) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW,
                        "susy.multiblock.induction_furnace.cooling_blocked"));
            }
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

    @NotNull protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("HPPPB", " PPP ", "  C  ", "  C  ", "     ", "     ", "     ")
                .aisle("PUPUP", "PUPUP", "  #  ", "  C  ", "  C  ", "  C  ", "  C  ")
                .aisle("hPPPb", " PPP ", "  C  ", "  C  ", "     ", "  W  ", "  W  ")
                .aisle("     ", "     ", "     ", "     ", "     ", "  W  ", "     ")
                .aisle(" F F ", " F F ", " F F ", " F F ", " C C ", " CMC ", " CSC ")
                .where('S', selfPredicate())
                .where('C', states(getCasingState()))
                .where('U', states(getCrucibleState()))
                .where('F', states(getFrameState())
                        .or(autoAbilities(true, false, false, false, false, false, false)))
                .where('P', states(getPipeState()))
                .where('W', states(getWiringState()))
                .where('M', autoAbilities(false, true, false, false, false, false, false))
                .where('B', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN))
                        .or(autoAbilities(false, false, true, false, false, false, false)))
                .where('b', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN))
                        .or(autoAbilities(false, false, false, true, false, false, false)))
                .where('H', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN))
                        .or(autoAbilities(false, false, false, false, true, false, false)))
                .where('h', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN))
                        .or(autoAbilities(false, false, false, false, false, true, false)))
                .where(' ', any())
                .where('#', air())
                .build();
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    protected static IBlockState getCrucibleState() {
        return SuSyBlocks.INDUCTION_CRUCIBLE.getState(BlockInductionCrucible.InductionCrucibleType.COPPER);
    }

    protected static IBlockState getFrameState() {
        return MetaBlocks.FRAMES.get(Materials.StainlessSteel).getBlock(Materials.StainlessSteel);
    }

    protected static IBlockState getPipeState() {
        return SuSyBlocks.MULTIBLOCK_CASING_2.getState(BlockSuSyMultiblockCasing2.CasingType.STAINLESS_STEEL_PIPE);
    }

    protected static IBlockState getVacuumPumpState() {
        return SuSyBlocks.VACUUM_PUMP.getState(BlockVacuumPump.VacuumPumpType.STAINLESS_STEEL);
    }

    protected static IBlockState getWiringState() {
        return SuSyBlocks.WIRING.getState(BlockWiring.WiringType.COPPER);
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe, boolean consumeIfSuccess) {
        return this.material.equals(recipe.getProperty(InductionCrucibleMaterialProperty.getInstance(), ""));
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
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @SideOnly(Side.CLIENT)
    @NonNull @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.PYROLYSE_OVEN_OVERLAY;
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
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
        if (pendingCoolant != null)
            data.setTag("FurnacePendingCoolant", pendingCoolant.writeToNBT(new NBTTagCompound()));
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        heat = data.getInteger("CoilHeat");
        pendingCoolant = FluidStack.loadFluidStackFromNBT(data.getCompoundTag("FurnacePendingCoolant"));
        if (pendingCoolant != null && pendingCoolant.amount <= 0) pendingCoolant = null;
        super.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(coolingBlocked);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        coolingBlocked = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == SuSyDataCodes.UPDATE_FURNACE_COOLING) coolingBlocked = buf.readBoolean();
        else super.receiveCustomData(dataId, buf);
    }

    @NotNull @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = super.getDataInfo();
        list.add(new TextComponentTranslation("susy.multiblock.induction_furnace.crucible_material",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(material))
                        .setStyle(new Style().setColor(TextFormatting.BLUE))));
        return list;
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
