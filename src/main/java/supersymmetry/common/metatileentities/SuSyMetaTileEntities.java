package supersymmetry.common.metatileentities;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipe.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.metatileentities.multi.primitive.MetaTileEntityCoagulationTank;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityMagneticRefrigerator;
import supersymmetry.common.metatileentities.single.electric.MetaTileEntityLatexCollector;
import supersymmetry.common.metatileentities.single.steam.MetaTileEntitySteamLatexCollector;
import supersymmetry.common.metatileentities.single.steam.MetaTileEntitySteamVulcanizingPress;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class SuSyMetaTileEntities {

    public static MetaTileEntityMagneticRefrigerator MAGNETIC_REFRIGERATOR;
    public static MetaTileEntityCoagulationTank COAGULATION_TANK;
    public static final MetaTileEntityLatexCollector[] LATEX_COLLECTOR;
    public static MetaTileEntitySteamLatexCollector LATEX_COLLECTOR_BRONZE;

    public static SimpleMachineMetaTileEntity[] VULCANIZING_PRESS;
    public static MetaTileEntitySteamVulcanizingPress VULCANIZING_PRESS_BRONZE;

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
    }

    private static @NotNull ResourceLocation susyId(@NotNull String name) {
        return new ResourceLocation(GTValues.MODID, name);
    }

    static{
        LATEX_COLLECTOR = new MetaTileEntityLatexCollector[GTValues.EV];
        VULCANIZING_PRESS = new SimpleMachineMetaTileEntity[GTValues.EV];
    }
}
