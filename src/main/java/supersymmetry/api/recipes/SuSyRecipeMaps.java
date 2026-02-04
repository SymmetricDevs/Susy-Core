package supersymmetry.api.recipes;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.MIXER_RECIPES;

import net.minecraft.item.ItemStack;

import gregicality.multiblocks.api.recipes.GCYMRecipeMaps;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.FuelRecipeBuilder;
import gregtech.api.recipes.builders.PrimitiveRecipeBuilder;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.core.sound.GTSoundEvents;
import supersymmetry.api.capability.impl.SuSyBoilerLogic;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.recipes.builders.*;
import supersymmetry.common.materials.SusyMaterials;

public class SuSyRecipeMaps {

    public static final RecipeMap<CoilingCoilRecipeBuilder> COOLING_RECIPES = new RecipeMap<>("magnetic_refrigerator",
            3, 3, 0, 1, new CoilingCoilRecipeBuilder(), false)
                    .setSound(GTSoundEvents.COOLING);

    public static final RecipeMap<SinteringRecipeBuilder> SINTERING_RECIPES = new RecipeMap<>("sintering_oven", 4, 4, 2,
            2, new SinteringRecipeBuilder(), false)
                    .setSound(GTSoundEvents.COMBUSTION);

    public static final RecipeMap<PrimitiveRecipeBuilder> COAGULATION_RECIPES = new RecipeMap<>("coagulation_tank", 2,
            1, 2, 0, new PrimitiveRecipeBuilder(), false);

    public static final RecipeMap<PseudoMultiRecipeBuilder> LATEX_COLLECTOR_RECIPES = new RecipeMap<>("latex_collector",
            0, 2, 1, 2, new PseudoMultiRecipeBuilder(), false)
                    .setProgressBar(SusyGuiTextures.PROGRESS_BAR_EXTRACTION, ProgressWidget.MoveType.VERTICAL_DOWNWARDS)
                    .setSound(GTSoundEvents.DRILL_TOOL);

    public static final RecipeMap<CatalystRecipeBuilder> VULCANIZATION_RECIPES = new RecipeMap<>("vulcanizing_press", 4,
            2, 2, 1, new CatalystRecipeBuilder(), false)
                    .setSlotOverlay(false, false, true, GuiTextures.MOLD_OVERLAY)
                    .setSound(GTSoundEvents.COMBUSTION);

    public static final RecipeMap<CatalystRecipeBuilder> ROASTER_RECIPES = new RecipeMap<>("roaster", 3, 2, 2, 3,
            new CatalystRecipeBuilder(), false)
                    .setSound(GTSoundEvents.COMBUSTION);

    public static final RecipeMap<SimpleRecipeBuilder> VACUUM_CHAMBER = new RecipeMap<>("vacuum_chamber", 4, 2, 2, 2,
            new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.CENTRIFUGE);

    public static final RecipeMap<CatalystRecipeBuilder> CSTR_RECIPES = new RecipeMap<>(
            "continuous_stirred_tank_reactor", 1, 0, 4, 2, new CatalystRecipeBuilder(), false)
                    .setSlotOverlay(false, true, false, GuiTextures.MOLECULAR_OVERLAY_3)
                    .setSlotOverlay(false, true, true, GuiTextures.MOLECULAR_OVERLAY_4)
                    .setSlotOverlay(true, true, GuiTextures.VIAL_OVERLAY_2)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<CatalystRecipeBuilder> FIXED_BED_REACTOR_RECIPES = new RecipeMap<>(
            "fixed_bed_reactor", 2, 1, 3, 2, new CatalystRecipeBuilder(), false)
                    .setSlotOverlay(false, false, SusyGuiTextures.CATALYST_BED_OVERLAY)
                    .setSlotOverlay(false, true, GuiTextures.MOLECULAR_OVERLAY_3)
                    .setSlotOverlay(true, true, GuiTextures.MOLECULAR_OVERLAY_3)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<CatalystRecipeBuilder> TRICKLE_BED_REACTOR_RECIPES = new RecipeMap<>(
            "trickle_bed_reactor", 2, 0, 3, 2, new CatalystRecipeBuilder(), false)
                    .setSlotOverlay(false, false, SusyGuiTextures.CATALYST_PELLET_OVERLAY)
                    .setSlotOverlay(false, true, GuiTextures.MOLECULAR_OVERLAY_3)
                    .setSlotOverlay(true, true, GuiTextures.MOLECULAR_OVERLAY_3)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<CatalystRecipeBuilder> BUBBLE_COLUMN_REACTOR_RECIPES = new RecipeMap<>(
            "bubble_column_reactor", 1, 0, 3, 2, new CatalystRecipeBuilder(), false)
                    .setSlotOverlay(false, false, SusyGuiTextures.LARGE_REACTOR_ITEM_OVERLAY)
                    .setSlotOverlay(false, true, GuiTextures.MOLECULAR_OVERLAY_3)
                    .setSlotOverlay(true, true, GuiTextures.MOLECULAR_OVERLAY_3)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<CatalystRecipeBuilder> BATCH_REACTOR_RECIPES = new RecipeMap<>("batch_reactor", 3, 3,
            3, 3, new CatalystRecipeBuilder(), false)
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

