package supersymmetry.loaders;

import supersymmetry.api.recipe.CoagulationRecipes;
import supersymmetry.api.recipe.FridgeRecipes;

public class SuSyRecipeLoader {

    public static void init() {
        SuSyMetaTileEntityLoader.init();
        FridgeRecipes.init();
        CoagulationRecipes.init();
        // make more loaders to categorize recipes and what is added
    }
}
