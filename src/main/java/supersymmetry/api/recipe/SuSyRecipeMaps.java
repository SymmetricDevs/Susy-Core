package supersymmetry.api.recipe;

import gregtech.api.recipes.RecipeMap;
import gregtech.core.sound.GTSoundEvents;
import supersymmetry.api.recipe.builders.CoilingCoilRecipeBuilder;

public class SuSyRecipeMaps {

    public static final RecipeMap<CoilingCoilRecipeBuilder> COOLING_RECIPES = new RecipeMap<>("magnetic_refrigerator", 1, 3, 0, 3, 0, 0, 0, 1, new CoilingCoilRecipeBuilder(), false)
            .setSound(GTSoundEvents.COOLING);
}
