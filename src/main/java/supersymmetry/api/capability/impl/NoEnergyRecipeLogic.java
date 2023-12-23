package supersymmetry.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.OverclockingLogic;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class NoEnergyRecipeLogic extends RecipeLogicEnergy {
    public NoEnergyRecipeLogic(MetaTileEntity tileEntity, RecipeMap recipeMap, Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap, energyContainer);
    }

    protected long getEnergyInputPerSecond() {
        return 2147483647L;
    }

    protected long getEnergyStored() {
        return 0L;
    }

    protected long getEnergyCapacity() {
        return 2147483647L;
    }

    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        return true;
    }

    protected long getMaxVoltage() {
        return 1L;
    }

    protected int[] runOverclockingLogic(@Nonnull IRecipePropertyStorage propertyStorage, int recipeEUt, long maxVoltage, int recipeDuration, int amountOC) {
        return OverclockingLogic.standardOverclockingLogic(1, this.getMaxVoltage(), recipeDuration, amountOC, this.getOverclockingDurationDivisor(), this.getOverclockingVoltageMultiplier());
    }

    public long getMaximumOverclockVoltage() {
        return GTValues.V[1];
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
