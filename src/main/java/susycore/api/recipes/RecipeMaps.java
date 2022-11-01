package susycore.api.recipes;

import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.sound.GTSounds;
import susycore.api.recipes.builders.FridgeRecipeBuilder;

public class RecipeMaps {

    public static final RecipeMap<FridgeRecipeBuilder> COOLING_RECIPES = new RecipeMap<>("magnetic_refrigerator", 1, 3, 0, 3, 0, 0, 0, 1, new FridgeRecipeBuilder(), false)
            .setSound(GTSounds.FURNACE);

}
