package supersymmetry.integration.theoneprobe;

import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;
import org.apache.logging.log4j.LogManager;
import supersymmetry.integration.theoneprobe.provider.CoagulationTankParallelProvider;
import supersymmetry.integration.theoneprobe.provider.VoidingMultiblockInfoProvider;

public class TheOneProbeModule {

    public static void init() {
        LogManager.getLogger("Supersymmetry TOP Integration").info("TheOneProbe found. Enabling integration...");
        ITheOneProbe oneProbe = TheOneProbe.theOneProbeImp;
        oneProbe.registerProvider(new VoidingMultiblockInfoProvider());
        oneProbe.registerProvider(new CoagulationTankParallelProvider());
    }
}
