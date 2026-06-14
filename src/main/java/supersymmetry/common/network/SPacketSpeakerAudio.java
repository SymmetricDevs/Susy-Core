package supersymmetry.common.network;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.sound.sampled.AudioFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import paulscode.sound.SoundSystemConfig;
import supersymmetry.api.SusyLog;
import supersymmetry.common.tileentities.TileEntitySpeaker;

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
        var snd = Minecraft.getMinecraft().getSoundHandler().sndManager.sndSystem;
        if (snd == null) return;
        if (id == null || id.isEmpty()) {
            SusyLog.logger.error("speaker packet with missing id");
            return;
        }
        if (bytes.length == 0) return;
        if ((bytes.length & 1) != 0) {
            SusyLog.logger.error("odd number of bytes in a u16 sound array, this shouldve been checked server side");
            return;
        }

        if (snd.playing(id)) {
            var decoder = SpeakerCodec.get(id);
            if (decoder != null) decoder.buffers.add(bytes);
            return;
        }

        var format = new AudioFormat(rate, 16, 1, true, false);
        var decoder = SpeakerCodec.prepare(id, format);

        decoder.buffers.add(bytes);

        try {
            snd.newStreamingSource(
                    false,
                    id,
                    new URI("file", null, String.format("/speaker_%s.speaker", id), null).toURL(),
                    String.format("speaker_%s.speaker", id),
                    false,
                    (float) pos.getX() + 0.5f,
                    (float) pos.getY() + 0.5f,
                    (float) pos.getZ() + 0.5f,
                    SoundSystemConfig.ATTENUATION_NONE,
                    1f);
        } catch (URISyntaxException | MalformedURLException e) {
            SusyLog.logger.error("failed to play speaker sound: {}", e.getMessage());
            return;
        }
        snd.play(id);
        snd.setVolume(id, 1.0f);
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
        if (len > TileEntitySpeaker.MAX_AUDIO_SIZE) {
            SusyLog.logger.error("oversized audio packet ({} bytes, max {})", len, TileEntitySpeaker.MAX_AUDIO_SIZE);
            buf.skipBytes(len);
            bytes = new byte[0];
            return;
        }
        bytes = new byte[len];
        buf.readBytes(bytes);
    }
}
