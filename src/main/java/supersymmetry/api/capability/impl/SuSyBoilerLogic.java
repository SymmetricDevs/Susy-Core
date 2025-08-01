package supersymmetry.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.IFilter;
import gregtech.api.capability.IMultiblockController;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.metatileentities.multi.steam.MetaTileEntitySuSyLargeBoiler;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static gregtech.api.capability.GregtechDataCodes.BOILER_HEAT;
import static gregtech.api.capability.GregtechDataCodes.BOILER_LAST_TICK_STEAM;
import static gregtech.api.capability.impl.CommonFluidFilters.matchesFluid;

public class SuSyBoilerLogic extends AbstractRecipeLogic {

    public static final IFilter<FluidStack> BOILER_FLUID = new IFilter<>() {

        @Override
        public boolean test(@NotNull FluidStack fluid) {
            FluidStack largeFluidStack = fluid.copy();
            largeFluidStack.amount = 1000;
            Recipe fluidFuelRecipe = RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.findRecipe(
                    GTValues.V[GTValues.MAX], NonNullList.create(), Collections.singletonList(largeFluidStack));
            return fluidFuelRecipe != null;
        }

        @Override
        public int getPriority() {
            return IFilter.whitelistLikePriority();
        }
    };

    private static final long STEAM_PER_WATER = 160;

    private int currentHeat;
    private int lastTickSteamOutput;
    private int excessWater, excessProjectedEU;

    public SuSyBoilerLogic(MetaTileEntitySuSyLargeBoiler tileEntity) {
        super(tileEntity, null);
        this.fluidOutputs = Collections.emptyList();
        this.itemOutputs = NonNullList.create();
    }

    @Override
    public void update() {
        if ((!isActive() || !canProgressRecipe() || !isWorkingEnabled()) && currentHeat > 0) {
            setHeat(currentHeat - 1);
            setLastTickSteam(0);
        }
        super.update();
    }

    @Override
    protected boolean canProgressRecipe() {
        return super.canProgressRecipe() && !(metaTileEntity instanceof IMultiblockController &&
                ((IMultiblockController) metaTileEntity).isStructureObstructed());
    }

    @Override
    protected void trySearchNewRecipe() {
        MetaTileEntitySuSyLargeBoiler boiler = getMetaTileEntity();
        if (ConfigHolder.machines.enableMaintenance && boiler.hasMaintenanceMechanics() &&
                boiler.getNumMaintenanceProblems() > 5) {
            return;
        }

        // can optimize with an override of checkPreviousRecipe() and a check here

        IMultipleTankHandler importFluids = boiler.getImportFluids();
        List<ItemStack> dummyList = NonNullList.create();

        boolean didStartRecipe = false;

        for (IFluidTank fluidTank : importFluids.getFluidTanks()) {
            FluidStack fuelStack = fluidTank.drain(Integer.MAX_VALUE, false);
            if (fuelStack == null || BOILER_FLUID.test(fuelStack)) continue;

            Recipe fluidFuelRecipe = RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.findRecipe(
                    GTValues.V[GTValues.MAX], dummyList, Collections.singletonList(fuelStack));
            // run only if it can apply a certain amount of "parallel", this is to mitigate int division
            if (fluidFuelRecipe != null) {
                int consumption = fluidFuelRecipe.getFluidInputs().get(0).getInputFluidStack().amount;
                fluidTank.drain(consumption, true);
                int fuelBurnTime = (fluidFuelRecipe.getDuration() * 96 / boiler.boilerType.steamPerTick());
                setMaxProgress(adjustBurnTimeForThrottle(
                        Math.max(1, fuelBurnTime)));
                didStartRecipe = true;
                break;
            }
        }

        if (!didStartRecipe) {
            IItemHandlerModifiable importItems = boiler.getImportItems();
            for (int i = 0; i < importItems.getSlots(); i++) {
                ItemStack stack = importItems.getStackInSlot(i);
                OrePrefix prefix = OreDictUnifier.getPrefix(stack);
                if (!isSupportedOrePrefix(prefix)) continue;

                Recipe solidFuelRecipe = SuSyRecipeMaps.BOILER_RECIPES.findRecipe(GTValues.V[GTValues.MAX],
                        Collections.singletonList(stack), NonNullList.create());
                if (solidFuelRecipe == null) continue;
                int fuelBurnTime = solidFuelRecipe.getDuration() * 96 / boiler.boilerType.steamPerTick();
                if (fuelBurnTime > 0) { // try to ensure this fuel can burn for at least 1 tick
                    setMaxProgress(adjustBurnTimeForThrottle(fuelBurnTime));
                    stack.shrink(1);
                    didStartRecipe = true;
                    break;
                }
            }
        }
        if (didStartRecipe) {
            this.progressTime = 1;
            this.recipeEUt = adjustEUtForThrottle(boiler.boilerType.steamPerTick());
            if (wasActiveAndNeedsUpdate) {
                wasActiveAndNeedsUpdate = false;
            } else {
                setActive(true);
            }
        }
        metaTileEntity.getNotifiedItemInputList().clear();
        metaTileEntity.getNotifiedFluidInputList().clear();
    }


