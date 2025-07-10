package supersymmetry.common.network;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import supersymmetry.client.ClientProxy;

public class SPacketFirstJoin implements IPacket, IClientExecutor {

    public SPacketFirstJoin() {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void executeClient(NetHandlerPlayClient netHandlerPlayClient) {
        ClientProxy.titleRenderTimer = 0;
    }

    @Override
    public void encode(PacketBuffer packetBuffer) {
    }

    @Override
    public void decode(PacketBuffer packetBuffer) {
    }
}