    public static final RecipeMap<SimpleRecipeBuilder> CRYSTALLIZER_RECIPES = new RecipeMap<>("crystallizer", 3, 3, 3,
            3, new SimpleRecipeBuilder(), false)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> DRYER_RECIPES = new RecipeMap<>("dryer", 2, 2, 2, 2,
            new SimpleRecipeBuilder(), false)
                    .setSound(GTSoundEvents.COOLING);

    public static final RecipeMap<SimpleRecipeBuilder> ION_EXCHANGE_COLUMN_RECIPES = new RecipeMap<>(
            "ion_exchange_column", 2, 1, 2, 2, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, ProgressWidget.MoveType.VERTICAL)
                    .setSlotOverlay(false, false, true, SusyGuiTextures.CUBIC_LATTICE_OVERLAY)
                    .setSlotOverlay(false, true, true, GuiTextures.MOLECULAR_OVERLAY_4)
                    .setSlotOverlay(true, false, true, SusyGuiTextures.CUBIC_LATTICE_OVERLAY)
                    .setSlotOverlay(true, true, true, GuiTextures.MOLECULAR_OVERLAY_4)
                    .setSound(GTSoundEvents.BATH);

    public static final RecipeMap<SimpleRecipeBuilder> ZONE_REFINER_RECIPES = new RecipeMap<>("zone_refiner", 1, 1, 0,
            0, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_CRYSTALLIZATION, ProgressWidget.MoveType.HORIZONTAL)
                    .setSlotOverlay(false, false, true, GuiTextures.FURNACE_OVERLAY_1)
                    .setSlotOverlay(true, false, true, GuiTextures.CRYSTAL_OVERLAY)
                    .setSound(GTSoundEvents.FURNACE);

