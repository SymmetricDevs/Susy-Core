package supersymmetry.common.network;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import supersymmetry.api.SusyLog;

public class SPacketSpeakerAudio implements IPacket, IClientExecutor {

    public String id;
    public int rate;
    public BlockPos pos;
    public byte[] bytes;

    public SPacketSpeakerAudio() {}

    public SPacketSpeakerAudio(String id, int rate, BlockPos pos, byte[] bytes) {
        this.id = id;
        this.rate = rate;
        this.pos = pos;
        this.bytes = bytes;
    }

    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        SusyLog.logger.info(
                "SPacketSpeakerAudio.executeClient id={} rate={} pos={} bytes={}", id, rate, pos, bytes);
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeInt(id.length());

        buf.writeString(id);
        buf.writeInt(rate);
        buf.writeBlockPos(pos);

        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    @Override
    public void decode(PacketBuffer buf) {
        id = buf.readString(buf.readInt());
        rate = buf.readInt();
        pos = buf.readBlockPos();

        int len = buf.readInt();
        bytes = new byte[len];
        buf.readBytes(bytes);
    }
}