    public static boolean isSupportedOrePrefix(OrePrefix prefix) {
        return SUPPORTED_ORE_PREFIXES.contains(prefix);
    }

    public static final Set<OrePrefix> SUPPORTED_ORE_PREFIXES = new ObjectArraySet<>();
    static {
        SUPPORTED_ORE_PREFIXES.add(OrePrefix.gem);
        SUPPORTED_ORE_PREFIXES.add(OrePrefix.gemChipped);
        SUPPORTED_ORE_PREFIXES.add(OrePrefix.gemFlawed);
        SUPPORTED_ORE_PREFIXES.add(OrePrefix.gemFlawless);
        SUPPORTED_ORE_PREFIXES.add(OrePrefix.gemExquisite);
        SUPPORTED_ORE_PREFIXES.add(OrePrefix.block);
        SUPPORTED_ORE_PREFIXES.add(OrePrefix.dust);
        SUPPORTED_ORE_PREFIXES.add(OrePrefix.dustSmall);
        SUPPORTED_ORE_PREFIXES.add(OrePrefix.dustTiny);
        SUPPORTED_ORE_PREFIXES.add(OrePrefix.plank);
        SUPPORTED_ORE_PREFIXES.add(OrePrefix.log);
        SUPPORTED_ORE_PREFIXES.add(OrePrefix.stick);
        SUPPORTED_ORE_PREFIXES.add(OrePrefix.stickLong);
    }

    @Override
    protected void updateRecipeProgress() {
        if (canRecipeProgress) {
            int generatedSteam = this.recipeEUt * getMaximumHeatFromMaintenance() / getMaximumHeat();
            if (generatedSteam > 0) {
                long amount = (generatedSteam + STEAM_PER_WATER) / STEAM_PER_WATER;
                excessWater += amount * STEAM_PER_WATER - generatedSteam;
                amount -= excessWater / STEAM_PER_WATER;
                excessWater %= STEAM_PER_WATER;

                FluidStack drainedWater = getBoilerFluidFromContainer(getInputTank(), (int) amount);
                if (amount != 0 && (drainedWater == null || drainedWater.amount < amount)) {
                    getMetaTileEntity().explodeMultiblock((1.0f * currentHeat / getMaximumHeat()) * 8.0f);
                } else {
                    setLastTickSteam(generatedSteam);
                    getOutputTank().fill(Materials.Steam.getFluid(generatedSteam), true);
                }
            }
            if (currentHeat < getMaximumHeat()) {
                setHeat(currentHeat + 1);
            }

            if (++progressTime > maxProgressTime) {
                completeRecipe();
            }
        }
    }

    private int getMaximumHeatFromMaintenance() {
        if (ConfigHolder.machines.enableMaintenance) {
            return (int) Math.min(currentHeat,
                    (1 - 0.1 * getMetaTileEntity().getNumMaintenanceProblems()) * getMaximumHeat());
        } else return currentHeat;
    }

    private int adjustEUtForThrottle(int rawEUt) {
        int throttle = getMetaTileEntity().getThrottle();
        return Math.max(25, (int) (rawEUt * (throttle / 100.0)));
    }

    private int adjustBurnTimeForThrottle(int rawBurnTime) {
        int EUt = getMetaTileEntity().boilerType.steamPerTick();
        int adjustedEUt = adjustEUtForThrottle(EUt);
        int adjustedBurnTime = rawBurnTime * EUt / adjustedEUt;
        // Account for integer division and store the extra EUt to use later
        this.excessProjectedEU += EUt * rawBurnTime - adjustedEUt * adjustedBurnTime;
        adjustedBurnTime += this.excessProjectedEU / adjustedEUt;
        this.excessProjectedEU %= adjustedEUt;
        return adjustedBurnTime;
    }

