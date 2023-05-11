package supersymmetry.api.recipes;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.core.sound.GTSoundEvents;
import gregtech.api.recipes.builders.PrimitiveRecipeBuilder;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtechfoodoption.recipe.GTFORecipeMaps;
import net.minecraft.client.gui.Gui;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.recipes.builders.CoilingCoilRecipeBuilder;
import supersymmetry.api.recipes.builders.NoEnergyRecipeBuilder;
import supersymmetry.api.recipes.builders.SinteringRecipeBuilder;
import supersymmetry.client.renderer.textures.SusyTextures;


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
            .setSlotOverlay(false, true, false, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSlotOverlay(false, true, true, GuiTextures.MOLECULAR_OVERLAY_4)
            .setSlotOverlay(true, true, GuiTextures.VIAL_OVERLAY_2)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> FIXED_BED_REACTOR_RECIPES = new RecipeMap<>("fixed_bed_reactor", 1, 1, 3, 2, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, SusyGuiTextures.CATALYST_BED_OVERLAY)
            .setSlotOverlay(false, true, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSlotOverlay(true, true, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> TRICKLE_BED_REACTOR_RECIPES = new RecipeMap<>("trickle_bed_reactor", 1, 0, 3, 2, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, SusyGuiTextures.CATALYST_PELLET_OVERLAY)
            .setSlotOverlay(false, true, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSlotOverlay(true, true, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> BUBBLE_COLUMN_REACTOR_RECIPES = new RecipeMap<>("bubble_column_reactor", 1, 0, 3, 2, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, SusyGuiTextures.LARGE_REACTOR_ITEM_OVERLAY)
            .setSlotOverlay(false, true, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSlotOverlay(true, true, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> BATCH_REACTOR_RECIPES = new RecipeMap<>("batch_reactor", 3, 3, 3, 3, new SimpleRecipeBuilder(), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, ProgressWidget.MoveType.CIRCULAR)
            .setSlotOverlay(false, false, false, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(false, false, true, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(false, true, false, GuiTextures.BEAKER_OVERLAY_3)
            .setSlotOverlay(false, true, true, GuiTextures.BEAKER_OVERLAY_3)
            .setSlotOverlay(true, false, false, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(true, true, false, GuiTextures.BEAKER_OVERLAY_3)
            .setSlotOverlay(true, true, true, GuiTextures.BEAKER_OVERLAY_3)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> CRYSTALLIZER_RECIPES = new RecipeMap<>("crystallizer",1, 1, 3, 1, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> DRYER = new RecipeMap<>("dryer", 1, 1, 1, 1, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.COOLING);

    public static final RecipeMap<SimpleRecipeBuilder> ION_EXCHANGE_COLUMN_RECIPES = new RecipeMap<>("ion_exchange_column", 1, 1, 2, 2, new SimpleRecipeBuilder(), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, ProgressWidget.MoveType.VERTICAL)
            .setSlotOverlay(false, false, true, SusyGuiTextures.CUBIC_LATTICE_OVERLAY)
            .setSlotOverlay(false, true, true, GuiTextures.MOLECULAR_OVERLAY_4)
            .setSlotOverlay(true, false, true, SusyGuiTextures.CUBIC_LATTICE_OVERLAY)
            .setSlotOverlay(true, true, true, GuiTextures.MOLECULAR_OVERLAY_4)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> ZONE_REFINER_RECIPES = new RecipeMap<>("zone_refiner", 1, 1, 0, 0, new SimpleRecipeBuilder(), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CRYSTALLIZATION, ProgressWidget.MoveType.HORIZONTAL)
            .setSlotOverlay(false, false, true, GuiTextures.FURNACE_OVERLAY_1)
            .setSlotOverlay(true, false, true, GuiTextures.CRYSTAL_OVERLAY)
            .setSound(GTSoundEvents.FURNACE);

    public static final RecipeMap<SimpleRecipeBuilder> TUBE_FURNACE_RECIPES = new RecipeMap<>("tube_furnace", 3, 1, 1, 0, new SimpleRecipeBuilder(), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, ProgressWidget.MoveType.HORIZONTAL)
            .setSlotOverlay(false, false, false, GuiTextures.FURNACE_OVERLAY_1)
            .setSlotOverlay(false, false, true, GuiTextures.FURNACE_OVERLAY_1)
            .setSlotOverlay(false, true, true, GuiTextures.TURBINE_OVERLAY)
            .setSlotOverlay(true, false, true, SusyGuiTextures.CUBIC_LATTICE_OVERLAY)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> FLUIDIZED_BED_REACTOR_RECIPES = new RecipeMap<>("fluidized_bed_reactor", 2, 1, 3, 2, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, true, SusyGuiTextures.LARGE_REACTOR_FLUID_OVERLAY)
            .setSlotOverlay(true, true, SusyGuiTextures.LARGE_REACTOR_FLUID_OVERLAY)
            .setSlotOverlay(false, false, SusyGuiTextures.LARGE_REACTOR_ITEM_OVERLAY)
            .setSlotOverlay(true, false, SusyGuiTextures.LARGE_REACTOR_ITEM_OVERLAY)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> POLYMERIZATION_RECIPES = new RecipeMap<>("polymerization_tank", 2, 1, 2, 1, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, true, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSlotOverlay(true, true, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSlotOverlay(false, false, GuiTextures.MOLECULAR_OVERLAY_1)
            .setSlotOverlay(true, false, GuiTextures.MOLECULAR_OVERLAY_1)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> ELECTROLYTIC_CELL_RECIPES = new RecipeMap<>("electrolytic_cell", 3, 3, 2, 4, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, true, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSlotOverlay(true, true, GuiTextures.MOLECULAR_OVERLAY_3)
            .setSlotOverlay(false, false, SusyGuiTextures.ELECTRODE_OVERLAY)
            .setSlotOverlay(true, false, GuiTextures.MOLECULAR_OVERLAY_1)
            .setSound(GTSoundEvents.ELECTROLYZER);

    public static final RecipeMap<SimpleRecipeBuilder> COKING_RECIPES = new RecipeMap<>("coking_tower", 1, 1, 3, 2, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.COMBUSTION);

    public static final RecipeMap<SimpleRecipeBuilder> VACUUM_DISTILLATION_RECIPES = new RecipeMap<>("vacuum_distillation", 0, 0, 1, 1, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> CATALYTIC_REFORMER_RECIPES = new RecipeMap<>("catalytic_reformer_recipes", 1, 0, 2, 4, new SimpleRecipeBuilder(), false)
            .setProgressBar(GuiTextures.PROGRESS_BAR_CRACKING, ProgressWidget.MoveType.HORIZONTAL)
            .setSound(GTSoundEvents.FURNACE);

    public static final RecipeMap<NoEnergyRecipeBuilder> SMOKE_STACK = new RecipeMap<>("smoke_stack", 0, 0, 1, 0, new NoEnergyRecipeBuilder(), false);

    public static final RecipeMap<SimpleRecipeBuilder> FERMENTATION_VAT_RECIPES = new RecipeMap<>("vat_fermentation", 3, 3, 3, 3, new SimpleRecipeBuilder(), false)
            .setSlotOverlay(false, false, true, GuiTextures.DUST_OVERLAY)
            .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
            .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> UV_RECIPES = new RecipeMap<>("uv_light_box", 2, 1, 1, 0, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.ARC);

    public static final RecipeMap<SimpleRecipeBuilder> ION_IMPLANTATION_RECIPES = new RecipeMap<>("ion_implantation", 3, 1, 2, 0, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.ELECTROLYZER);

    public static final RecipeMap<SimpleRecipeBuilder> CVD_RECIPES = new RecipeMap<>("cvd", 3, 1, 2, 0, new SimpleRecipeBuilder(), false)
            .setSound(GTSoundEvents.ARC);

    public static void init(){
        RecipeMaps.SIFTER_RECIPES.setMaxFluidInputs(1);
        RecipeMaps.SIFTER_RECIPES.setMaxFluidOutputs(1);
        RecipeMaps.SIFTER_RECIPES.setMaxInputs(2);
        RecipeMaps.CENTRIFUGE_RECIPES.setMaxFluidInputs(2);
        RecipeMaps.CENTRIFUGE_RECIPES.setSlotOverlay(false, true, false, GuiTextures.CENTRIFUGE_OVERLAY);
        RecipeMaps.MIXER_RECIPES.setMaxFluidInputs(3);
        RecipeMaps.MIXER_RECIPES.setMaxFluidOutputs(2);
        RecipeMaps.ARC_FURNACE_RECIPES.setMaxInputs(2);
        RecipeMaps.ELECTROLYZER_RECIPES.setMaxInputs(4);
        RecipeMaps.ELECTROLYZER_RECIPES.setMaxFluidOutputs(3);
        RecipeMaps.ELECTROLYZER_RECIPES.setMaxOutputs(3);
        RecipeMaps.ELECTROMAGNETIC_SEPARATOR_RECIPES.setMaxFluidOutputs(2);
        RecipeMaps.ELECTROMAGNETIC_SEPARATOR_RECIPES.setMaxFluidInputs(1);
        GTFORecipeMaps.GREENHOUSE_RECIPES.setMaxFluidInputs(4);
        RecipeMaps.PYROLYSE_RECIPES.setMaxFluidOutputs(3);
        RecipeMaps.ELECTROMAGNETIC_SEPARATOR_RECIPES.setSlotOverlay(false, false, SusyGuiTextures.ELECTROMAGNETIC_SEPARATOR_ITEM_OVERLAY);
        RecipeMaps.ELECTROMAGNETIC_SEPARATOR_RECIPES.setSlotOverlay(false, true, SusyGuiTextures.ELECTROMAGNETIC_SEPARATOR_FLUID_OVERLAY);
        RecipeMaps.SIFTER_RECIPES.setSlotOverlay(false, true, SusyGuiTextures.SIFTER_FLUID_OVERLAY);
        RecipeMaps.SIFTER_RECIPES.setSlotOverlay(true, true, SusyGuiTextures.SIFTER_FLUID_OVERLAY);
        RecipeMaps.SIFTER_RECIPES.setSlotOverlay(false, false, SusyGuiTextures.SIFTER_ITEM_INPUT_OVERLAY);
        RecipeMaps.SIFTER_RECIPES.setSlotOverlay(true, false, SusyGuiTextures.SIFTER_ITEM_OUTPUT_OVERLAY);
        RecipeMaps.LASER_ENGRAVER_RECIPES.setMaxFluidInputs(1);
    }
}
