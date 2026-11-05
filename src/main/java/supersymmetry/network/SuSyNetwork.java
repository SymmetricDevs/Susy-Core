package supersymmetry.network;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import supersymmetry.Supersymmetry;

public class SuSyNetwork {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE
            .newSimpleChannel(Supersymmetry.MODID + ":warnings");

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new MissingMods());

        int id = 0;
        CHANNEL.registerMessage(PacketToolbeltSelectionChange.Handler.class, PacketToolbeltSelectionChange.class, id++,
                net.minecraftforge.fml.relauncher.Side.CLIENT);
        CHANNEL.registerMessage(PacketToolbeltSelectionChange.Handler.class, PacketToolbeltSelectionChange.class, id++,
                net.minecraftforge.fml.relauncher.Side.SERVER);
    }
}
