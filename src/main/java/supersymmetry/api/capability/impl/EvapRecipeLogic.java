package supersymmetry.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.recipes.logic.OverclockingLogic;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityEvaporationPool;

import javax.annotation.Nonnull;

import static supersymmetry.api.util.SuSyUtility.JOULES_PER_EU;

public class EvapRecipeLogic extends MultiblockRecipeLogic {
    private final MetaTileEntityEvaporationPool pool;
    public static final int HEAT_DENOMINATOR = 6*20; //transfers one sixth of its energy every second (20 ticks)
    public static final int MAX_STEP_FRACTION = 4; //denominator of fraction of progress which can be done in one step. (1/(MAX_STEP_FRACTION)) = max percent allowed

    public EvapRecipeLogic(MetaTileEntityEvaporationPool tileEntity) {
        super(tileEntity);
        pool = tileEntity;
        
    }

    @Override
    protected void updateRecipeProgress() {
        //if null then no heating can be done, otherwise add joules according to coil values and energy available
        if (pool.coilStats != null) {
            int coilHeat = pool.coilStats.getCoilTemperature();
            //assumes specific heat of 1J/(g*delta temp) and perfect heat transfer on one face of the coil for 1/6 of total delta temp. Uses mass as a multiplier and 1/4 because its not a solid block of material
            int heatingJoules = (coilHeat/HEAT_DENOMINATOR) * ((int)pool.coilStats.getMaterial().getMass()/4) * ( ((pool.getColumnCount()/2 +1) * pool.getRowCount()) + pool.getColumnCount()/2);
            heatingJoules = Math.min(((int)getEnergyStored()) * JOULES_PER_EU, heatingJoules);
            pool.inputEnergy(heatingJoules);
        }

        int maxSteps = pool.calcMaxSteps(recipeEUt * JOULES_PER_EU);

        if (this.canRecipeProgress && maxSteps > 0) {
            hasNotEnoughEnergy = false;
            int actualSteps =  Math.min(this.maxProgressTime >> 2, maxSteps);
            progressTime += actualSteps;

            int resultingEnergy;

            //take energy from buffer first to avoid muddying kJ calculations
            if (pool.getJoulesBuffer()/(recipeEUt * JOULES_PER_EU) > 0) {
                resultingEnergy =  pool.getJoulesBuffer()/(recipeEUt * JOULES_PER_EU);
                actualSteps -= resultingEnergy;
                resultingEnergy = pool.getJoulesBuffer() - (resultingEnergy * (recipeEUt * JOULES_PER_EU));
                pool.setJoulesBuffer(resultingEnergy);
            }

            resultingEnergy = pool.getKiloJoules() - (recipeEUt * JOULES_PER_EU * actualSteps)/1000 - (recipeEUt * JOULES_PER_EU * actualSteps % 1000 == 0 ? 0 : 1);
            pool.setKiloJoules(resultingEnergy);

            if (this.progressTime > this.maxProgressTime) completeRecipe();
        } else {
            this.hasNotEnoughEnergy = true;
            //only decrease progress once a tick when using max sized pool
            if (pool.getOffsetTimer() % (pool.MAX_COLUMNS * pool.MAX_COLUMNS)/(pool.getColumnCount() * pool.getRowCount()) == 0L) this.decreaseProgress();
        }
    }

    //copied from NoEnergyMultiblockRecipeLogic and modified to allow energy and no energy
    @Override
    protected long getEnergyInputPerSecond() {
        return super.getEnergyInputPerSecond() == 0? 2147483647L : super.getEnergyInputPerSecond();
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
    protected boolean drawEnergy(int recipeEUt, boolean simulate) { return true; }

    @Override
    protected long getMaxVoltage() {
        return Math.max(1L, super.getMaxVoltage());
    }

    @Override
    protected int[] runOverclockingLogic(@Nonnull IRecipePropertyStorage propertyStorage, int recipeEUt, long maxVoltage, int recipeDuration, int amountOC) {
        return OverclockingLogic.standardOverclockingLogic(recipeEUt, this.getMaxVoltage(), recipeDuration, amountOC, this.getOverclockingDurationDivisor(), this.getOverclockingVoltageMultiplier());
    }

    @Override
    public long getMaximumOverclockVoltage() {
        return getEnergyCapacity() == 0? GTValues.V[1] : super.getMaximumOverclockVoltage();
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
