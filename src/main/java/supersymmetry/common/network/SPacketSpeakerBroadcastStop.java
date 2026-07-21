package supersymmetry.common.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import supersymmetry.api.SusyLog;

public class SPacketSpeakerBroadcastStop implements IPacket, IClientExecutor {

    public List<String> sourceIds;

    public SPacketSpeakerBroadcastStop() {}

    public SPacketSpeakerBroadcastStop(List<String> sourceIds) {
        this.sourceIds = sourceIds;
    }

    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        var snd = Minecraft.getMinecraft().getSoundHandler().sndManager.sndSystem;
        if (snd == null) return;
        for (var id : sourceIds) {
            if (id == null || id.isEmpty()) {
                SusyLog.logger.error("broadcast stop packet with missing id");
                continue;
            }
            SPacketSpeakerAudio.TRACKED.remove(id);
            snd.stop(id);
        }
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeInt(sourceIds.size());
        for (var id : sourceIds) {
            buf.writeInt(id.length());
            buf.writeString(id);
        }
    }

    @Override
    public void decode(PacketBuffer buf) {
        int count = buf.readInt();
        sourceIds = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            sourceIds.add(buf.readString(buf.readInt()));
        }
    }
}
