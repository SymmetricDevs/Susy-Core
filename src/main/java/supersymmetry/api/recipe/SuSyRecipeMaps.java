package supersymmetry.api.recipe;

import gregtech.api.recipes.RecipeMap;
import gregtech.core.sound.GTSoundEvents;
import supersymmetry.api.recipe.builders.CoilingCoilRecipeBuilder;
import supersymmetry.api.recipe.builders.SinteringRecipeBuilder;

public class SuSyRecipeMaps {

    public static final RecipeMap<CoilingCoilRecipeBuilder> COOLING_RECIPES = new RecipeMap<>("magnetic_refrigerator", 1, 3, 0, 3, 0, 0, 0, 1, new CoilingCoilRecipeBuilder(), false)
            .setSound(GTSoundEvents.COOLING);

    public static final RecipeMap<SinteringRecipeBuilder> SINTERING_RECIPES = new RecipeMap<>("sintering_oven", 1, 4, 1, 4, 1, 2, 0, 2, new SinteringRecipeBuilder(), false)
            .setSound(GTSoundEvents.COMBUSTION);
}
