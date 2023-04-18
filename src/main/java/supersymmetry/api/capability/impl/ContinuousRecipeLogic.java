package supersymmetry.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;

import java.util.function.Supplier;

public class ContinuousRecipeLogic extends RecipeLogicEnergy {

    public ContinuousRecipeLogic(MetaTileEntity tileEntity, RecipeMap recipeMap, Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap, energyContainer);
    }

    @Override
    protected boolean prepareRecipe(Recipe recipe) {
        recipe = Recipe.trimRecipeOutputs(recipe, this.getRecipeMap(), this.metaTileEntity.getItemOutputLimit(), this.metaTileEntity.getFluidOutputLimit());

        int recipeTier = GTUtility.getTierByVoltage(recipe.getEUt());
        int maximumTier = getOverclockForTier(getMaximumOverclockVoltage());
        int numberOfOCs = maximumTier - recipeTier;
        double parallelLimitDouble = Math.pow(this.getOverclockingDurationDivisor(), numberOfOCs) / recipe.getDuration();
        int parallelLimit = parallelLimitDouble <= 1 ? 1 : (int) parallelLimitDouble;

        recipe = findParallelRecipe(
                this,
                recipe,
                getInputInventory(),
                getInputTank(),
                getOutputInventory(),
                getOutputTank(),
                getMaxParallelVoltage(),
                parallelLimit);
        if (recipe != null && this.setupAndConsumeRecipeInputs(recipe, this.getInputInventory())) {
            this.setupRecipe(recipe);
            return true;
        }
        return false;
    }
}
