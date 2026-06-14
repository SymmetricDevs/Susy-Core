package supersymmetry.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;

public class SPacketSpeakerStop implements IPacket, IClientExecutor {

    public String id;

    public SPacketSpeakerStop() {}

    public SPacketSpeakerStop(String id) {
        this.id = id;
    }

    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        var snd = Minecraft.getMinecraft().getSoundHandler().sndManager.sndSystem;
        if (snd == null) return;
        snd.stop(id);
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeInt(id.length());
        buf.writeString(id);
    }

    @Override
    public void decode(PacketBuffer buf) {
        id = buf.readString(buf.readInt());
    }
}
