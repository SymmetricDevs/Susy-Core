package supersymmetry.api.integration.theoneprobe;

import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;
import supersymmetry.api.integration.EvaporationPoolInfoProvider;

public class TheOneProbeCompatibility {

    public static void registerCompatibility() {
        ITheOneProbe oneProbe = TheOneProbe.theOneProbeImp;
        oneProbe.registerProvider(new EvaporationPoolInfoProvider());
    }
}