    private int getMaximumHeat() {
        return getMetaTileEntity().boilerType.getTicksToBoiling();
    }

    public int getHeatScaled() {
        return (int) Math.round(currentHeat / (1.0 * getMaximumHeat()) * 100);
    }

    public void setHeat(int heat) {
        if (heat != this.currentHeat && !metaTileEntity.getWorld().isRemote) {
            writeCustomData(BOILER_HEAT, b -> b.writeVarInt(heat));
        }
        this.currentHeat = heat;
    }

    public int getLastTickSteam() {
        return lastTickSteamOutput;
    }

    public void setLastTickSteam(int lastTickSteamOutput) {
        if (lastTickSteamOutput != this.lastTickSteamOutput && !metaTileEntity.getWorld().isRemote) {
            writeCustomData(BOILER_LAST_TICK_STEAM, b -> b.writeVarInt(lastTickSteamOutput));
        }
        this.lastTickSteamOutput = lastTickSteamOutput;
    }

    @Override
    public int getInfoProviderEUt() {
        return this.lastTickSteamOutput;
    }

    @Override
    public boolean consumesEnergy() {
        return false;
    }

    public void invalidate() {
        progressTime = 0;
        maxProgressTime = 0;
        recipeEUt = 0;
        setActive(false);
        setLastTickSteam(0);
    }

    @Override
    protected void completeRecipe() {
        progressTime = 0;
        setMaxProgress(0);
        recipeEUt = 0;
        wasActiveAndNeedsUpdate = true;
    }

    @NotNull
    @Override
    public MetaTileEntitySuSyLargeBoiler getMetaTileEntity() {
        return (MetaTileEntitySuSyLargeBoiler) super.getMetaTileEntity();
    }

    @NotNull
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        compound.setInteger("Heat", currentHeat);
        compound.setInteger("ExcessWater", excessWater);
        compound.setInteger("ExcessProjectedEU", excessProjectedEU);
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.currentHeat = compound.getInteger("Heat");
        this.excessWater = compound.getInteger("ExcessWater");
        this.excessProjectedEU = compound.getInteger("ExcessProjectedEU");
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeVarInt(currentHeat);
        buf.writeVarInt(lastTickSteamOutput);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.currentHeat = buf.readVarInt();
        this.lastTickSteamOutput = buf.readVarInt();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == BOILER_HEAT) {
            this.currentHeat = buf.readVarInt();
        } else if (dataId == BOILER_LAST_TICK_STEAM) {
            this.lastTickSteamOutput = buf.readVarInt();
        }
    }

    // Required overrides to use RecipeLogic, but all of them are redirected by the above overrides.

    @Override
    protected long getEnergyInputPerSecond() {
        GTLog.logger.error("Large Boiler called getEnergyInputPerSecond(), this should not be possible!");
        return 0;
    }

    @Override
    protected long getEnergyStored() {
        GTLog.logger.error("Large Boiler called getEnergyStored(), this should not be possible!");
        return 0;
    }

    @Override
    protected long getEnergyCapacity() {
        GTLog.logger.error("Large Boiler called getEnergyCapacity(), this should not be possible!");
        return 0;
    }

    @Override
    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        GTLog.logger.error("Large Boiler called drawEnergy(), this should not be possible!");
        return false;
    }

    @Override
    public long getMaxVoltage() {
        GTLog.logger.error("Large Boiler called getMaxVoltage(), this should not be possible!");
        return 0;
    }

    /**
     * @param fluidHandler the handler to drain from
     * @param amount       the amount to drain
     * @return a valid boiler fluid from a container
     */
    @Nullable
    private static FluidStack getBoilerFluidFromContainer(@NotNull IFluidHandler fluidHandler, int amount) {
        if (amount == 0) return null;
        FluidStack drainedWater = fluidHandler.drain(Materials.Water.getFluid(amount), true);
        if (drainedWater == null || drainedWater.amount == 0) {
            drainedWater = fluidHandler.drain(Materials.DistilledWater.getFluid(amount), true);
        }
        if (drainedWater == null || drainedWater.amount == 0) {
            for (String fluidName : ConfigHolder.machines.boilerFluids) {
                Fluid fluid = FluidRegistry.getFluid(fluidName);
                if (fluid != null) {
                    drainedWater = fluidHandler.drain(new FluidStack(fluid, amount), true);
                    if (drainedWater != null && drainedWater.amount > 0) {
                        break;
                    }
                }
            }
        }
        return drainedWater;
    }
}
