package supersymmetry.api.recipe;

import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.PrimitiveRecipeBuilder;
import gregtech.api.sound.GTSounds;
import supersymmetry.api.recipe.builders.CoilingCoilRecipeBuilder;

public class SuSyRecipeMaps {

    public static final RecipeMap<CoilingCoilRecipeBuilder> COOLING_RECIPES = new RecipeMap<>("magnetic_refrigerator", 1, 3, 0, 3, 0, 0, 0, 1, new CoilingCoilRecipeBuilder(), false)
            .setSound(GTSounds.COOLING);

    public static final RecipeMap<PrimitiveRecipeBuilder> COAGULATION_RECIPES = new RecipeMap<>("coagulation_tank", 0,  0,  1, 1,1,1,0,0, new PrimitiveRecipeBuilder(), false);
}