    public static final RecipeMap<SimpleRecipeBuilder> TUBE_FURNACE_RECIPES = new RecipeMap<>("tube_furnace", 6, 1, 1,
            1, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSlotOverlay(false, false, false, GuiTextures.FURNACE_OVERLAY_1)
                    .setSlotOverlay(false, false, true, GuiTextures.FURNACE_OVERLAY_1)
                    .setSlotOverlay(false, true, true, GuiTextures.TURBINE_OVERLAY)
                    .setSlotOverlay(true, false, true, SusyGuiTextures.CUBIC_LATTICE_OVERLAY)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> ELECTROSTATIC_SEPARATOR = new RecipeMap<>(
            "electrostatic_separator", 1, 6, 1, 2, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MAGNET, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ARC);

    public static final RecipeMap<SimpleRecipeBuilder> SPINNING_RECIPES = new RecipeMap<>("spinning", 1, 1, 1, 0,
            new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MAGNET, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.CENTRIFUGE);

    public static final RecipeMap<SimpleRecipeBuilder> POLISHING_MACHINE = new RecipeMap<>("polishing_machine", 1, 1, 2,
            1, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, ProgressWidget.MoveType.CIRCULAR)
                    .setSound(GTSoundEvents.CENTRIFUGE);

    public static final RecipeMap<CatalystRecipeBuilder> FLUIDIZED_BED_REACTOR_RECIPES = new RecipeMap<>(
            "fluidized_bed_reactor", 2, 3, 3, 3, new CatalystRecipeBuilder(), false)
                    .setSlotOverlay(false, true, SusyGuiTextures.LARGE_REACTOR_FLUID_OVERLAY)
                    .setSlotOverlay(true, true, SusyGuiTextures.LARGE_REACTOR_FLUID_OVERLAY)
                    .setSlotOverlay(false, false, SusyGuiTextures.LARGE_REACTOR_ITEM_OVERLAY)
                    .setSlotOverlay(true, false, SusyGuiTextures.LARGE_REACTOR_ITEM_OVERLAY)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<CatalystRecipeBuilder> POLYMERIZATION_RECIPES = new RecipeMap<>("polymerization_tank",
            3, 1, 3, 2, new CatalystRecipeBuilder(), false)
                    .setSlotOverlay(false, true, GuiTextures.MOLECULAR_OVERLAY_3)
                    .setSlotOverlay(true, true, GuiTextures.MOLECULAR_OVERLAY_3)
                    .setSlotOverlay(false, false, GuiTextures.MOLECULAR_OVERLAY_1)
                    .setSlotOverlay(true, false, GuiTextures.MOLECULAR_OVERLAY_1)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> ELECTROLYTIC_CELL_RECIPES = new RecipeMap<>("electrolytic_cell",
            3, 3, 3, 4, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, true, GuiTextures.MOLECULAR_OVERLAY_3)
                    .setSlotOverlay(true, true, GuiTextures.MOLECULAR_OVERLAY_3)
                    .setSlotOverlay(false, false, SusyGuiTextures.ELECTRODE_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.MOLECULAR_OVERLAY_1)
                    .setSound(GTSoundEvents.ELECTROLYZER);

    public static final RecipeMap<SimpleRecipeBuilder> COKING_RECIPES = new RecipeMap<>("coking_tower", 1, 1, 3, 2,
            new SimpleRecipeBuilder(), false)
                    .setSound(GTSoundEvents.COMBUSTION);

    public static final RecipeMap<SimpleRecipeBuilder> VACUUM_DISTILLATION_RECIPES = new RecipeMap<>(
            "vacuum_distillation", 1, true, 1, true, 2, true, 12, false, new SimpleRecipeBuilder(), false)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> SIEVE_DISTILLATION_RECIPES = new RecipeMap<>(
            "sieve_distillation", 1, true, 1, true, 2, true, 12, false, new SimpleRecipeBuilder(), false)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<CatalystRecipeBuilder> CATALYTIC_REFORMER_RECIPES = new RecipeMap<>(
            "catalytic_reformer_recipes", 1, 0, 2, 4, new CatalystRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_CRACKING, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.FURNACE);
    public static final RecipeMap<SimpleRecipeBuilder> FERMENTATION_VAT_RECIPES = new RecipeMap<>("vat_fermentation", 3,
            3, 3, 3, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, true, GuiTextures.DUST_OVERLAY)
                    .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> UV_RECIPES = new RecipeMap<>("uv_light_box", 2, 1, 1, 1,
            new SimpleRecipeBuilder(), false)
                    .setSound(GTSoundEvents.ARC);

    public static final RecipeMap<SimpleRecipeBuilder> ION_IMPLANTATION_RECIPES = new RecipeMap<>("ion_implantation", 3,
            1, 2, 0, new SimpleRecipeBuilder(), false)
                    .setSound(GTSoundEvents.ELECTROLYZER);

    public static final RecipeMap<SimpleRecipeBuilder> CVD_RECIPES = new RecipeMap<>("cvd", 3, 1, 3, 2,
            new SimpleRecipeBuilder(), false)
                    .setSound(GTSoundEvents.ARC);

    public static final RecipeMap<SimpleRecipeBuilder> SPUTTER_DEPOSITION_RECIPES = new RecipeMap<>(
            "sputter_deposition", 6, 1, 2, 2, new SimpleRecipeBuilder(), false)
                    .setSound(GTSoundEvents.ELECTROLYZER);

    public static final RecipeMap<SimpleRecipeBuilder> ORE_SORTER_RECIPES = new RecipeMap<>("ore_sorter", 2, 20, 1, 1,
            new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.MACERATOR);

    public static final RecipeMap<SimpleRecipeBuilder> NATURAL_DRAFT_COOLING_TOWER = new RecipeMap<>(
            "natural_draft_cooling_tower", 1, 0, 1, 1, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COOLING);

    public static final RecipeMap<SimpleRecipeBuilder> FLUID_DECOMPRESSOR_RECIPES = new RecipeMap<>(
            "fluid_decompressor", 1, 0, 2, 2, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COMPRESSOR);

    public static final RecipeMap<SimpleRecipeBuilder> FLUID_COMPRESSOR_RECIPES = new RecipeMap<>("fluid_compressor", 2,
            0, 2, 2, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COMPRESSOR);

    public static final RecipeMap<NoEnergyRecipeBuilder> HEAT_EXCHANGER_RECIPES = new RecipeMap<>("heat_exchanger", 1,
            0, 2, 2, new NoEnergyRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COOLING);

    public static final RecipeMap<NoEnergyRecipeBuilder> CONDENSER_RECIPES = new RecipeMap<>("condenser", 0, 0, 2, 2,
            new NoEnergyRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COOLING);

    public static final RecipeMap<NoEnergyRecipeBuilder> HEAT_RADIATOR_RECIPES = new RecipeMap<>("radiator", 1, 0, 1, 1,
            new NoEnergyRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COOLING);

    public static final RecipeMap<SimpleRecipeBuilder> QUENCHER_RECIPES = new RecipeMap<>("quencher", 2, 1, 2, 1,
            new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COOLING);

    public static final RecipeMap<SimpleRecipeBuilder> WEAPONS_FACTORY_RECIPES = new RecipeMap<>("weapons_factory", 9,
            1, 2, 0, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_CIRCUIT, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ASSEMBLER);

    public static final RecipeMap<SimpleRecipeBuilder> LARGE_WEAPONS_FACTORY_RECIPES = new RecipeMap<>(
            "large_weapons_factory", 9, 1, 3, 0, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_CIRCUIT, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ASSEMBLER);

    public static final RecipeMap<SimpleRecipeBuilder> RAILROAD_ENGINEERING_STATION_RECIPES = new RecipeMap<>(
            "railroad_engineering_station", 16, 1, 4, 0, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ASSEMBLER);

    public static final RecipeMap<SimpleRecipeBuilder> MINING_DRILL_RECIPES = new RecipeMap<>("mining_drill", 1, 1, 1,
            1, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, true, GuiTextures.CRUSHED_ORE_OVERLAY)
                    .setSlotOverlay(true, false, true, GuiTextures.DUST_OVERLAY)
                    .setSound(GTSoundEvents.MACERATOR);

    public static final RecipeMap<SimpleRecipeBuilder> GRAVITY_SEPARATOR_RECIPES = new RecipeMap<>("gravity_separator",
            1, 6, 1, 3, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MACERATE, ProgressWidget.MoveType.VERTICAL)
                    .setSlotOverlay(false, false, true, SusyGuiTextures.ORE_CHUNK_OVERLAY)
                    .setSlotOverlay(false, true, false, SusyGuiTextures.SIFTER_FLUID_OVERLAY)
                    .setSlotOverlay(false, true, true, SusyGuiTextures.SIFTER_FLUID_OVERLAY)
                    .setSlotOverlay(true, false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
                    .setSlotOverlay(true, false, true, GuiTextures.CRUSHED_ORE_OVERLAY)
                    .setSlotOverlay(true, true, false, SusyGuiTextures.SIFTER_FLUID_OVERLAY)
                    .setSlotOverlay(true, true, true, SusyGuiTextures.SIFTER_FLUID_OVERLAY)
                    .setSound(GTSoundEvents.MACERATOR);

    public static final RecipeMap<SimpleRecipeBuilder> REACTION_FURNACE_RECIPES = new RecipeMap<>("reaction_furnace", 3,
            3, 3, 3, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ARC);

    public static final RecipeMap<SimpleRecipeBuilder> PRESSURE_SWING_ADSORBER_RECIPES = new RecipeMap<>(
            "pressure_swing_adsorption", 1, 1, 2, 2, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, ProgressWidget.MoveType.VERTICAL)
                    .setSound(GTSoundEvents.COMPRESSOR);

    public static final RecipeMap<FuelRecipeBuilder> MAGNETOHYDRODYNAMIC_FUELS = new RecipeMap<>(
            "magnetohydrodynamic_generator", 0, 0, 1, 1, new FuelRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_FUSION, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ARC);

    public static final RecipeMap<SimpleRecipeBuilder> ADVANCED_ARC_FURNACE = new RecipeMap<>("advanced_arc_furnace", 9,
            2, 4, 1, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ARC);

    public static final RecipeMap<EvaporationPoolRecipeBuilder> EVAPORATION_POOL = new RecipeMap<>("evaporation_pool",
            2, 4, 1, 1, new EvaporationPoolRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, ProgressWidget.MoveType.VERTICAL)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> CLARIFIER = new RecipeMap<>("clarifier", 2, 2, 2, 2,
            new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, ProgressWidget.MoveType.CIRCULAR)
                    .setSound(GTSoundEvents.CENTRIFUGE);

    public static final RecipeMap<SimpleRecipeBuilder> MULTI_STAGE_FLASH_DISTILLATION = new RecipeMap<>(
            "multi_stage_flash_distillation", 1, 0, 3, 3, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COOLING);

    public static final RecipeMap<SimpleRecipeBuilder> FROTH_FLOTATION = new RecipeMap<>("froth_flotation", 3, 2, 4, 2,
            new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MIXER, ProgressWidget.MoveType.CIRCULAR)
                    .setSound(GTSoundEvents.BATH);

    public static final RecipeMap<SimpleRecipeBuilder> HIGH_TEMPERATURE_DISTILLATION = new RecipeMap<>(
            "high_temperature_distillation", 1, 1, 1, 12, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, ProgressWidget.MoveType.VERTICAL_DOWNWARDS)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR);

    public static final RecipeMap<SimpleRecipeBuilder> IN_SITU_LEACHER = new RecipeMap<>("in_situ_leacher", 2, 2, 2, 2,
            new SimpleRecipeBuilder(), false)
                    .setSound(GTSoundEvents.COMPRESSOR);

    public static final RecipeMap<SimpleRecipeBuilder> EUV_LITHOGRAPHY = new RecipeMap<>("euv_lithography", 3, 3, 3, 3,
            new SimpleRecipeBuilder(), false)
                    .setSound(GTSoundEvents.ELECTROLYZER);

    public static final RecipeMap<SimpleRecipeBuilder> ROTARY_KILN = new RecipeMap<>("rotary_kiln", 3, 2, 3, 3,
            new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.MACERATOR);

    public static final RecipeMap<SimpleRecipeBuilder> HIGH_PRESSURE_CRYOGENIC_DISTILLATION = new RecipeMap<>(
            "high_pressure_cryogenic_distillation", 1, 0, 9, 9, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, ProgressWidget.MoveType.VERTICAL_DOWNWARDS)
                    .setSound(GTSoundEvents.COOLING);

    public static final RecipeMap<SimpleRecipeBuilder> LOW_PRESSURE_CRYOGENIC_DISTILLATION = new RecipeMap<>(
            "low_pressure_cryogenic_distillation", 1, 0, 9, 9, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, ProgressWidget.MoveType.VERTICAL)
                    .setSound(GTSoundEvents.COOLING);

    public static final RecipeMap<SimpleRecipeBuilder> SINGLE_COLUMN_CRYOGENIC_DISTILLATION = new RecipeMap<>(
            "single_column_cryogenic_distillation", 1, 0, 9, 9, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, ProgressWidget.MoveType.VERTICAL)
                    .setSound(GTSoundEvents.COOLING);

    public static final RecipeMap<NoEnergyRecipeBuilder> REVERBERATORY_FURNACE = new RecipeMap<>(
            "reverberatory_furnace", 3, 3, 3, 3, new NoEnergyRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.FURNACE);

    public static final RecipeMap<PrimitiveRecipeBuilder> PHASE_SEPARATOR = new RecipeMap<>("phase_separator", 0, 1, 2,
            3, new PrimitiveRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.BATH);

    public static final RecipeMap<BathCondenserRecipeBuilder> BATH_CONDENSER = new RecipeMap<>("bath_condenser", 0, 0,
            2, 3, new BathCondenserRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_SIFT, ProgressWidget.MoveType.VERTICAL)
                    .setSound(GTSoundEvents.BATH);

    public static final RecipeMap<BiomeRecipeBuilder> PUMPING_RECIPES = new RecipeMap<>("large_fluid_pump", 1, 0, 0, 1,
            new BiomeRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.MINER);

    public static final RecipeMap<DimensionRecipeBuilder> DRONE_PAD = new RecipeMap<>("drone_pad", 4, 9, 0, 0,
            new DimensionRecipeBuilder().minimumDuration(800), false);

    public static final RecipeMap<SimpleRecipeBuilder> BLENDER_RECIPES = new RecipeMap<>("blender", 9, 1, 6, 2,
            new SimpleRecipeBuilder().EUt(VA[LV]), false)
                    .setSlotOverlay(false, false, false, GuiTextures.MOLECULAR_OVERLAY_1)
                    .setSlotOverlay(false, false, true, GuiTextures.MOLECULAR_OVERLAY_2)
                    .setSlotOverlay(false, true, false, GuiTextures.MOLECULAR_OVERLAY_3)
                    .setSlotOverlay(false, true, true, GuiTextures.MOLECULAR_OVERLAY_4)
                    .setSlotOverlay(true, false, GuiTextures.VIAL_OVERLAY_1)
                    .setSlotOverlay(true, true, GuiTextures.VIAL_OVERLAY_2)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.CHEMICAL_REACTOR)
                    .setSmallRecipeMap(MIXER_RECIPES);

