package supersymmetry.common.metatileentities;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.storage.MetaTileEntityDrum;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.ContinuousMachineMetaTileEntity;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityFluidizedBedReactor;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityPolmyerizationTank;
import supersymmetry.common.metatileentities.multi.primitive.MetaTileEntityCoagulationTank;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityMagneticRefrigerator;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntitySinteringOven;
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
    public static MetaTileEntitySteamVulcanizingPress VULCANIZING_PRESS_BRONZE;

    public static SimpleMachineMetaTileEntity[] ROASTER;
    public static MetaTileEntitySteamRoaster ROASTER_BRONZE;

    public static MetaTileEntitySinteringOven SINTERING_OVEN;

    public static MetaTileEntitySteamMixer MIXER_BRONZE;

    public static MetaTileEntitySteamVacuumChamber VACUUM_CHAMBER_BRONZE;
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


    public static void init() {
        MAGNETIC_REFRIGERATOR = registerMetaTileEntity(14500, new MetaTileEntityMagneticRefrigerator(susyId("magnetic_refrigerator")));
        COAGULATION_TANK = registerMetaTileEntity(14501, new MetaTileEntityCoagulationTank(susyId("coagulation_tank")));

        LATEX_COLLECTOR[0] = registerMetaTileEntity(14502, new MetaTileEntityLatexCollector(susyId("latex_collector.lv"),1));
        LATEX_COLLECTOR[1] = registerMetaTileEntity(14503, new MetaTileEntityLatexCollector(susyId("latex_collector.mv"),2));
        LATEX_COLLECTOR[2] = registerMetaTileEntity(14504, new MetaTileEntityLatexCollector(susyId("latex_collector.hv"),3));
        LATEX_COLLECTOR[3] = registerMetaTileEntity(14505, new MetaTileEntityLatexCollector(susyId("latex_collector.ev"),4));

        LATEX_COLLECTOR_BRONZE = registerMetaTileEntity(14510, new MetaTileEntitySteamLatexCollector(susyId("latex_collector.bronze")));

        registerSimpleMTE(VULCANIZING_PRESS, 3, 14515, "vulcanizing_press", SuSyRecipeMaps.VULCANIZATION_RECIPES, SusyTextures.VULCANIZING_PRESS_OVERLAY, true);
        VULCANIZING_PRESS_BRONZE = registerMetaTileEntity(14520, new MetaTileEntitySteamVulcanizingPress(susyId("vulcanizing_press.steam"), false));

        SINTERING_OVEN = registerMetaTileEntity(14521, new MetaTileEntitySinteringOven(susyId("sintering_oven")));

        ROASTER_BRONZE = registerMetaTileEntity(14522, new MetaTileEntitySteamRoaster(susyId("roaster.steam"), false));
        registerSimpleMTE(ROASTER, 12, 14523, "roaster", SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, true);

        MIXER_BRONZE = registerMetaTileEntity(14536, new MetaTileEntitySteamMixer(susyId("mixer.steam"), false));

        VACUUM_CHAMBER_BRONZE = registerMetaTileEntity(14537, new MetaTileEntitySteamVacuumChamber(susyId("vacuum_chamber.steam"), false));
        registerSimpleMTE(VACUUM_CHAMBER, 12, 14538, "vacuum_chamber", SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, true);

        LEAD_DRUM = registerMetaTileEntity(14551, new MetaTileEntityDrum(susyId("drum.lead"), Materials.Lead, 32000));

        registerContinuousMachineMTE(CONTINUOUS_STIRRED_TANK_REACTOR, 12, 14552, "continuous_stirred_tank_reactor", SuSyRecipeMaps.CSTR_RECIPES, SusyTextures.CONTINUOUS_STIRRED_TANK_REACTOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerContinuousMachineMTE(FIXED_BED_REACTOR, 12, 14565, "fixed_bed_reactor", SuSyRecipeMaps.FIXED_BED_REACTOR_RECIPES, SusyTextures.FIXED_BED_REACTOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerContinuousMachineMTE(TRICKLE_BED_REACTOR, 12, 14578, "trickle_bed_reactor", SuSyRecipeMaps.TRICKLE_BED_REACTOR_RECIPES, SusyTextures.TRICKLE_BED_REACTOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerSimpleMTE(CRYSTALLIZER, 12, 14591, "crystallizer", SuSyRecipeMaps.CRYSTALLIZER_RECIPES, SusyTextures.CRYSTALLIZER_OVERLAY, true, GTUtility.defaultTankSizeFunction);
        registerContinuousMachineMTE(BUBBLE_COLUMN_REACTOR, 12, 14604, "bubble_column_reactor", SuSyRecipeMaps.BUBBLE_COLUMN_REACTOR_RECIPES, SusyTextures.BUBBLE_COLUMN_REACTOR_OVERLAY, true, GTUtility.defaultTankSizeFunction);

        FLUIDIZED_BED_REACTOR = registerMetaTileEntity(14617, new MetaTileEntityFluidizedBedReactor(susyId("fluidized_bed_reactor")));
        POLYMERIZATION_TANK = registerMetaTileEntity(14618, new MetaTileEntityPolmyerizationTank(susyId("polymerization_tank")));

        registerSimpleMTE(DRYER, 12, 14619, "dryer", SuSyRecipeMaps.DRYER, SusyTextures.DRYER_OVERLAY, true, GTUtility.defaultTankSizeFunction);
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
        VULCANIZING_PRESS = new SimpleMachineMetaTileEntity[GTValues.EV];
        ROASTER = new SimpleMachineMetaTileEntity[GTValues.OpV];
        VACUUM_CHAMBER = new SimpleMachineMetaTileEntity[GTValues.OpV];
        CONTINUOUS_STIRRED_TANK_REACTOR = new ContinuousMachineMetaTileEntity[GTValues.OpV];
        FIXED_BED_REACTOR = new ContinuousMachineMetaTileEntity[GTValues.OpV];
        TRICKLE_BED_REACTOR = new ContinuousMachineMetaTileEntity[GTValues.OpV];
        CRYSTALLIZER = new SimpleMachineMetaTileEntity[GTValues.OpV];
        BUBBLE_COLUMN_REACTOR = new ContinuousMachineMetaTileEntity[GTValues.OpV];
        DRYER = new SimpleMachineMetaTileEntity[GTValues.OpV];
    }
}
