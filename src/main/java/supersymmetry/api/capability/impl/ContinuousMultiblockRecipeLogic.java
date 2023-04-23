package supersymmetry.api.capability.impl;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.GTUtility;

public class ContinuousMultiblockRecipeLogic extends MultiblockRecipeLogic {
    public ContinuousMultiblockRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity);
    }

    public ContinuousMultiblockRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
        super(tileEntity, hasPerfectOC);
    }

    @Override
    protected boolean prepareRecipe(Recipe recipe) {
        recipe = Recipe.trimRecipeOutputs(recipe, this.getRecipeMap(), this.metaTileEntity.getItemOutputLimit(), this.metaTileEntity.getFluidOutputLimit());

        int recipeTier = GTUtility.getTierByVoltage(recipe.getEUt());
        int maximumTier = getOverclockForTier(getMaximumOverclockVoltage());
        int numberOfOCs = maximumTier - recipeTier;
        double parallelLimitDouble = Math.pow(this.getOverclockingDurationDivisor(), numberOfOCs) / recipe.getDuration();
        int parallelLimit = parallelLimitDouble <= 1 ? 1 : (int) parallelLimitDouble;

        recipe = findParallelRecipe(this, recipe,
                getInputInventory(), getInputTank(),
                getOutputInventory(), getOutputTank(),
                getMaxParallelVoltage(), parallelLimit);

        if (recipe != null && this.setupAndConsumeRecipeInputs(recipe, this.getInputInventory())) {
            this.setupRecipe(recipe);
            return true;
        }

        return false;
    }
}