    public static final RecipeMap<FuelRecipeBuilder> LARGE_STEAM_TURBINE = new RecipeMap<>("large_steam_turbine", 0, 0,
            1, 1, new FuelRecipeBuilder(), false)
                    .setSlotOverlay(false, true, GuiTextures.CENTRIFUGE_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.TURBINE)
                    .allowEmptyOutput();

    public static final RecipeMap<FuelRecipeBuilder> ADVANCED_STEAM_TURBINE = new RecipeMap<>(
            "advanced_steam_turbine", 0, 0, 1, 1, new FuelRecipeBuilder(), false)
                    .setSlotOverlay(false, true, GuiTextures.CENTRIFUGE_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.TURBINE)
                    .allowEmptyOutput();

    public static final RecipeMap<PrimitiveRecipeBuilder> PRIMITIVE_SMELTER = new RecipeMap<>("primitive_smelter", 4, 2,
            0, 0, new PrimitiveRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.FURNACE_OVERLAY_1)
                    .setSlotOverlay(true, false, GuiTextures.FURNACE_OVERLAY_2)
                    .setSound(GTSoundEvents.FURNACE);

    public static final RecipeMap<SimpleRecipeBuilder> SCRAP_RECYCLER = new RecipeMap<>("scrap_recycler", 1, 9, 0, 0,
            new SimpleRecipeBuilder(), false)
                    .setSound(GTSoundEvents.ASSEMBLER);

