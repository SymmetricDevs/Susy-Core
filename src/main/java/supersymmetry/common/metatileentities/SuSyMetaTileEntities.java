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
    public static SimpleMachineMetaTileEntity[] CRYSTALLIZER;
    public static ContinuousMachineMetaTileEntity[] BUBBLE_COLUMN_REACTOR;
    public static SimpleMachineMetaTileEntity[] DRYER;
    public static MetaTileEntityFluidizedBedReactor FLUIDIZED_BED_REACTOR;
    public static MetaTileEntityPolmyerizationTank POLYMERIZATION_TANK;

    public static MetaTileEntityElectrolyticCell ELECTROLYTIC_CELL;


    public static void init() {
        MAGNETIC_REFRIGERATOR = registerMetaTileEntity(14500, new MetaTileEntityMagneticRefrigerator(susyId("magnetic_refrigerator")));
        COAGULATION_TANK = registerMetaTileEntity(14501, new MetaTileEntityCoagulationTank(susyId("coagulation_tank")));

        LATEX_COLLECTOR[0] = registerMetaTileEntity(14502, new MetaTileEntityLatexCollector(susyId("latex_collector.lv"),1));
        LATEX_COLLECTOR[1] = registerMetaTileEntity(14503, new MetaTileEntityLatexCollector(susyId("latex_collector.mv"),2));
        LATEX_COLLECTOR[2] = registerMetaTileEntity(14504, new MetaTileEntityLatexCollector(susyId("latex_collector.hv"),3));
        LATEX_COLLECTOR[3] = registerMetaTileEntity(14505, new MetaTileEntityLatexCollector(susyId("latex_collector.ev"),4));

        LATEX_COLLECTOR_BRONZE = registerMetaTileEntity(14510, new MetaTileEntitySteamLatexCollector(susyId("latex_collector.bronze")));

        registerSimpleSteamMTE(STEAM_VULCANIZING_PRESS, 14515, "vulcanizing_press", SuSyRecipeMaps.VULCANIZATION_RECIPES, SuSySteamProgressIndicators.COMPRESS, SusyTextures.VULCANIZING_PRESS_OVERLAY, true);
        registerSimpleMTE(VULCANIZING_PRESS, 3, 14517, "vulcanizing_press", SuSyRecipeMaps.VULCANIZATION_RECIPES, SusyTextures.VULCANIZING_PRESS_OVERLAY, true);

        SINTERING_OVEN = registerMetaTileEntity(14521, new MetaTileEntitySinteringOven(susyId("sintering_oven")));

        registerSimpleSteamMTE(STEAM_ROASTER, 14722, "roaster", SuSyRecipeMaps.ROASTER_RECIPES, SuSySteamProgressIndicators.ARROW, SusyTextures.ROASTER_OVERLAY, true);
        registerSimpleMTE(ROASTER, 12, 14523, "roaster", SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, true);

        registerSimpleSteamMTE(STEAM_MIXER, 14536, "mixer", RecipeMaps.MIXER_RECIPES, SuSySteamProgressIndicators.MIXER, SusyTextures.MIXER_OVERLAY_STEAM, false);

        registerSimpleSteamMTE(STEAM_VACUUM_CHAMBER, 14538, "vacuum_chamber", SuSyRecipeMaps.VACUUM_CHAMBER, SuSySteamProgressIndicators.COMPRESS, Textures.GAS_COLLECTOR_OVERLAY, false);
        registerSimpleMTE(VACUUM_CHAMBER, 12, 14540, "vacuum_chamber", SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, true);

        LEAD_DRUM = registerMetaTileEntity(14553, new MetaTileEntityDrum(susyId("drum.lead"), Materials.Lead, 32000));

        registerContinuousMachineMTE(CONTINUOUS_STIRRED_TANK_REACTOR, 12, 14554, "continuous_stirred_tank_reactor", SuSyRecipeMaps.CSTR_RECIPES, SusyTextures.CONTINUOUS_STIRRED_TANK_REACTOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerContinuousMachineMTE(FIXED_BED_REACTOR, 12, 14567, "fixed_bed_reactor", SuSyRecipeMaps.FIXED_BED_REACTOR_RECIPES, SusyTextures.FIXED_BED_REACTOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerContinuousMachineMTE(TRICKLE_BED_REACTOR, 12, 14580, "trickle_bed_reactor", SuSyRecipeMaps.TRICKLE_BED_REACTOR_RECIPES, SusyTextures.TRICKLE_BED_REACTOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerSimpleMTE(CRYSTALLIZER, 12, 14593, "crystallizer", SuSyRecipeMaps.CRYSTALLIZER_RECIPES, SusyTextures.CRYSTALLIZER_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerContinuousMachineMTE(BUBBLE_COLUMN_REACTOR, 12, 14606, "bubble_column_reactor", SuSyRecipeMaps.BUBBLE_COLUMN_REACTOR_RECIPES, SusyTextures.BUBBLE_COLUMN_REACTOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);

        FLUIDIZED_BED_REACTOR = registerMetaTileEntity(14619, new MetaTileEntityFluidizedBedReactor(susyId("fluidized_bed_reactor")));
        POLYMERIZATION_TANK = registerMetaTileEntity(14620, new MetaTileEntityPolmyerizationTank(susyId("polymerization_tank")));

        registerSimpleMTE(DRYER, 12, 14621, "dryer", SuSyRecipeMaps.DRYER, SusyTextures.DRYER_OVERLAY, true, GTUtility.defaultTankSizeFunction);

        ELECTROLYTIC_CELL = registerMetaTileEntity(14634, new MetaTileEntityElectrolyticCell(susyId("electrolytic_cell")));

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
        VULCANIZING_PRESS = new SimpleMachineMetaTileEntity[GTValues.EV];
        STEAM_ROASTER = new SuSySimpleSteamMetaTileEntity[2];
        ROASTER = new SimpleMachineMetaTileEntity[GTValues.OpV];
        STEAM_MIXER = new SuSySimpleSteamMetaTileEntity[2];
        VACUUM_CHAMBER = new SimpleMachineMetaTileEntity[GTValues.OpV];
        STEAM_VACUUM_CHAMBER = new SuSySimpleSteamMetaTileEntity[2];
        CONTINUOUS_STIRRED_TANK_REACTOR = new ContinuousMachineMetaTileEntity[GTValues.OpV];
        FIXED_BED_REACTOR = new ContinuousMachineMetaTileEntity[GTValues.OpV];
        TRICKLE_BED_REACTOR = new ContinuousMachineMetaTileEntity[GTValues.OpV];
        CRYSTALLIZER = new SimpleMachineMetaTileEntity[GTValues.OpV];
        BUBBLE_COLUMN_REACTOR = new ContinuousMachineMetaTileEntity[GTValues.OpV];
        DRYER = new SimpleMachineMetaTileEntity[GTValues.OpV];
    }
}
