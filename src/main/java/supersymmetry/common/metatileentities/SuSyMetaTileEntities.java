package supersymmetry.common.metatileentities;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.PropertyFluidFilter;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityEnergyHatch;
import gregtech.common.metatileentities.storage.MetaTileEntityDrum;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.CatalystMachineMetaTileEntity;
import supersymmetry.api.metatileentity.ContinuousMachineMetaTileEntity;
import supersymmetry.api.metatileentity.PseudoMultiMachineMetaTileEntity;
import supersymmetry.api.metatileentity.PseudoMultiSteamMachineMetaTileEntity;
import supersymmetry.api.metatileentity.steam.SuSySteamProgressIndicator;
import supersymmetry.api.metatileentity.steam.SuSySteamProgressIndicators;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.util.SuSyUtility;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.metatileentities.multi.electric.*;
import supersymmetry.common.metatileentities.multi.primitive.MetaTileEntityCoagulationTank;
import supersymmetry.common.metatileentities.multi.primitive.MetaTileEntityPrimitiveMudPump;
import supersymmetry.common.metatileentities.multi.primitive.MetaTileEntityPrimitiveSmelter;
import supersymmetry.common.metatileentities.multiblockpart.MetaTileEntityPrimitiveItemBus;
import supersymmetry.common.metatileentities.multiblockpart.SusyMetaTileEntityDumpingHatch;
import supersymmetry.common.metatileentities.multiblockpart.SusyMetaTileEntityEnergyHatch;
import supersymmetry.common.metatileentities.single.electric.MetaTileEntityBathCondenser;
import supersymmetry.common.metatileentities.single.electric.MetaTileEntityLatexCollector;
import supersymmetry.common.metatileentities.single.electric.MetaTileEntityPhaseSeparator;
import supersymmetry.common.metatileentities.single.steam.MetaTileEntitySteamLatexCollector;
import supersymmetry.common.metatileentities.single.steam.SuSySimpleSteamMetaTileEntity;

