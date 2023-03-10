package supersymmetry.loaders;

import supersymmetry.api.recipe.CoagulationRecipes;
import supersymmetry.api.recipe.FridgeRecipes;
import supersymmetry.api.recipe.VulcanizationRecipes;

public class SuSyRecipeLoader {

    public static void init() {
        SuSyMetaTileEntityLoader.init();
        FridgeRecipes.init();
        CoagulationRecipes.init();
        VulcanizationRecipes.init();
        // make more loaders to categorize recipes and what is added
    }
}
