package supersymmetry.api.recipes;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.core.sound.GTSoundEvents;
import gregtech.api.recipes.builders.PrimitiveRecipeBuilder;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import supersymmetry.api.recipes.builders.CoilingCoilRecipeBuilder;
import supersymmetry.api.recipes.builders.SinteringRecipeBuilder;


public class SuSyRecipeMaps {

    public static final RecipeMap<CoilingCoilRecipeBuilder> COOLING_RECIPES = new RecipeMap<>("magnetic_refrigerator", 1, 3, 0, 3, 0, 0, 0, 1, new CoilingCoilRecipeBuilder(), false)
            .setSound(GTSoundEvents.COOLING);

    public static final RecipeMap<SinteringRecipeBuilder> SINTERING_RECIPES = new RecipeMap<>("sintering_oven", 1, 4, 1, 4, 1, 2, 0, 2, new SinteringRecipeBuilder(), false)
            .setSound(GTSoundEvents.COMBUSTION);

    public static final RecipeMap<PrimitiveRecipeBuilder> COAGULATION_RECIPES = new RecipeMap<>("coagulation_tank", 0, 2, 1, 1, 1, 2, 0, 0, new PrimitiveRecipeBuilder(), false);

    public static final RecipeMap<SimpleRecipeBuilder> VULCANIZATION_RECIPES = new RecipeMap<>("vulcanizing_press", 4, 2, 2, 1, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, true, GuiTextures.MOLD_OVERLAY)
            .setSound(GTSoundEvents.COMBUSTION);

    public static final RecipeMap<SimpleRecipeBuilder> ROASTER_RECIPES = new RecipeMap<>("roaster", 1, 2, 0, 2, 0, 2, 0, 3, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.COMBUSTION);

    public static final RecipeMap<SimpleRecipeBuilder> VACUUM_CHAMBER = new RecipeMap<>("vacuum_chamber", 1, 4, 1, 1, 0, 0, 0, 0, new SimpleRecipeBuilder(), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, ProgressWidget.MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.CENTRIFUGE);

    public static final RecipeMap<SimpleRecipeBuilder> CSTR_RECIPES = new RecipeMap<>("continuous_stirred_tank_reactor", 0, 0, 4, 2, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> FIXED_BED_REACTOR_RECIPES = new RecipeMap<>("fixed_bed_reactor", 1, 0, 3, 2, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> TRICKLE_BED_REACTOR_RECIPES = new RecipeMap<>("trickle_bed_reactor", 1, 0, 3, 2, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> CRYSTALLIZER_RECIPES = new RecipeMap<>("crystallizer",1, 1, 1, 1, new SimpleRecipeBuilder(), false);

    public static final RecipeMap<SimpleRecipeBuilder> BUBBLE_COLUMN_REACTOR_RECIPES = new RecipeMap<>("bubble_column_reactor", 1, 0, 3, 2, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> DRYER = new RecipeMap<>("dryer", 1, 1, 1, 1, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> FLUIDIZED_BED_REACTOR_RECIPES = new RecipeMap<>("fluidized_bed_reactor", 2, 1, 3, 2, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> POLYMERIZATION_RECIPES = new RecipeMap<>("polymerization_tank", 2, 1, 2, 1, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);


    public static void init(){
        RecipeMaps.SIFTER_RECIPES.setMaxFluidInputs(1);
        RecipeMaps.SIFTER_RECIPES.setMaxFluidOutputs(1);
        RecipeMaps.SIFTER_RECIPES.setMaxInputs(2);
        RecipeMaps.CENTRIFUGE_RECIPES.setMaxFluidInputs(2);
        RecipeMaps.CENTRIFUGE_RECIPES.setSlotOverlay(false, true, false, GuiTextures.CENTRIFUGE_OVERLAY);

        RecipeMaps.MIXER_RECIPES.setMaxFluidInputs(3);
        RecipeMaps.MIXER_RECIPES.setMaxFluidOutputs(2);
        RecipeMaps.ARC_FURNACE_RECIPES.setMaxInputs(2);
    }
}
