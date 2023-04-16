package supersymmetry.api.recipes;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
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
    public static final RecipeMap<SimpleRecipeBuilder> VACUUM_CHAMBER;
    public static final RecipeMap<SimpleRecipeBuilder> CSTR_RECIPES;
    public static final RecipeMap<SimpleRecipeBuilder> FIXED_BED_REACTOR_RECIPES;
    public static final RecipeMap<SimpleRecipeBuilder> TRICKLE_BED_REACTOR_RECIPES;
    public static final RecipeMap<SimpleRecipeBuilder> CRYSTALLIZER_RECIPES;
    public static final RecipeMap<SimpleRecipeBuilder> BUBBLE_COLUMN_REACTOR_RECIPES;
    public static final RecipeMap<SimpleRecipeBuilder> FLUIDIZED_BED_REACTOR_RECIPES;
    public static final RecipeMap<SimpleRecipeBuilder> POLYMERIZATION_RECIPES;

    static {
        COOLING_RECIPES = new RecipeMap<>("magnetic_refrigerator", 3, 3, 0, 1, new CoilingCoilRecipeBuilder(), false)
            .setSound(GTSoundEvents.COOLING);

        SINTERING_RECIPES = new RecipeMap<>("sintering_oven", 4, 4, 2, 2, new SinteringRecipeBuilder(), false)
            .setSound(GTSoundEvents.COMBUSTION);

        COAGULATION_RECIPES = new RecipeMap<>("coagulation_tank", 2,1,2, 0, new PrimitiveRecipeBuilder(), false);

        VULCANIZATION_RECIPES = new RecipeMap<>("vulcanizing_press", 3, 2, 1, 1, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, true, GuiTextures.MOLD_OVERLAY)        
            .setSound(GTSoundEvents.COMBUSTION);

        ROASTER_RECIPES = new RecipeMap<>("roaster", 2, 2, 0, 3, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.COMBUSTION);

        VACUUM_CHAMBER = new RecipeMap<>("vacuum_chamber", 4, 1, 0, 0, new SimpleRecipeBuilder(), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, ProgressWidget.MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.CENTRIFUGE);

        CSTR_RECIPES = new RecipeMap<>("continuous_stirred_tank_reactor", 0, 0, 4, 2, new SimpleRecipeBuilder(), false)
                .setSound(GTSoundEvents.CHEMICAL_REACTOR);

        FIXED_BED_REACTOR_RECIPES = new RecipeMap<>("fixed_bed_reactor", 1, 0, 3, 2, new SimpleRecipeBuilder(), false)
                .setSound(GTSoundEvents.CHEMICAL_REACTOR);

        TRICKLE_BED_REACTOR_RECIPES = new RecipeMap<>("trickle_bed_reactor", 1, 0, 3, 2, new SimpleRecipeBuilder(), false)
                .setSound(GTSoundEvents.CHEMICAL_REACTOR);

        CRYSTALLIZER_RECIPES = new RecipeMap<>("crystallizer",1, 1, 1, 1, new SimpleRecipeBuilder(), false);

        BUBBLE_COLUMN_REACTOR_RECIPES = new RecipeMap<>("bubble_column_reactor", 0, 0, 3, 2, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

        FLUIDIZED_BED_REACTOR_RECIPES = new RecipeMap<>("fluidized_bed_reactor", 1, 0, 3, 2, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

        POLYMERIZATION_RECIPES = new RecipeMap<>("polymerization_tank", 1, 1, 2, 1, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);
    }

    public static void init(){
        RecipeMaps.SIFTER_RECIPES.setMaxFluidInputs(1);
        RecipeMaps.SIFTER_RECIPES.setMaxFluidOutputs(1);
        RecipeMaps.SIFTER_RECIPES.setMaxInputs(2);
    }
}
