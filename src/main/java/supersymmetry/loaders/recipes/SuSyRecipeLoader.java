package supersymmetry.loaders.recipes;

import supersymmetry.loaders.SuSyMetaTileEntityLoader;

public class SuSyRecipeLoader {

    public static void init() {
        SuSyMetaTileEntityLoader.init();
        FridgeRecipes.init();
        CoagulationRecipes.init();
        VulcanizationRecipes.init();
        OreRecipeHandler.init();
        // make more loaders to categorize recipes and what is added
    }
}