    public static final RecipeMap<SimpleRecipeBuilder> ROCKET_ASSEMBLER = new RecipeMap<>("rocket_assembler", 25, 2, 5,
            0, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ASSEMBLER);

    public static final RecipeMap<NoEnergyRecipeBuilder> JET_WINGPACK_FUELS = new RecipeMap<>("jet_wingpack_fuels", 0,
            0, 1, 0, new NoEnergyRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.DARK_CANISTER_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_GAS_COLLECTOR, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.TURBINE)
                    .allowEmptyOutput();

    public static final RecipeMap<SimpleRecipeBuilder> HOT_ISOSTATIC_PRESS = new RecipeMap<>("hot_isostatic_press", 3,
            1, 1, 0, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_COMPRESS, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.COMPRESSOR);

    public static final RecipeMap<SimpleRecipeBuilder> GAS_ATOMIZER = new RecipeMap<>("gas_atomizer", 1, 1, 1, 0,
            new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MACERATE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.FURNACE); // TODO: Replace with a sound like a pump hissing

    public static final RecipeMap<SimpleRecipeBuilder> CURTAIN_COATER = new RecipeMap<>("curtain_coater", 1, 1, 1, 1,
            new SimpleRecipeBuilder().EUt(VA[LV]), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.BATH);

    public static final RecipeMap<SimpleRecipeBuilder> MILLING_RECIPES = new RecipeMap<>("milling", 2, 1, 0, 0,
            new SimpleRecipeBuilder().EUt(VA[LV]), false)
                    .setSound(GTSoundEvents.CUT);

