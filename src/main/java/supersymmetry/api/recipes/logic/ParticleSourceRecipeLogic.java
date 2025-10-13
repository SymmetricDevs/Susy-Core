package supersymmetry.api.recipes.logic;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;

public class ParticleSourceRecipeLogic extends MultiblockRecipeLogic {
    public ParticleSourceRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity);
    }

    @Override
    public boolean prepareRecipe(Recipe recipe) {
        recipe = Recipe.trimRecipeOutputs(recipe, this.getRecipeMap(), this.metaTileEntity.getItemOutputLimit(), this.metaTileEntity.getFluidOutputLimit());

        if (recipe != null && this.setupAndConsumeRecipeInputs(recipe, this.getInputInventory())) {
            this.setupRecipe(recipe);
            return true;
        }

        return false;
    }
}
