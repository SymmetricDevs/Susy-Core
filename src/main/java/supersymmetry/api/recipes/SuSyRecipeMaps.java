package supersymmetry.api.recipes;

import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.PrimitiveRecipeBuilder;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.core.sound.GTSoundEvents;
import supersymmetry.api.recipes.builders.CoilingCoilRecipeBuilder;
import supersymmetry.api.recipes.builders.SinteringRecipeBuilder;

public class SuSyRecipeMaps {
    public static final RecipeMap<CoilingCoilRecipeBuilder> COOLING_RECIPES;
    public static final RecipeMap<SinteringRecipeBuilder> SINTERING_RECIPES;
    public static final RecipeMap<PrimitiveRecipeBuilder> COAGULATION_RECIPES;
    public static final RecipeMap<SimpleRecipeBuilder> VULCANIZATION_RECIPES;
    public static final RecipeMap<SimpleRecipeBuilder> ROASTER_RECIPES;

    static {
        COOLING_RECIPES = (new RecipeMap<>("magnetic_refrigerator", 3, 3, 0, 1, new CoilingCoilRecipeBuilder(), false))
            .setSound(GTSoundEvents.COOLING);
        SINTERING_RECIPES = new RecipeMap<>("sintering_oven", 4, 4, 2, 2, new SinteringRecipeBuilder(), false)
            .setSound(GTSoundEvents.COMBUSTION);
        COAGULATION_RECIPES = new RecipeMap<>("coagulation_tank", 2,1,2, 0, new PrimitiveRecipeBuilder(), false);
        VULCANIZATION_RECIPES = new RecipeMap<>("vulcanizing_press", 3, 2, 1, 1, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, true, GuiTextures.MOLD_OVERLAY)        
            .setSound(GTSoundEvents.COMBUSTION);
        ROASTER_RECIPES = new RecipeMap<>("roaster", 2, 2, 0, 3, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.COMBUSTION);
    }
}
