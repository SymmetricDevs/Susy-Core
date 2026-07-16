package supersymmetry.common.network;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.sound.sampled.AudioFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
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
    public int radius;

    public SPacketSpeakerAudio() {}

    public SPacketSpeakerAudio(String id, int rate, BlockPos pos, byte[] bytes, int radius) {
        this.id = id;
        this.rate = rate;
        this.pos = pos;
        this.bytes = bytes;
        this.radius = radius;
    }

    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        // TODO highpass maybe?
        // also could make it also work with susy planets, so that there is no sound in space
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
                    SoundSystemConfig.ATTENUATION_LINEAR,
                    radius);
        } catch (URISyntaxException | MalformedURLException e) {
            SusyLog.logger.error("failed to play speaker sound: {}", e.getMessage());
            return;
        }
        snd.play(id);
        snd.setVolume(id, 0.7f * Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS));
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeInt(id.length());
        buf.writeString(id);

        buf.writeInt(rate);
        buf.writeBlockPos(pos);
        buf.writeInt(radius);

        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    @Override
    public void decode(PacketBuffer buf) {
        id = buf.readString(buf.readInt());

        rate = buf.readInt();
        pos = buf.readBlockPos();
        radius = buf.readInt();

        int len = buf.readInt();
        if (len > TileEntitySpeaker.getMaxAudioSize()) {
            SusyLog.logger.error("oversized audio packet ({} bytes, max {})", len, TileEntitySpeaker.getMaxAudioSize());
            buf.skipBytes(len);
            bytes = new byte[0];
            return;
        }
        bytes = new byte[len];
        buf.readBytes(bytes);
    }
}