    public static final RecipeMap<MixerSettlerRecipeBuilder> MIXER_SETTLER_RECIPES = new RecipeMap<>("mixer_settler", 2,
            2, 3, 3, new MixerSettlerRecipeBuilder().EUt(VA[LV]), false)
                    .setSound(GTSoundEvents.MIXER);

    public static final RecipeMap<DimensionRecipeBuilder> QUARRY_RECIPES = new RecipeMap<>("quarry", 1, 9, 0, 0,
            new DimensionRecipeBuilder(), false)
                    .setSound(GTSoundEvents.MINER);

    public static final RecipeMap<SimpleRecipeBuilder> METALLURGICAL_CONVERTER = new RecipeMap<>(
            "metallurgical_converter", 3, 2, 3, 2, new SimpleRecipeBuilder().EUt(VA[LV]), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.FURNACE);

    public static final RecipeMap<SimpleRecipeBuilder> ECCENTRIC_ROLL_CRUSHER = new RecipeMap<>(
            "eccentric_roll_crusher", 1, 4, 0, 0, new SimpleRecipeBuilder(), false)
                    .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.CRUSHED_ORE_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MACERATE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.MACERATOR);

    public static final RecipeMap<SimpleRecipeBuilder> BALL_MILL = new RecipeMap<>("ball_mill", 1, 4, 1, 0,
            new SimpleRecipeBuilder().EUt(VA[LV]), false)
                    .setSlotOverlay(false, false, GuiTextures.CRUSHED_ORE_OVERLAY)
                    .setSlotOverlay(true, false, GuiTextures.DUST_OVERLAY)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_MACERATE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.MACERATOR);

    public static final RecipeMap<SimpleRecipeBuilder> INJECTION_MOLDER = new RecipeMap<>("injection_molder", 2, 1, 0,
            0, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.MIXER);

    public static final RecipeMap<FuelRecipeBuilder> BOILER_RECIPES = new RecipeMap<>("boiler", 1, 0, 0, 0,
            new FuelRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL)
                    .setSlotOverlay(false, false, GuiTextures.FURNACE_OVERLAY_1)
                    .setSound(GTSoundEvents.BOILER)
                    .allowEmptyOutput();

    public static final RecipeMap<FuelRecipeBuilder> FUEL_CELL_RECIPES = new RecipeMap<>("fuel_cell", 0, 0, 2, 0,
            new FuelRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_HAMMER, ProgressWidget.MoveType.VERTICAL)
                    .setSound(GTSoundEvents.ELECTROLYZER)
                    .allowEmptyOutput();

    public static final RecipeMap<SimpleRecipeBuilder> INDUCTION_FURNACE = new RecipeMap<>(
            "induction_furnace", 6, 3, 3, 3, new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_ARC_FURNACE, ProgressWidget.MoveType.HORIZONTAL)
                    .setSound(GTSoundEvents.ARC);

    public static final RecipeMap<SimpleRecipeBuilder> RESISTANCE_FURNACE = new RecipeMap<>("resistance_furnace",
            6, 2, 0, 1, new SimpleRecipeBuilder(), false)
                    .setSound(GTSoundEvents.FURNACE);

    public static final RecipeMap<SimpleRecipeBuilder> SALVAGING_RECIPES = new RecipeMap<>("salvaging", 1, 9, 0, 0,
            new SimpleRecipeBuilder(), false)
                    .setProgressBar(GuiTextures.PROGRESS_BAR_RECYCLER, ProgressWidget.MoveType.HORIZONTAL)
                    .setSlotOverlay(true, false, GuiTextures.BOXED_OVERLAY);

    static {
        GCYMRecipeMaps.ALLOY_BLAST_RECIPES.onRecipeBuild(recipeBuilder -> ADVANCED_ARC_FURNACE.recipeBuilder()
                .fluidInputs(SusyMaterials.RefractoryGunningMixture.getFluid(50 *
                        Math.max(1, (recipeBuilder.getDuration() - 800) / 400) *
                        Math.max(1, (recipeBuilder.getBlastFurnaceTemp() - 1800) / 1800)))
                .inputs(recipeBuilder.getInputs().toArray(new GTRecipeInput[0]))
                .fluidInputs(recipeBuilder.getFluidInputs())
                .outputs(recipeBuilder.getOutputs())
                .chancedOutputs(recipeBuilder.getChancedOutputs())
                .fluidOutputs(recipeBuilder.getFluidOutputs())
                .chancedFluidOutputs(recipeBuilder.getChancedFluidOutputs())
                .cleanroom(recipeBuilder.getCleanroom())
                .duration(recipeBuilder.getDuration() / 4)
                .EUt(recipeBuilder.getEUt())
                .buildAndRegister());

        SuSyRecipeMaps.ADVANCED_ARC_FURNACE.onRecipeBuild(recipeBuilder -> {
            for (var fluidInput : recipeBuilder.getFluidInputs()) {
                if (fluidInput.getInputFluidStack().getFluid() == SusyMaterials.RefractoryGunningMixture.getFluid()) {
                    return;
                }
            }
            recipeBuilder.fluidInputs(SusyMaterials.RefractoryGunningMixture.getFluid(50));
        });

        SuSyRecipeMaps.METALLURGICAL_CONVERTER.onRecipeBuild(
                recipeBuilder -> recipeBuilder.fluidInputs(SusyMaterials.RefractoryGunningMixture.getFluid(50)));

        MIXER_RECIPES.onRecipeBuild(recipeBuilder -> SuSyRecipeMaps.BLENDER_RECIPES.recipeBuilder()
                .inputs(recipeBuilder.getInputs().toArray(new GTRecipeInput[0]))
                .fluidInputs(recipeBuilder.getFluidInputs())
                .outputs(recipeBuilder.getOutputs())
                .chancedOutputs(recipeBuilder.getChancedOutputs())
                .fluidOutputs(recipeBuilder.getFluidOutputs())
                .chancedFluidOutputs(recipeBuilder.getChancedFluidOutputs())
                .cleanroom(recipeBuilder.getCleanroom())
                .duration(recipeBuilder.getDuration())
                .EUt(recipeBuilder.getEUt())
                .buildAndRegister());

        BOILER_RECIPES.onRecipeBuild(recipeBuilder -> {
            ItemStack input = recipeBuilder.getInputs().get(0).getInputStacks()[0];
            if (OreDictUnifier.getPrefix(input) != OrePrefix.dust) {
                return;
            }
            MaterialStack matStack = OreDictUnifier.getMaterial(input);
            if (matStack == null) return;
            Material mat = matStack.material;
            for (OrePrefix prefix : SuSyBoilerLogic.SUPPORTED_ORE_PREFIXES) {
                if (prefix == OrePrefix.dust) {
                    continue;
                }
                ItemStack otherInput = OreDictUnifier.get(prefix, mat);
                if (!otherInput.isEmpty()) {
                    int duration = (int) (recipeBuilder.getDuration() * prefix.getMaterialAmount(mat) / GTValues.M);
                    if (duration == 0) { // special cases where material amount is -1 (not set)s
                        if (prefix == OrePrefix.log) {
                            duration = recipeBuilder.getDuration() * 4;
                        }
                        if (prefix == OrePrefix.plank) {
                            duration = recipeBuilder.getDuration();
                        }
                    }
                    if (duration > 0) {
                        BOILER_RECIPES.recipeBuilder()
                                .inputs(otherInput)
                                .duration(duration)
                                .EUt((int) V[LV])
                                .buildAndRegister();
                    }
                }
            }
        });

        SuSyRecipeMaps.BALL_MILL.onRecipeBuild(recipeBuilder -> recipeBuilder
                .fluidInputs(SusyMaterials.PreheatedAir
                        .getFluid(recipeBuilder.getDuration() * recipeBuilder.getEUt() / 512)));
    }
}
