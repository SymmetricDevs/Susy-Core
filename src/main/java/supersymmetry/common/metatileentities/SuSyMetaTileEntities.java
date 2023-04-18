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
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityFluidizedBedReactor;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityPolmyerizationTank;
import supersymmetry.common.metatileentities.multi.primitive.MetaTileEntityCoagulationTank;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityMagneticRefrigerator;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntitySinteringOven;
import supersymmetry.common.metatileentities.single.electric.MetaTileEntityLatexCollector;
import supersymmetry.common.metatileentities.single.steam.*;

import java.util.Arrays;

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
    public static SimpleMachineMetaTileEntity[] CONTINUOUS_STIRRED_TANK_REACTOR;
    public static SimpleMachineMetaTileEntity[] FIXED_BED_REACTOR;
    public static SimpleMachineMetaTileEntity[] TRICKLE_BED_REACTOR;
    public static SimpleMachineMetaTileEntity[] CRYSTALLIZER;
    public static SimpleMachineMetaTileEntity[] BUBBLE_COLUMN_REACTOR;
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

        VULCANIZING_PRESS[0] = registerMetaTileEntity(14515, new SimpleMachineMetaTileEntity(susyId("vulcanizing_press.lv"), SuSyRecipeMaps.VULCANIZATION_RECIPES, SusyTextures.VULCANIZING_PRESS_OVERLAY,1, true));
        VULCANIZING_PRESS[1] = registerMetaTileEntity(14516, new SimpleMachineMetaTileEntity(susyId("vulcanizing_press.mv"), SuSyRecipeMaps.VULCANIZATION_RECIPES, SusyTextures.VULCANIZING_PRESS_OVERLAY,2, true));
        VULCANIZING_PRESS[2] = registerMetaTileEntity(14517, new SimpleMachineMetaTileEntity(susyId("vulcanizing_press.hv"), SuSyRecipeMaps.VULCANIZATION_RECIPES, SusyTextures.VULCANIZING_PRESS_OVERLAY,3, true));
        VULCANIZING_PRESS[3] = registerMetaTileEntity(14518, new SimpleMachineMetaTileEntity(susyId("vulcanizing_press.ev"), SuSyRecipeMaps.VULCANIZATION_RECIPES, SusyTextures.VULCANIZING_PRESS_OVERLAY,4, true));

        VULCANIZING_PRESS_BRONZE = registerMetaTileEntity(14520, new MetaTileEntitySteamVulcanizingPress(susyId("vulcanizing_press.steam"), false));

        SINTERING_OVEN = registerMetaTileEntity(14521, new MetaTileEntitySinteringOven(susyId("sintering_oven")));

        ROASTER_BRONZE = registerMetaTileEntity(14522, new MetaTileEntitySteamRoaster(susyId("roaster.steam"), false));

        ROASTER[0] = registerMetaTileEntity(14523, new SimpleMachineMetaTileEntity(susyId("roaster.lv"), SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, 1, true));
        ROASTER[1] = registerMetaTileEntity(14524, new SimpleMachineMetaTileEntity(susyId("roaster.mv"), SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, 2, true));
        ROASTER[2] = registerMetaTileEntity(14525, new SimpleMachineMetaTileEntity(susyId("roaster.hv"), SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, 3, true));
        ROASTER[3] = registerMetaTileEntity(14526, new SimpleMachineMetaTileEntity(susyId("roaster.ev"), SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, 4, true));
        ROASTER[4] = registerMetaTileEntity(14527, new SimpleMachineMetaTileEntity(susyId("roaster.iv"), SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, 5, true));
        ROASTER[5] = registerMetaTileEntity(14528, new SimpleMachineMetaTileEntity(susyId("roaster.luv"), SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, 6, true));
        ROASTER[6] = registerMetaTileEntity(14529, new SimpleMachineMetaTileEntity(susyId("roaster.zpm"), SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, 7, true));
        ROASTER[7] = registerMetaTileEntity(14530, new SimpleMachineMetaTileEntity(susyId("roaster.uv"), SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, 8, true));
        ROASTER[8] = registerMetaTileEntity(14531, new SimpleMachineMetaTileEntity(susyId("roaster.uhv"), SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, 9, true));
        ROASTER[9] = registerMetaTileEntity(14532, new SimpleMachineMetaTileEntity(susyId("roaster.uev"), SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, 10, true));
        ROASTER[10] = registerMetaTileEntity(14533, new SimpleMachineMetaTileEntity(susyId("roaster.uiv"), SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, 11, true));
        ROASTER[11] = registerMetaTileEntity(14534, new SimpleMachineMetaTileEntity(susyId("roaster.uxv"), SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, 12, true));
        ROASTER[12] = registerMetaTileEntity(14535, new SimpleMachineMetaTileEntity(susyId("roaster.opv"), SuSyRecipeMaps.ROASTER_RECIPES, SusyTextures.ROASTER_OVERLAY, 13, true));

        MIXER_BRONZE = registerMetaTileEntity(14536, new MetaTileEntitySteamMixer(susyId("mixer.steam"), false));

        VACUUM_CHAMBER_BRONZE = registerMetaTileEntity(14537, new MetaTileEntitySteamVacuumChamber(susyId("vacuum_chamber.steam"), false));

        VACUUM_CHAMBER[0] = registerMetaTileEntity(14538, new SimpleMachineMetaTileEntity(susyId("vacuum_chamber.lv"), SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, 1, true));
        VACUUM_CHAMBER[1] = registerMetaTileEntity(14539, new SimpleMachineMetaTileEntity(susyId("vacuum_chamber.mv"), SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, 2, true));
        VACUUM_CHAMBER[2] = registerMetaTileEntity(14540, new SimpleMachineMetaTileEntity(susyId("vacuum_chamber.hv"), SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, 3, true));
        VACUUM_CHAMBER[3] = registerMetaTileEntity(14541, new SimpleMachineMetaTileEntity(susyId("vacuum_chamber.ev"), SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, 4, true));
        VACUUM_CHAMBER[4] = registerMetaTileEntity(14542, new SimpleMachineMetaTileEntity(susyId("vacuum_chamber.iv"), SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, 5, true));
        VACUUM_CHAMBER[5] = registerMetaTileEntity(14543, new SimpleMachineMetaTileEntity(susyId("vacuum_chamber.luv"), SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, 6, true));
        VACUUM_CHAMBER[6] = registerMetaTileEntity(14544, new SimpleMachineMetaTileEntity(susyId("vacuum_chamber.zpm"), SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, 7, true));
        VACUUM_CHAMBER[7] = registerMetaTileEntity(14545, new SimpleMachineMetaTileEntity(susyId("vacuum_chamber.uv"), SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, 8, true));
        VACUUM_CHAMBER[8] = registerMetaTileEntity(14546, new SimpleMachineMetaTileEntity(susyId("vacuum_chamber.uhv"), SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, 9, true));
        VACUUM_CHAMBER[9] = registerMetaTileEntity(14547, new SimpleMachineMetaTileEntity(susyId("vacuum_chamber.uev"), SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, 10, true));
        VACUUM_CHAMBER[10] = registerMetaTileEntity(14548, new SimpleMachineMetaTileEntity(susyId("vacuum_chamber.uiv"), SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, 11, true));
        VACUUM_CHAMBER[11] = registerMetaTileEntity(14549, new SimpleMachineMetaTileEntity(susyId("vacuum_chamber.uxv"), SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY, 12, true));
        VACUUM_CHAMBER[12] = registerMetaTileEntity(14550, new SimpleMachineMetaTileEntity(susyId("vacuum_chamber.opv"), SuSyRecipeMaps.VACUUM_CHAMBER, Textures.GAS_COLLECTOR_OVERLAY,13, true));

        LEAD_DRUM = registerMetaTileEntity(14551, new MetaTileEntityDrum(susyId("drum.lead"), Materials.Lead, 32000));

        registerSimpleMetaTileEntity(CONTINUOUS_STIRRED_TANK_REACTOR, 14552, "continuous_stirred_tank_reactor", SuSyRecipeMaps.CSTR_RECIPES, SusyTextures.CONTINUOUS_STIRRED_TANK_REACTOR_OVERLAY, true, SuSyMetaTileEntities::susyId, GTUtility.defaultTankSizeFunction);
        registerSimpleMetaTileEntity(FIXED_BED_REACTOR, 14565, "fixed_bed_reactor", SuSyRecipeMaps.FIXED_BED_REACTOR_RECIPES, SusyTextures.FIXED_BED_REACTOR_OVERLAY, true, SuSyMetaTileEntities::susyId, GTUtility.defaultTankSizeFunction);
        registerSimpleMetaTileEntity(TRICKLE_BED_REACTOR, 14578, "trickle_bed_reactor", SuSyRecipeMaps.TRICKLE_BED_REACTOR_RECIPES, SusyTextures.TRICKLE_BED_REACTOR_OVERLAY, true, SuSyMetaTileEntities::susyId, GTUtility.defaultTankSizeFunction);
        registerSimpleMetaTileEntity(CRYSTALLIZER, 14591, "crystallizer", SuSyRecipeMaps.CRYSTALLIZER_RECIPES, SusyTextures.CRYSTALLIZER_OVERLAY, true, SuSyMetaTileEntities::susyId, GTUtility.defaultTankSizeFunction);
        registerSimpleMetaTileEntity(BUBBLE_COLUMN_REACTOR, 14604, "bubble_column_reactor", SuSyRecipeMaps.BUBBLE_COLUMN_REACTOR_RECIPES, SusyTextures.BUBBLE_COLUMN_REACTOR_OVERLAY, true, SuSyMetaTileEntities::susyId, GTUtility.defaultTankSizeFunction);

        FLUIDIZED_BED_REACTOR = registerMetaTileEntity(14617, new MetaTileEntityFluidizedBedReactor(susyId("fluidized_bed_reactor")));
        POLYMERIZATION_TANK = registerMetaTileEntity(14618, new MetaTileEntityPolmyerizationTank(susyId("polymerization_tank")));

        registerSimpleMetaTileEntity(DRYER, 14619, "dryer", SuSyRecipeMaps.DRYER, SusyTextures.DRYER_OVERLAY, true, SuSyMetaTileEntities::susyId, GTUtility.defaultTankSizeFunction);
    }

    private static @NotNull ResourceLocation susyId(@NotNull String name) {
        return new ResourceLocation(GTValues.MODID, name);
    }

    static{
        LATEX_COLLECTOR = new MetaTileEntityLatexCollector[GTValues.EV];
        VULCANIZING_PRESS = new SimpleMachineMetaTileEntity[GTValues.EV];
        ROASTER = new SimpleMachineMetaTileEntity[GTValues.OpV];
        VACUUM_CHAMBER = new SimpleMachineMetaTileEntity[GTValues.OpV];
        CONTINUOUS_STIRRED_TANK_REACTOR = new SimpleMachineMetaTileEntity[GTValues.OpV];
        FIXED_BED_REACTOR = new SimpleMachineMetaTileEntity[GTValues.OpV];
        TRICKLE_BED_REACTOR = new SimpleMachineMetaTileEntity[GTValues.OpV];
        CRYSTALLIZER = new SimpleMachineMetaTileEntity[GTValues.OpV];
        BUBBLE_COLUMN_REACTOR = new SimpleMachineMetaTileEntity[GTValues.OpV];
        DRYER = new SimpleMachineMetaTileEntity[GTValues.OpV];
    }
}
