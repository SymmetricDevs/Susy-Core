package supersymmetry.integration.theoneprobe;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import gregtech.api.modules.GregTechModule;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;
import supersymmetry.SuSyValues;
import supersymmetry.Supersymmetry;
import supersymmetry.integration.theoneprobe.provider.DelegatorInfoProvider;
import supersymmetry.integration.theoneprobe.provider.EvaporationPoolInfoProvider;
import supersymmetry.integration.theoneprobe.provider.LittleTilesStorageInfoProvider;
import supersymmetry.integration.theoneprobe.provider.StrandShaperInfoProvider;
import supersymmetry.modules.SuSyModules;

@GregTechModule(
                moduleID = SuSyModules.MODULE_TOP,
                containerID = Supersymmetry.MODID,
                modDependencies = Mods.Names.THE_ONE_PROBE,
                name = "SuSy TheOneProbe Integration",
                description = "SuSy TheOneProbe Integration Module")
public class TheOneProbeModule extends IntegrationSubmodule {

    @Override
    public void init(FMLInitializationEvent event) {
        getLogger().info("TheOneProbe found. Enabling SuSy top integration...");
        ITheOneProbe oneProbe = TheOneProbe.theOneProbeImp;
        oneProbe.registerProvider(new EvaporationPoolInfoProvider());
        oneProbe.registerProvider(new DelegatorInfoProvider());
        oneProbe.registerProvider(new StrandShaperInfoProvider());
        if (Loader.isModLoaded(SuSyValues.MODID_LITTLE_TILES)) {
            oneProbe.registerProvider(new LittleTilesStorageInfoProvider());
        }
    }
}
