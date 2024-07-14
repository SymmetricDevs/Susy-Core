package supersymmetry.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.logic.OverclockingLogic;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import supersymmetry.api.SusyLog;
import supersymmetry.api.recipes.properties.EvaporationEnergyProperty;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityEvaporationPool;

import javax.annotation.Nonnull;

import static supersymmetry.api.util.SuSyUtility.JOULES_PER_EU;

public class EvapRecipeLogic extends MultiblockRecipeLogic {
    private final MetaTileEntityEvaporationPool pool;
    public static final int HEAT_DENOMINATOR = 6 * 10; //transfers one sixth of its energy every half a second (10 ticks) as 1/6th every second seemed too low, with cupro depositing 1ALv/t
    public static final int MAX_STEP_FRACTION = 4; //denominator of fraction of progress which can be done in one step. (1/(MAX_STEP_FRACTION)) = max percent allowed

    public EvapRecipeLogic(MetaTileEntityEvaporationPool tileEntity) {
        super(tileEntity);
        pool = tileEntity;
    }

    public int getJt() {
        if (this.previousRecipe == null || !this.previousRecipe.hasProperty(EvaporationEnergyProperty.getInstance())) {
            return -1;
        }

        return this.previousRecipe.getProperty(EvaporationEnergyProperty.getInstance(), -1);
    }

    //do not attempt to run invalid recipes
    @Override
    public boolean checkRecipe(Recipe recipe) {
        return recipe.hasProperty(EvaporationEnergyProperty.getInstance()) && recipe.getProperty(EvaporationEnergyProperty.getInstance(), -1) != -1;
    }


    @Override
    protected void updateRecipeProgress() {
        //if null then no heating can be done, otherwise add joules according to coil values and energy available
        if (pool.coilStats != null && pool.getIsHeated()) {
            int coilHeat = pool.coilStats.getCoilTemperature();
            //assumes specific heat of 1J/(g*delta temp) and perfect heat transfer on one face of the coil for 1/6 of total delta temp. Uses mass as a multiplier and 1/4 because coil is not solid block of primary material. Last portion calculates number of coils.
            int heatingJoules = (coilHeat / HEAT_DENOMINATOR) * ((int) pool.coilStats.getMaterial().getMass() / 4) * (((pool.getColumnCount() / 2 + 1) * pool.getRowCount()) + pool.getColumnCount() / 2); //20 should limit it to reasonable per tick levels
            heatingJoules = Math.min(((int) getEnergyStored()) * JOULES_PER_EU, heatingJoules); //attempt to transfer entire heatingJoules amount or what is left in energy container
            boolean couldInput = pool.inputEnergy(heatingJoules);
            if (couldInput)
                pool.getEnergyContainer().removeEnergy(heatingJoules / (JOULES_PER_EU * pool.coilStats.getEnergyDiscount())); //energy should always be available as heatingJoules is either itself or energy*JpEU
        }

        int maxSteps = pool.calcMaxSteps(getJt());

        //if the recipe can progress and at least one step can be taken
        if (maxSteps > 0) {
            hasNotEnoughEnergy = false;

            int actualSteps = Math.min(this.maxProgressTime / MAX_STEP_FRACTION, maxSteps);
            progressTime += actualSteps; //actualSteps <= maxSteps, meaning it can always be progressed this amount

            int kJFloor = getJt() * actualSteps / 1000;
            int joulesNeeded;

            //if kJ store is insufficient to cover full cost, joulesNeeded is whatever remains after kJ covers cost
            if (pool.getKiloJoules() < kJFloor) {
                joulesNeeded = getJt() * actualSteps - pool.getKiloJoules() * 1000;
            } else {
                joulesNeeded = getJt() * actualSteps - kJFloor * 1000; //difference in joules betweeen kJ losslessly required and J actually required (4kJ losslessly covers 3kJ out of 3600J, meaning 600J are needed to avoid wasting kJ
            }

            //if buffer cant cover draw entirely from kiloJoules
            if (pool.getJoulesBuffer() < joulesNeeded) {
                ++kJFloor;
                joulesNeeded = 0;
            }

            //drain appropriately
            pool.setKiloJoules(pool.getKiloJoules() - kJFloor);
            pool.setJoulesBuffer(pool.getJoulesBuffer() - joulesNeeded);

            if (this.progressTime > this.maxProgressTime) completeRecipe();
        } else {
            this.hasNotEnoughEnergy = true;
            //only decrease progress once a tick when using max sized pool (two sequential divisions to avoid cast to long)
            if (pool.getOffsetTimer() % (((MetaTileEntityEvaporationPool.MAX_COLUMNS * MetaTileEntityEvaporationPool.MAX_COLUMNS) / pool.getColumnCount()) / pool.getRowCount()) == 0)
                this.decreaseProgress();
        }
    }

    //copied from NoEnergyMultiblockRecipeLogic and modified to allow energy and no energy
    @Override
    protected long getEnergyInputPerSecond() {
        return super.getEnergyInputPerSecond() == 0 ? 2147483647L : super.getEnergyInputPerSecond();
    }

    @Override
    protected long getEnergyStored() {
        return Math.max(0L, super.getEnergyStored());
    }

    @Override
    protected long getEnergyCapacity() {
        return super.getEnergyCapacity() == 0 ? 2147483647L : super.getEnergyCapacity();
    }

    @Override
    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        return true;
    }

    // stops multi from "filling" with energy due to -1 EuT required (?) for custom power logic
    @Override
    protected boolean hasEnoughPower(@Nonnull int[] resultOverclock) {
        int totalEUt = resultOverclock[0] * resultOverclock[1];
        int capacity;
        if (totalEUt >= 0) {
            if ((long) totalEUt > this.getEnergyCapacity() / 2L) {
                capacity = resultOverclock[0];
            } else {
                capacity = totalEUt;
            }

            return this.getEnergyStored() >= (long) capacity;
        } else {
            return true;
        }
    }

    @Override
    public long getMaxVoltage() {
        return Math.max(1L, super.getMaxVoltage());
    }

    @Override
    protected int[] runOverclockingLogic(@Nonnull IRecipePropertyStorage propertyStorage, int recipeEUt, long maxVoltage, int recipeDuration, int amountOC) {
        return OverclockingLogic.standardOverclockingLogic(recipeEUt, this.getMaxVoltage(), recipeDuration, amountOC, this.getOverclockingDurationDivisor(), this.getOverclockingVoltageMultiplier());
    }

    @Override
    public long getMaximumOverclockVoltage() {
        return getEnergyCapacity() == 0 ? GTValues.V[1] : super.getMaximumOverclockVoltage();
    }

    public void invalidate() {
        this.previousRecipe = null;
        this.progressTime = 0;
        this.maxProgressTime = 0;
        this.recipeEUt = 0;
        this.fluidOutputs = null;
        this.itemOutputs = null;
        this.setActive(false);
    }
}
