package supersymmetry.loaders;

import supersymmetry.api.recipe.FridgeRecipes;

public class SuSyRecipeLoader {

    public static void init() {
        SuSyMetaTileEntityLoader.init();
        FridgeRecipes.init();
        // make more loaders to categorize recipes and what is added
    }
}
