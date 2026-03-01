package supersymmetry.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import supersymmetry.Supersymmetry;

public class SuSyNetwork {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE
            .newSimpleChannel(Supersymmetry.MODID + ":warnings");

    public static void init() {
        CHANNEL.registerMessage(MissingModsMessage.Handler.class, MissingModsMessage.class, 0, Side.CLIENT);
    }
}
