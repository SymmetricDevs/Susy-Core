package supersymmetry.integration.theoneprobe;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import gregtech.api.modules.GregTechModule;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;
import mcjty.theoneprobe.TheOneProbe;
import supersymmetry.Supersymmetry;
import supersymmetry.integration.theoneprobe.provider.LittleTilesStorageInfoProvider;
import supersymmetry.modules.SuSyModules;

@GregTechModule(
        moduleID = SuSyModules.MODULE_TOP_LT,
        containerID = Supersymmetry.MODID,
        modDependencies = { Mods.Names.THE_ONE_PROBE, "littletiles" },
        name = "SuSy TheOneProbe LittleTiles Integration",
        description = "SuSy TheOneProbe LittleTiles Integration Module")
public class TheOneProbeLittleTilesModule extends IntegrationSubmodule {

    @Override
    public void init(FMLInitializationEvent event) {
        getLogger().info("TheOneProbe and LittleTiles found. Enabling SuSy top integration...");
        TheOneProbe.theOneProbeImp.registerProvider(new LittleTilesStorageInfoProvider());
    }
}
