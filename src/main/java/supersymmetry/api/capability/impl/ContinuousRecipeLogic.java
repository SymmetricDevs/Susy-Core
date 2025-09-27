package supersymmetry.api.capability.impl;

import static gregtech.api.GTValues.ULV;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.util.GTUtility;
import supersymmetry.api.recipes.builders.logic.SuSyOverclockingLogic;
import supersymmetry.api.recipes.catalysts.CatalystInfo;

public class ContinuousRecipeLogic extends CatalystRecipeLogic {

    public ContinuousRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap,
                                 Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap, energyContainer);
    }

    @Override
    public boolean prepareRecipe(Recipe recipe) {
        recipe = Recipe.trimRecipeOutputs(recipe, this.getRecipeMap(), this.metaTileEntity.getItemOutputLimit(),
                this.metaTileEntity.getFluidOutputLimit());

        calculateOverclockLimit(recipe);
        recipe = findParallelRecipe(
                recipe,
                getInputInventory(),
                getInputTank(),
                getOutputInventory(),
                getOutputTank(),
                getMaxParallelVoltage(),
                getParallelLimit());

        if (recipe != null && this.setupAndConsumeRecipeInputs(recipe, this.getInputInventory())) {
            this.setupRecipe(recipe);
            return true;
        }
        return false;
    }

    @Override
    protected int[] runOverclockingLogic(@NotNull IRecipePropertyStorage propertyStorage, int recipeEUt,
                                         long maxVoltage, int duration, int amountOC) {
        double[] overclock = runContinuousOverclockingLogic(recipeEUt, maxVoltage, duration, amountOC);
        return new int[] { (int) overclock[0], overclock[1] <= 1 ? 1 : (int) overclock[1] };
    }

    protected double[] runContinuousOverclockingLogic(int recipeEUt, long maxVoltage, int duration, int amountOC) {
        if (requiredCatalystTier != CatalystInfo.NO_TIER && catalystInfo != null) {
            return SuSyOverclockingLogic.continuousCatalystOverclockingLogic(
                    recipeEUt,
                    maxVoltage,
                    duration,
                    amountOC,
                    catalystInfo,
                    requiredCatalystTier,
                    getOverclockingDurationDivisor(),
                    getOverclockingVoltageMultiplier());
        } else {
            return SuSyOverclockingLogic.continuousOverclockingLogic(
                    recipeEUt,
                    maxVoltage,
                    duration,
                    amountOC,
                    this.getOverclockingDurationDivisor(),
                    this.getOverclockingVoltageMultiplier());
        }
    }

    protected void calculateOverclockLimit(Recipe recipe) {
        if (!isAllowOverclocking()) return;

        int recipeTier = GTUtility.getTierByVoltage(recipeEUt);
        int maximumTier = getOverclockForTier(getMaximumOverclockVoltage());
        if (maximumTier <= GTValues.LV) return;

        // The maximum number of overclocks is determined by the difference between the tier the recipe is running at,
        // and the maximum tier that the machine can overclock to.
        int numberOfOCs = maximumTier - recipeTier;
        if (recipeTier == ULV) numberOfOCs--; // no ULV overclocking

        double parallelLimitDouble = 1 / runContinuousOverclockingLogic(recipe.getEUt(), getMaximumOverclockVoltage(),
                recipe.getDuration(), numberOfOCs)[1];

        setParallelLimit(parallelLimitDouble <= 1 ? 1 : (int) parallelLimitDouble);
    }
}