import java.util.function.Function;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class SuSyMetaTileEntities {

    public static MetaTileEntityMagneticRefrigerator MAGNETIC_REFRIGERATOR;

    public static MetaTileEntityCoagulationTank COAGULATION_TANK;

    public static PseudoMultiMachineMetaTileEntity[] LATEX_COLLECTOR;
    public static PseudoMultiSteamMachineMetaTileEntity[] STEAM_LATEX_COLLECTOR;

    public static CatalystMachineMetaTileEntity[] VULCANIZING_PRESS;
    public static SuSySimpleSteamMetaTileEntity[] STEAM_VULCANIZING_PRESS;

    public static CatalystMachineMetaTileEntity[] ROASTER;
    public static SuSySimpleSteamMetaTileEntity[] STEAM_ROASTER;

    public static MetaTileEntitySinteringOven SINTERING_OVEN;

    public static SuSySimpleSteamMetaTileEntity[] STEAM_MIXER;

    public static SuSySimpleSteamMetaTileEntity[] STEAM_VACUUM_CHAMBER;
    public static SimpleMachineMetaTileEntity[] VACUUM_CHAMBER;

    public static MetaTileEntityDrum LEAD_DRUM;
    public static MetaTileEntityDrum BRASS_DRUM;

    //Machines for chem overhaul
    public static ContinuousMachineMetaTileEntity[] CONTINUOUS_STIRRED_TANK_REACTOR;
    public static ContinuousMachineMetaTileEntity[] FIXED_BED_REACTOR;
    public static ContinuousMachineMetaTileEntity[] TRICKLE_BED_REACTOR;
    public static ContinuousMachineMetaTileEntity[] BUBBLE_COLUMN_REACTOR;
    public static SimpleMachineMetaTileEntity[] BATCH_REACTOR;
    public static SimpleMachineMetaTileEntity[] CRYSTALLIZER;
    public static SimpleMachineMetaTileEntity[] DRYER;
    public static SimpleMachineMetaTileEntity[] ION_EXCHANGE_COLUMN;
    public static SimpleMachineMetaTileEntity[] ZONE_REFINER;
    public static SimpleMachineMetaTileEntity[] TUBE_FURNACE;
    public static MetaTileEntityFluidizedBedReactor FLUIDIZED_BED_REACTOR;
    public static MetaTileEntityPolmyerizationTank POLYMERIZATION_TANK;
    public static MetaTileEntityElectrolyticCell ELECTROLYTIC_CELL;

    // Machines for Oil Overhaul
    public static MetaTileEntityCokingTower COKING_TOWER;
    public static MetaTileEntityVacuumDistillationTower VACUUM_DISTILLATION_TOWER;
    public static MetaTileEntityCatalyticReformer CATALYTIC_REFORMER;
    public static MetaTileEntitySmokeStack SMOKE_STACK;

    public static MetaTileEntityFermentationVat FERMENTATION_VAT;

    public static SimpleMachineMetaTileEntity[] UV_LIGHT_BOX;

    public static SimpleMachineMetaTileEntity[] ION_IMPLANTER;
    public static SimpleMachineMetaTileEntity[] CVD;

    public static SimpleMachineMetaTileEntity[] WEAPONS_FACTORY;

    public static SimpleMachineMetaTileEntity[] FLUID_DECOMPRESSOR;

    public static SimpleMachineMetaTileEntity[] FLUID_COMPRESSOR;

    public static MetaTileEntityOreSorter ORE_SORTER;
    public static MetaTileEntityCondenser CONDENSER;
    public static MetaTileEntityCoolingUnit COOLING_UNIT;
    public static MetaTileEntitySUSYLargeTurbine BASIC_GAS_TURBINE;
    public static MetaTileEntitySUSYLargeTurbine BASIC_STEAM_TURBINE;
    public static MetaTileEntityHeatExchanger HEAT_EXCHANGER;
    public static MetaTileEntityHeatRadiator HEAT_RADIATOR;
    public static MetaTileEntityLargeWeaponsFactory LARGE_WEAPONS_FACTORY;
    public static MetaTileEntityMagnetohydrodynamicGenerator MAGNETOHYDRODYNAMIC_GENERATOR;
    public static MetaTileEntityMiningDrill MINING_DRILL;
    public static MetaTileEntityGravitySeparator GRAVITY_SEPARATOR;
    public static MetaTileEntityQuencher QUENCHER;
    public static MetaTileEntityRailroadEngineeringStation RAILROAD_ENGINEERING_STATION;

    public static MetaTileEntityEnergyHatch[] NEW_ENERGY_OUTPUT_HATCH_4A = new MetaTileEntityEnergyHatch[3];
    public static MetaTileEntityEnergyHatch[] NEW_ENERGY_OUTPUT_HATCH_16A = new MetaTileEntityEnergyHatch[4];

    public static MetaTileEntityPrimitiveMudPump PRIMITIVE_MUD_PUMP;

    public static MetaTileEntityPressureSwingAdsorber PRESSURE_SWING_ADSORBER;
    public static MetaTileEntityReactionFurnace REACTION_FURNACE;
    public static MetaTileEntityDronePad DRONE_PAD;

    public static SusyMetaTileEntityDumpingHatch DUMPING_HATCH;

    public static MetaTileEntityAdvancedArcFurnace ADVANCED_ARC_FURNACE;
    public static MetaTileEntityClarifier CLARIFIER;
    public static MetaTileEntityDumper DUMPER;
    public static MetaTileEntityEvaporationPool EVAPORATION_POOL;
    public static int EVAPORATION_POOL_ID;
    public static MetaTileEntityFlareStack FLARE_STACK;
    public static MetaTileEntityFrothFlotationTank FROTH_FLOTATION_TANK;
    public static MetaTileEntityMultiStageFlashDistiller MULTI_STAGE_FLASH_DISTILLER;

    public static MetaTileEntityOceanPumper OCEAN_PUMPER;
    public static MetaTileEntityHighTemperatureDistillationTower HIGH_TEMPERATURE_DISTILLATION_TOWER;
    public static MetaTileEntityRotaryKiln ROTARY_KILN;
    public static MetaTileEntityHighPressureCryogenicDistillationPlant HIGH_PRESSURE_CRYOGENIC_DISTILLATION_PLANT;
    public static MetaTileEntityLowPressureCryogenicDistillationPlant LOW_PRESSURE_CRYOGENIC_DISTILLATION_PLANT;
    public static MetaTileEntitySingleColumnCryogenicDistillationPlant SINGLE_COLUMN_CRYOGENIC_DISTILLATION_PLANT;
    public static MetaTileEntityReverberatoryFurnace REVERBERATORY_FURNACE;
    public static MetaTileEntityBlender BLENDER;
    public static SimpleMachineMetaTileEntity[] PHASE_SEPARATOR;
    public static SimpleMachineMetaTileEntity[] BATH_CONDENSER;
    public static SimpleMachineMetaTileEntity[] ELECTROSTATIC_SEPARATOR;
    public static SimpleMachineMetaTileEntity[] TEXTILE_SPINNER;
    public static SimpleMachineMetaTileEntity[] POLISHING_MACHINE;

    public static MetaTileEntityPrimitiveSmelter PRIMITIVE_SMELTER;
    public static MetaTileEntityPrimitiveItemBus PRIMITIVE_ITEM_IMPORT;
    public static MetaTileEntityPrimitiveItemBus PRIMITIVE_ITEM_EXPORT;

    public static void init() {
        MAGNETIC_REFRIGERATOR = registerMetaTileEntity(14500, new MetaTileEntityMagneticRefrigerator(susyId("magnetic_refrigerator")));
        COAGULATION_TANK = registerMetaTileEntity(14501, new MetaTileEntityCoagulationTank(susyId("coagulation_tank")));

        LATEX_COLLECTOR[0] = registerMetaTileEntity(14502, new MetaTileEntityLatexCollector(susyId("latex_collector.lv"), 1));
        LATEX_COLLECTOR[1] = registerMetaTileEntity(14503, new MetaTileEntityLatexCollector(susyId("latex_collector.mv"), 2));
        LATEX_COLLECTOR[2] = registerMetaTileEntity(14504, new MetaTileEntityLatexCollector(susyId("latex_collector.hv"), 3));
        LATEX_COLLECTOR[3] = registerMetaTileEntity(14505, new MetaTileEntityLatexCollector(susyId("latex_collector.ev"), 4));

        STEAM_LATEX_COLLECTOR[0] = registerMetaTileEntity(14510, new MetaTileEntitySteamLatexCollector(susyId("latex_collector.bronze"), false));
        STEAM_LATEX_COLLECTOR[1] = registerMetaTileEntity(14511, new MetaTileEntitySteamLatexCollector(susyId("latex_collector.steel"), true));

        SINTERING_OVEN = registerMetaTileEntity(14521, new MetaTileEntitySinteringOven(susyId("sintering_oven")));

        registerSimpleSteamMTE(STEAM_VULCANIZING_PRESS, 14515, "vulcanizing_press", SuSyRecipeMaps.VULCANIZATION_RECIPES, SuSySteamProgressIndicators.COMPRESS, SusyTextures.VULCANIZING_PRESS_OVERLAY, true);
        registerCatalystMTE(VULCANIZING_PRESS, 3, 14517, "vulcanizing_press", SuSyRecipeMaps.VULCANIZATION_RECIPES, SusyTextures.VULCANIZING_PRESS_OVERLAY, true);

        registerSimpleSteamMTE(STEAM_ROASTER, 14679, "roaster", SuSyRecipeMaps.ROASTER_RECIPES, SuSySteamProgressIndicators.ARROW, SusyTextures.ROASTER_OVERLAY, true);
        registerCatalystMTE(ROASTER, 12, 14523, "roaster", SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, true, SuSyUtility.bulkTankSizeFunction);

        registerSimpleSteamMTE(STEAM_MIXER, 14536, "mixer", RecipeMaps.MIXER_RECIPES, SuSySteamProgressIndicators.MIXER, SusyTextures.MIXER_OVERLAY_STEAM, false);

        registerSimpleSteamMTE(STEAM_VACUUM_CHAMBER, 14538, "vacuum_chamber", SuSyRecipeMaps.VACUUM_CHAMBER, SuSySteamProgressIndicators.COMPRESS, Textures.GAS_COLLECTOR_OVERLAY, false);
        registerSimpleMTE(VACUUM_CHAMBER, 12, 14540, "vacuum_chamber", SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, true);

        //chem overhaul
        registerContinuousMachineMTE(CONTINUOUS_STIRRED_TANK_REACTOR, 12, 14554, "continuous_stirred_tank_reactor", SuSyRecipeMaps.CSTR_RECIPES, SusyTextures.CONTINUOUS_STIRRED_TANK_REACTOR_OVERLAY, true, SuSyUtility.reactorTankSizeFunction);
        registerContinuousMachineMTE(FIXED_BED_REACTOR, 12, 14567, "fixed_bed_reactor", SuSyRecipeMaps.FIXED_BED_REACTOR_RECIPES, SusyTextures.FIXED_BED_REACTOR_OVERLAY, true, SuSyUtility.reactorTankSizeFunction);
        registerContinuousMachineMTE(TRICKLE_BED_REACTOR, 12, 14580, "trickle_bed_reactor", SuSyRecipeMaps.TRICKLE_BED_REACTOR_RECIPES, SusyTextures.TRICKLE_BED_REACTOR_OVERLAY, true, SuSyUtility.reactorTankSizeFunction);
        registerContinuousMachineMTE(BUBBLE_COLUMN_REACTOR, 12, 14606, "bubble_column_reactor", SuSyRecipeMaps.BUBBLE_COLUMN_REACTOR_RECIPES, SusyTextures.BUBBLE_COLUMN_REACTOR_OVERLAY, true, SuSyUtility.reactorTankSizeFunction);

        registerSimpleMTE(BATCH_REACTOR, 12, 14681, "batch_reactor", SuSyRecipeMaps.BATCH_REACTOR_RECIPES, SusyTextures.BATCH_REACTOR_OVERLAY, true, SuSyUtility.reactorTankSizeFunction);

        //max tier = 12 -> OpV [excludes ULv] -> 13 ids taken (add maxTier +1 to start ID to get next valid id)
        registerSimpleMTE(CRYSTALLIZER, 12, 14593, "crystallizer", SuSyRecipeMaps.CRYSTALLIZER_RECIPES, SusyTextures.CRYSTALLIZER_OVERLAY, true, SuSyUtility.reactorTankSizeFunction);
        registerSimpleMTE(DRYER, 12, 14621, "dryer", SuSyRecipeMaps.DRYER_RECIPES, SusyTextures.DRYER_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerSimpleMTE(ION_EXCHANGE_COLUMN, 12, 14694, "ion_exchange_column", SuSyRecipeMaps.ION_EXCHANGE_COLUMN_RECIPES, SusyTextures.ION_EXCHANGE_COLUMN_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerSimpleMTE(ZONE_REFINER, 12, 14707, "zone_refiner", SuSyRecipeMaps.ZONE_REFINER_RECIPES, SusyTextures.ZONE_REFINER_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerSimpleMTE(TUBE_FURNACE, 12, 14720, "tube_furnace", SuSyRecipeMaps.TUBE_FURNACE_RECIPES, SusyTextures.TUBE_FURNACE_OVERLAY, true, GTUtility.defaultTankSizeFunction);

        FLUIDIZED_BED_REACTOR = registerMetaTileEntity(14619, new MetaTileEntityFluidizedBedReactor(susyId("fluidized_bed_reactor")));
        POLYMERIZATION_TANK = registerMetaTileEntity(14620, new MetaTileEntityPolmyerizationTank(susyId("polymerization_tank")));
        ELECTROLYTIC_CELL = registerMetaTileEntity(14634, new MetaTileEntityElectrolyticCell(susyId("electrolytic_cell")));
        GRAVITY_SEPARATOR = registerMetaTileEntity(15052, new MetaTileEntityGravitySeparator(susyId("gravity_separator")));

        PRIMITIVE_SMELTER = registerMetaTileEntity(14800, new MetaTileEntityPrimitiveSmelter(susyId("primitive_smelter")));
        PRIMITIVE_ITEM_IMPORT = registerMetaTileEntity(14801, new MetaTileEntityPrimitiveItemBus(susyId("primitive_item_import"), false));
        PRIMITIVE_ITEM_EXPORT = registerMetaTileEntity(14802, new MetaTileEntityPrimitiveItemBus(susyId("primitive_item_export"), true));

        //oil stuff
        COKING_TOWER = registerMetaTileEntity(14635, new MetaTileEntityCokingTower(susyId("coking_tower")));
        VACUUM_DISTILLATION_TOWER = registerMetaTileEntity(14636, new MetaTileEntityVacuumDistillationTower(susyId("vacuum_distillation_tower")));
        SMOKE_STACK = registerMetaTileEntity(14637, new MetaTileEntitySmokeStack(susyId("smoke_stack")));
        CATALYTIC_REFORMER = registerMetaTileEntity(14638, new MetaTileEntityCatalyticReformer(susyId("catalytic_reformer")));
        FERMENTATION_VAT = registerMetaTileEntity(14639, new MetaTileEntityFermentationVat(susyId("fermentation_vat")));

        //circuit stuff
        registerSimpleMTE(UV_LIGHT_BOX, 12, 14640, "uv_light_box", SuSyRecipeMaps.UV_RECIPES, SusyTextures.UV_LIGHT_BOX_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerSimpleMTE(CVD, 12, 14653, "cvd", SuSyRecipeMaps.CVD_RECIPES, SusyTextures.CVD_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerSimpleMTE(ION_IMPLANTER, 12, 14666, "ion_implanter", SuSyRecipeMaps.ION_IMPLANTATION_RECIPES, SusyTextures.ION_IMPLANTER_OVERLAY, true, GTUtility.defaultTankSizeFunction);

        //thermodynamic stuff
        registerSimpleMTE(FLUID_COMPRESSOR, 12, 15000, "fluid_compressor", SuSyRecipeMaps.FLUID_COMPRESSOR_RECIPES, SusyTextures.FLUID_COMPRESSOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerSimpleMTE(FLUID_DECOMPRESSOR, 12, 15013, "fluid_decompressor", SuSyRecipeMaps.FLUID_DECOMPRESSOR_RECIPES, SusyTextures.FLUID_DECOMPRESSOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);

        //war crimes
        registerSimpleMTE(WEAPONS_FACTORY, 12, 15026, "weapons_factory", SuSyRecipeMaps.WEAPONS_FACTORY_RECIPES, Textures.ASSEMBLER_OVERLAY, true, GTUtility.defaultTankSizeFunction);

        //mbd stuff
        ORE_SORTER = registerMetaTileEntity(15040, new MetaTileEntityOreSorter(susyId("ore_sorter")));
        CONDENSER = registerMetaTileEntity(15041, new MetaTileEntityCondenser(susyId("condenser")));
        COOLING_UNIT = registerMetaTileEntity(15042, new MetaTileEntityCoolingUnit(susyId("cooling_unit")));
        HEAT_EXCHANGER = registerMetaTileEntity(15044, new MetaTileEntityHeatExchanger(susyId("heat_exchanger")));
        HEAT_RADIATOR = registerMetaTileEntity(15045, new MetaTileEntityHeatRadiator(susyId("heat_radiator")));
        LARGE_WEAPONS_FACTORY = registerMetaTileEntity(15046, new MetaTileEntityLargeWeaponsFactory(susyId("large_weapons_factory")));
        MAGNETOHYDRODYNAMIC_GENERATOR = registerMetaTileEntity(15047, new MetaTileEntityMagnetohydrodynamicGenerator(susyId("magnetohydrodynamic_generator")));
        MINING_DRILL = registerMetaTileEntity(15048, new MetaTileEntityMiningDrill(susyId("mining_drill")));
        QUENCHER = registerMetaTileEntity(15049, new MetaTileEntityQuencher(susyId("quencher")));
        RAILROAD_ENGINEERING_STATION = registerMetaTileEntity(15050, new MetaTileEntityRailroadEngineeringStation(susyId("railroad_engineering_station")));

        PRIMITIVE_MUD_PUMP = registerMetaTileEntity(15051, new MetaTileEntityPrimitiveMudPump(susyId("primitive_mud_pump")));

        PRESSURE_SWING_ADSORBER = registerMetaTileEntity(15060, new MetaTileEntityPressureSwingAdsorber(susyId("pressure_swing_adsorber")));
        REACTION_FURNACE = registerMetaTileEntity(15061, new MetaTileEntityReactionFurnace(susyId("reaction_furnace")));

        DRONE_PAD = registerMetaTileEntity(15062, new MetaTileEntityDronePad(susyId("drone_pad")));

        LEAD_DRUM = registerMetaTileEntity(14553, new MetaTileEntityDrum(susyId("drum.lead"), Materials.Lead, 32000));
        BRASS_DRUM = registerMetaTileEntity(17010, new MetaTileEntityDrum(susyId("drum.brass"), new PropertyFluidFilter(1280, true, false, true, false), false, Materials.Brass.getMaterialRGB(), 16000));

        NEW_ENERGY_OUTPUT_HATCH_4A[0] = registerMetaTileEntity(16000, new SusyMetaTileEntityEnergyHatch(susyId("energy_hatch.output_4a.lv"), 1, 4, true));
        NEW_ENERGY_OUTPUT_HATCH_16A[0] = registerMetaTileEntity(16001, new SusyMetaTileEntityEnergyHatch(susyId("energy_hatch.output_16a.lv"), 1, 16, true));
        NEW_ENERGY_OUTPUT_HATCH_4A[1] = registerMetaTileEntity(16002, new SusyMetaTileEntityEnergyHatch(susyId("energy_hatch.output_4a.mv"), 2, 4, true));
        NEW_ENERGY_OUTPUT_HATCH_16A[1] = registerMetaTileEntity(16003, new SusyMetaTileEntityEnergyHatch(susyId("energy_hatch.output_16a.mv"), 2, 16, true));
        NEW_ENERGY_OUTPUT_HATCH_4A[2] = registerMetaTileEntity(16004, new SusyMetaTileEntityEnergyHatch(susyId("energy_hatch.output_4a.hv"), 3, 4, true));
        NEW_ENERGY_OUTPUT_HATCH_16A[2] = registerMetaTileEntity(16005, new SusyMetaTileEntityEnergyHatch(susyId("energy_hatch.output_16a.hv"), 3, 16, true));
        NEW_ENERGY_OUTPUT_HATCH_16A[3] = registerMetaTileEntity(16006, new SusyMetaTileEntityEnergyHatch(susyId("energy_hatch.output_16a.ev"), 4, 16, true));

        BASIC_STEAM_TURBINE = registerMetaTileEntity(17000, new MetaTileEntitySUSYLargeTurbine(susyId("basic_steam_turbine"), SuSyRecipeMaps.LARGE_STEAM_TURBINE, 1, MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_TURBINE_CASING), Textures.SOLID_STEEL_CASING, SusyTextures.LARGE_STEAM_TURBINE_OVERLAY));
        BASIC_GAS_TURBINE = registerMetaTileEntity(17001, new MetaTileEntitySUSYLargeTurbine(susyId("basic_gas_turbine"), RecipeMaps.GAS_TURBINE_FUELS, 2, MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_TURBINE_CASING), Textures.SOLID_STEEL_CASING, SusyTextures.LARGE_GAS_TURBINE_OVERLAY));

        ADVANCED_ARC_FURNACE = registerMetaTileEntity(17003, new MetaTileEntityAdvancedArcFurnace(susyId("advanced_arc_furnace")));
        CLARIFIER = registerMetaTileEntity(17004, new MetaTileEntityClarifier(susyId("clarifier")));
        DUMPER = registerMetaTileEntity(17005, new MetaTileEntityDumper(susyId("dumper")));
        EVAPORATION_POOL = registerMetaTileEntity(EVAPORATION_POOL_ID, new MetaTileEntityEvaporationPool(susyId("evaporation_pool")));
        FLARE_STACK = registerMetaTileEntity(17007, new MetaTileEntityFlareStack(susyId("flare_stack")));
        FROTH_FLOTATION_TANK = registerMetaTileEntity(17008, new MetaTileEntityFrothFlotationTank(susyId("froth_flotation_tank")));
        MULTI_STAGE_FLASH_DISTILLER = registerMetaTileEntity(17009, new MetaTileEntityMultiStageFlashDistiller(susyId("multi_stage_flash_distiller")));

        OCEAN_PUMPER = registerMetaTileEntity(17011, new MetaTileEntityOceanPumper(susyId("ocean_pumper")));
        HIGH_TEMPERATURE_DISTILLATION_TOWER = registerMetaTileEntity(17012, new MetaTileEntityHighTemperatureDistillationTower(susyId("high_temperature_distillation_tower")));
        ROTARY_KILN = registerMetaTileEntity(17013, new MetaTileEntityRotaryKiln(susyId("rotary_kiln")));
        HIGH_PRESSURE_CRYOGENIC_DISTILLATION_PLANT = registerMetaTileEntity(17014, new MetaTileEntityHighPressureCryogenicDistillationPlant(susyId("high_pressure_cryogenic_distillation_plant")));
        LOW_PRESSURE_CRYOGENIC_DISTILLATION_PLANT = registerMetaTileEntity(17015, new MetaTileEntityLowPressureCryogenicDistillationPlant(susyId("low_pressure_cryogenic_distillation_plant")));
        REVERBERATORY_FURNACE = registerMetaTileEntity(17016, new MetaTileEntityReverberatoryFurnace(susyId("reverberatory_furnace")));
        SINGLE_COLUMN_CRYOGENIC_DISTILLATION_PLANT = registerMetaTileEntity(17017, new MetaTileEntitySingleColumnCryogenicDistillationPlant(susyId("single_column_cryogenic_distillation_plant")));
        BLENDER = registerMetaTileEntity(17020, new MetaTileEntityBlender(susyId("blender")));

        PHASE_SEPARATOR[0] = registerMetaTileEntity(17018, new MetaTileEntityPhaseSeparator(susyId("phase_separator")));
        BATH_CONDENSER[0] = registerMetaTileEntity(17019, new MetaTileEntityBathCondenser(susyId("bath_condenser")));

        registerSimpleMTE(ELECTROSTATIC_SEPARATOR, 12, 17035, "electrostatic_separator", SuSyRecipeMaps.ELECTROSTATIC_SEPARATOR, SusyTextures.ELECTROSTATIC_SEPARATOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerSimpleMTE(POLISHING_MACHINE, 12, 17048, "polishing_machine", SuSyRecipeMaps.POLISHING_MACHINE, SusyTextures.POLISHING_MACHINE_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerSimpleMTE(TEXTILE_SPINNER, 12, 17061, "textile_spinner", SuSyRecipeMaps.SPINNING_RECIPES, SusyTextures.TEXTILE_SPINNER_OVERLAY, true);
    }

    private static void registerSimpleSteamMTE(SuSySimpleSteamMetaTileEntity[] machines, int startId, String name, RecipeMap<?> recipeMap, SuSySteamProgressIndicator progressIndicator, ICubeRenderer texture, boolean isBricked) {
        machines[0] = registerMetaTileEntity(startId, new SuSySimpleSteamMetaTileEntity(susyId(String.format("%s.bronze", name)), recipeMap, progressIndicator, texture, isBricked, false));
        machines[1] = registerMetaTileEntity(startId + 1, new SuSySimpleSteamMetaTileEntity(susyId(String.format("%s.steel", name)), recipeMap, progressIndicator, texture, isBricked, true));
    }

    private static void registerSimpleMTE(SimpleMachineMetaTileEntity[] machines, int maxTier, int startId, String name, RecipeMap<?> map, ICubeRenderer texture, boolean hasFrontFacing) {
        for (int i = 0; i <= maxTier; i++) {
            machines[i] = registerMetaTileEntity(startId + i, new SimpleMachineMetaTileEntity(susyId(String.format("%s.%s", name, GTValues.VN[i + 1].toLowerCase())), map, texture, i + 1, hasFrontFacing));
        }
    }

    private static void registerSimpleMTE(SimpleMachineMetaTileEntity[] machines, int maxTier, int startId, String name, RecipeMap<?> map, ICubeRenderer texture, boolean hasFrontFacing, Function<Integer, Integer> tankScalingFunction) {
        for (int i = 0; i <= maxTier; i++) {
            machines[i] = registerMetaTileEntity(startId + i, new SimpleMachineMetaTileEntity(susyId(String.format("%s.%s", name, GTValues.VN[i + 1].toLowerCase())), map, texture, i + 1, hasFrontFacing, tankScalingFunction));
        }
    }

    private static void registerContinuousMachineMTE(ContinuousMachineMetaTileEntity[] machines, int maxTier, int startId, String name, RecipeMap<?> map, ICubeRenderer texture, boolean hasFrontFacing, Function<Integer, Integer> tankScalingFunction) {
        for (int i = 0; i <= maxTier; i++) {
            machines[i] = registerMetaTileEntity(startId + i, new ContinuousMachineMetaTileEntity(susyId(String.format("%s.%s", name, GTValues.VN[i + 1].toLowerCase())), map, texture, i + 1, hasFrontFacing, tankScalingFunction));
        }
    }

    private static void registerCatalystMTE(CatalystMachineMetaTileEntity[] machines, int maxTier, int startId, String name, RecipeMap<?> map, ICubeRenderer texture, boolean hasFrontFacing, Function<Integer, Integer> tankScalingFunction) {
        for (int i = 0; i <= maxTier; i++) {
            machines[i] = registerMetaTileEntity(startId + i, new CatalystMachineMetaTileEntity(susyId(String.format("%s.%s", name, GTValues.VN[i + 1].toLowerCase())), map, texture, i + 1, hasFrontFacing, tankScalingFunction));
        }
    }

    private static void registerCatalystMTE(CatalystMachineMetaTileEntity[] machines, int maxTier, int startId, String name, RecipeMap<?> map, ICubeRenderer texture, boolean hasFrontFacing) {
        registerCatalystMTE(machines, maxTier, startId, name, map, texture, hasFrontFacing, GTUtility.defaultTankSizeFunction);
    }

    private static @NotNull ResourceLocation susyId(@NotNull String name) {
        return new ResourceLocation(GTValues.MODID, name);
    }

    static{
        EVAPORATION_POOL_ID = 17006;

        STEAM_LATEX_COLLECTOR = new PseudoMultiSteamMachineMetaTileEntity[2];
        STEAM_VULCANIZING_PRESS = new SuSySimpleSteamMetaTileEntity[2];
        STEAM_ROASTER = new SuSySimpleSteamMetaTileEntity[2];

        STEAM_MIXER = new SuSySimpleSteamMetaTileEntity[2];
        STEAM_VACUUM_CHAMBER = new SuSySimpleSteamMetaTileEntity[2];

        LATEX_COLLECTOR = new PseudoMultiMachineMetaTileEntity[GTValues.EV];
        VULCANIZING_PRESS = new CatalystMachineMetaTileEntity[GTValues.EV];
        ROASTER = new CatalystMachineMetaTileEntity[GTValues.OpV];
        VACUUM_CHAMBER = new SimpleMachineMetaTileEntity[GTValues.OpV];

        CONTINUOUS_STIRRED_TANK_REACTOR = new ContinuousMachineMetaTileEntity[GTValues.OpV];
        FIXED_BED_REACTOR = new ContinuousMachineMetaTileEntity[GTValues.OpV];
        TRICKLE_BED_REACTOR = new ContinuousMachineMetaTileEntity[GTValues.OpV];
        BUBBLE_COLUMN_REACTOR = new ContinuousMachineMetaTileEntity[GTValues.OpV];

        BATCH_REACTOR = new SimpleMachineMetaTileEntity[GTValues.OpV];

        CRYSTALLIZER = new SimpleMachineMetaTileEntity[GTValues.OpV];
        DRYER = new SimpleMachineMetaTileEntity[GTValues.OpV];
        ION_EXCHANGE_COLUMN = new SimpleMachineMetaTileEntity[GTValues.OpV];
        ZONE_REFINER = new SimpleMachineMetaTileEntity[GTValues.OpV];
        TUBE_FURNACE = new SimpleMachineMetaTileEntity[GTValues.OpV];

        UV_LIGHT_BOX = new SimpleMachineMetaTileEntity[GTValues.OpV];
        CVD = new SimpleMachineMetaTileEntity[GTValues.OpV];
        ION_IMPLANTER = new SimpleMachineMetaTileEntity[GTValues.OpV];

        FLUID_COMPRESSOR = new SimpleMachineMetaTileEntity[GTValues.OpV];
        FLUID_DECOMPRESSOR = new SimpleMachineMetaTileEntity[GTValues.OpV];
        WEAPONS_FACTORY = new SimpleMachineMetaTileEntity[GTValues.OpV];

        ELECTROSTATIC_SEPARATOR = new SimpleMachineMetaTileEntity[GTValues.OpV];
        TEXTILE_SPINNER = new SimpleMachineMetaTileEntity[GTValues.OpV];
        POLISHING_MACHINE = new SimpleMachineMetaTileEntity[GTValues.OpV];

        PHASE_SEPARATOR = new SimpleMachineMetaTileEntity[1];
        BATH_CONDENSER = new SimpleMachineMetaTileEntity[1];
    }
}
