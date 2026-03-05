package supersymmetry.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import supersymmetry.Supersymmetry;

public class SuSyNetwork {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE
            .newSimpleChannel(Supersymmetry.MODID + ":warnings");

    public static void init() {}
}
