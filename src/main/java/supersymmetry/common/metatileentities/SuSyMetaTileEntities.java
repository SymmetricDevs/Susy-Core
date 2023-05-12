package supersymmetry.common.metatileentities;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.storage.MetaTileEntityDrum;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.ContinuousMachineMetaTileEntity;
import supersymmetry.api.metatileentity.steam.SuSySteamProgressIndicator;
import supersymmetry.api.metatileentity.steam.SuSySteamProgressIndicators;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.metatileentities.multi.electric.*;
import supersymmetry.common.metatileentities.multi.primitive.MetaTileEntityCoagulationTank;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntitySmokeStack;
import supersymmetry.common.metatileentities.multi.primitive.MetaTileEntityPrimitiveMudPump;
import supersymmetry.common.metatileentities.single.electric.MetaTileEntityLatexCollector;
import supersymmetry.common.metatileentities.single.steam.*;

import java.util.function.Function;

import static gregtech.common.metatileentities.MetaTileEntities.*;

public class SuSyMetaTileEntities {

    public static MetaTileEntityMagneticRefrigerator MAGNETIC_REFRIGERATOR;

    public static MetaTileEntityCoagulationTank COAGULATION_TANK;

    public static final MetaTileEntityLatexCollector[] LATEX_COLLECTOR;
    public static MetaTileEntitySteamLatexCollector LATEX_COLLECTOR_BRONZE;

    public static SimpleMachineMetaTileEntity[] VULCANIZING_PRESS;
    public static SuSySimpleSteamMetaTileEntity[] STEAM_VULCANIZING_PRESS;

    public static SimpleMachineMetaTileEntity[] ROASTER;
    public static SuSySimpleSteamMetaTileEntity[] STEAM_ROASTER;

    public static MetaTileEntitySinteringOven SINTERING_OVEN;

    public static SuSySimpleSteamMetaTileEntity[] STEAM_MIXER;

    public static SuSySimpleSteamMetaTileEntity[] STEAM_VACUUM_CHAMBER;
    public static SimpleMachineMetaTileEntity[] VACUUM_CHAMBER;

    public static MetaTileEntityDrum LEAD_DRUM;

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
    public static MetaTileEntityGasTurbine GAS_TURBINE;
    public static MetaTileEntityHeatExchanger HEAT_EXCHANGER;
    public static MetaTileEntityHeatRadiator HEAT_RADIATOR;
    public static MetaTileEntityLargeWeaponsFactory LARGE_WEAPONS_FACTORY;
    public static MetaTileEntityMagnetohydrodynamicGenerator MAGNETOHYDRODYNAMIC_GENERATOR;
    public static MetaTileEntityMiningDrill MINING_DRILL;
    public static MetaTileEntityQuencher QUENCHER;
    public static MetaTileEntityRailroadEngineeringStation RAILROAD_ENGINEERING_STATION;

    public static MetaTileEntityPrimitiveMudPump PRIMITIVE_MUD_PUMP;

    public static void init() {
        MAGNETIC_REFRIGERATOR = registerMetaTileEntity(14500, new MetaTileEntityMagneticRefrigerator(susyId("magnetic_refrigerator")));
        COAGULATION_TANK = registerMetaTileEntity(14501, new MetaTileEntityCoagulationTank(susyId("coagulation_tank")));

        LATEX_COLLECTOR[0] = registerMetaTileEntity(14502, new MetaTileEntityLatexCollector(susyId("latex_collector.lv"),1));
        LATEX_COLLECTOR[1] = registerMetaTileEntity(14503, new MetaTileEntityLatexCollector(susyId("latex_collector.mv"),2));
        LATEX_COLLECTOR[2] = registerMetaTileEntity(14504, new MetaTileEntityLatexCollector(susyId("latex_collector.hv"),3));
        LATEX_COLLECTOR[3] = registerMetaTileEntity(14505, new MetaTileEntityLatexCollector(susyId("latex_collector.ev"),4));

        LATEX_COLLECTOR_BRONZE = registerMetaTileEntity(14510, new MetaTileEntitySteamLatexCollector(susyId("latex_collector.bronze")));
        SINTERING_OVEN = registerMetaTileEntity(14521, new MetaTileEntitySinteringOven(susyId("sintering_oven")));

        registerSimpleSteamMTE(STEAM_VULCANIZING_PRESS, 14515, "vulcanizing_press", SuSyRecipeMaps.VULCANIZATION_RECIPES, SuSySteamProgressIndicators.COMPRESS, SusyTextures.VULCANIZING_PRESS_OVERLAY, true);
        registerSimpleMTE(VULCANIZING_PRESS, 3, 14517, "vulcanizing_press", SuSyRecipeMaps.VULCANIZATION_RECIPES, SusyTextures.VULCANIZING_PRESS_OVERLAY, true);

        registerSimpleSteamMTE(STEAM_ROASTER, 14679, "roaster", SuSyRecipeMaps.ROASTER_RECIPES, SuSySteamProgressIndicators.ARROW, SusyTextures.ROASTER_OVERLAY, true);
        registerSimpleMTE(ROASTER, 12, 14523, "roaster", SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, true);

        registerSimpleSteamMTE(STEAM_MIXER, 14536, "mixer", RecipeMaps.MIXER_RECIPES, SuSySteamProgressIndicators.MIXER, SusyTextures.MIXER_OVERLAY_STEAM, false);

        registerSimpleSteamMTE(STEAM_VACUUM_CHAMBER, 14538, "vacuum_chamber", SuSyRecipeMaps.VACUUM_CHAMBER, SuSySteamProgressIndicators.COMPRESS, Textures.GAS_COLLECTOR_OVERLAY, false);
        registerSimpleMTE(VACUUM_CHAMBER, 12, 14540, "vacuum_chamber", SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, true);

        //chem overhaul
        registerContinuousMachineMTE(CONTINUOUS_STIRRED_TANK_REACTOR, 12, 14554, "continuous_stirred_tank_reactor", SuSyRecipeMaps.CSTR_RECIPES, SusyTextures.CONTINUOUS_STIRRED_TANK_REACTOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerContinuousMachineMTE(FIXED_BED_REACTOR, 12, 14567, "fixed_bed_reactor", SuSyRecipeMaps.FIXED_BED_REACTOR_RECIPES, SusyTextures.FIXED_BED_REACTOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerContinuousMachineMTE(TRICKLE_BED_REACTOR, 12, 14580, "trickle_bed_reactor", SuSyRecipeMaps.TRICKLE_BED_REACTOR_RECIPES, SusyTextures.TRICKLE_BED_REACTOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerContinuousMachineMTE(BUBBLE_COLUMN_REACTOR, 12, 14606, "bubble_column_reactor", SuSyRecipeMaps.BUBBLE_COLUMN_REACTOR_RECIPES, SusyTextures.BUBBLE_COLUMN_REACTOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);

        registerSimpleMTE(BATCH_REACTOR, 12, 14681, "batch_reactor", SuSyRecipeMaps.BATCH_REACTOR_RECIPES, SusyTextures.BATCH_REACTOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);

        //max tier = 12 -> OpV [excludes ULv] -> 13 ids taken (add maxTier +1 to start ID to get next valid id)
        registerSimpleMTE(CRYSTALLIZER, 12, 14593, "crystallizer", SuSyRecipeMaps.CRYSTALLIZER_RECIPES, SusyTextures.CRYSTALLIZER_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerSimpleMTE(DRYER, 12, 14621, "dryer", SuSyRecipeMaps.DRYER, SusyTextures.DRYER_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerSimpleMTE(ION_EXCHANGE_COLUMN, 12, 14694, "ion_exchange_column", SuSyRecipeMaps.ION_EXCHANGE_COLUMN_RECIPES, SusyTextures.ION_EXCHANGE_COLUMN_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerSimpleMTE(ZONE_REFINER, 12, 14707, "zone_refiner", SuSyRecipeMaps.ZONE_REFINER_RECIPES, SusyTextures.ZONE_REFINER_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerSimpleMTE(TUBE_FURNACE, 12, 14720, "tube_furnace", SuSyRecipeMaps.TUBE_FURNACE_RECIPES, SusyTextures.TUBE_FURNACE_OVERLAY, true, GTUtility.defaultTankSizeFunction);

        FLUIDIZED_BED_REACTOR = registerMetaTileEntity(14619, new MetaTileEntityFluidizedBedReactor(susyId("fluidized_bed_reactor")));
        POLYMERIZATION_TANK = registerMetaTileEntity(14620, new MetaTileEntityPolmyerizationTank(susyId("polymerization_tank")));
        ELECTROLYTIC_CELL = registerMetaTileEntity(14634, new MetaTileEntityElectrolyticCell(susyId("electrolytic_cell")));

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
        GAS_TURBINE = registerMetaTileEntity(15043, new MetaTileEntityGasTurbine(susyId("gas_turbine")));
        HEAT_EXCHANGER = registerMetaTileEntity(15044, new MetaTileEntityHeatExchanger(susyId("heat_exchanger")));
        HEAT_RADIATOR = registerMetaTileEntity(15045, new MetaTileEntityHeatRadiator(susyId("heat_radiator")));
        LARGE_WEAPONS_FACTORY = registerMetaTileEntity(15046, new MetaTileEntityLargeWeaponsFactory(susyId("large_weapons_factory")));
        MAGNETOHYDRODYNAMIC_GENERATOR = registerMetaTileEntity(15047, new MetaTileEntityMagnetohydrodynamicGenerator(susyId("magnetohydrodynamic_generator")));
        MINING_DRILL = registerMetaTileEntity(15048, new MetaTileEntityMiningDrill(susyId("mining_drill")));
        QUENCHER = registerMetaTileEntity(15049, new MetaTileEntityQuencher(susyId("quencher")));
        RAILROAD_ENGINEERING_STATION = registerMetaTileEntity(15050, new MetaTileEntityRailroadEngineeringStation(susyId("railroad_engineering_station")));

        PRIMITIVE_MUD_PUMP = registerMetaTileEntity(15051, new MetaTileEntityPrimitiveMudPump(susyId("primitive_mud_pump")));

        LEAD_DRUM = registerMetaTileEntity(14553, new MetaTileEntityDrum(susyId("drum.lead"), Materials.Lead, 32000));
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

    private static @NotNull ResourceLocation susyId(@NotNull String name) {
        return new ResourceLocation(GTValues.MODID, name);
    }

    static{
        LATEX_COLLECTOR = new MetaTileEntityLatexCollector[GTValues.EV];
        STEAM_VULCANIZING_PRESS = new SuSySimpleSteamMetaTileEntity[2];
        STEAM_ROASTER = new SuSySimpleSteamMetaTileEntity[2];
        STEAM_MIXER = new SuSySimpleSteamMetaTileEntity[2];
        STEAM_VACUUM_CHAMBER = new SuSySimpleSteamMetaTileEntity[2];

        VULCANIZING_PRESS = new SimpleMachineMetaTileEntity[GTValues.EV];
        ROASTER = new SimpleMachineMetaTileEntity[GTValues.OpV];
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
    }
}
