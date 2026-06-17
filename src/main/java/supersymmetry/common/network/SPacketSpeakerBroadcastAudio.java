package supersymmetry.common.network;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import paulscode.sound.SoundSystemConfig;

public class SPacketSpeakerBroadcastAudio implements IPacket, IClientExecutor {

    public int rate;
    public byte[] bytes;
    public int radius;
    public List<Target> targets;

    public static class Target {

        public String sourceId;
        public BlockPos pos;

        public Target() {}

        public Target(String sourceId, BlockPos pos) {
            this.sourceId = sourceId;
            this.pos = pos;
        }
    }

    public SPacketSpeakerBroadcastAudio() {}

    public SPacketSpeakerBroadcastAudio(int rate, byte[] bytes, int radius, List<Target> targets) {
        this.rate = rate;
        this.bytes = bytes;
        this.radius = radius;
        this.targets = targets;
    }

    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        var snd = Minecraft.getMinecraft().getSoundHandler().sndManager.sndSystem;
        if (snd == null) return;
        if (bytes.length == 0) return;
        if ((bytes.length & 1) != 0) return;

        var format = new AudioFormat(rate, 16, 1, true, false);

        var playerPos = Minecraft.getMinecraft().player.getPosition();

        targets.sort(
                (a, b) -> {
                    var da = playerPos.distanceSq(a.pos);
                    var db = playerPos.distanceSq(b.pos);
                    return Double.compare(da, db);
                });

        var chosen = new ArrayList<Target>();
        // picks 3 speakers, ignoring ones placed close together
        for (var target : targets) {
            boolean grouped = false;
            for (var c : chosen) {
                if (Math.abs(c.pos.getX() - target.pos.getX()) <= 4 &&
                        Math.abs(c.pos.getZ() - target.pos.getZ()) <= 4) {
                    grouped = true;
                    break;
                }
            }
            if (!grouped) {
                chosen.add(target);
                if (chosen.size() == 3) break;
            }
        }

        float vol = 1f / chosen.size();

        for (var target : chosen) {
            var sourceId = target.sourceId;

            if (snd.playing(sourceId)) {
                var decoder = SpeakerCodec.get(sourceId);
                if (decoder != null) decoder.buffers.add(bytes);
                continue;
            }

            var decoder = SpeakerCodec.prepare(sourceId, format);
            decoder.buffers.add(bytes);

            try {
                snd.newStreamingSource(
                        false,
                        sourceId,
                        new URI("file", null, String.format("/speaker_%s.speaker", sourceId), null).toURL(),
                        String.format("speaker_%s.speaker", sourceId),
                        false,
                        (float) target.pos.getX() + 0.5f,
                        (float) target.pos.getY() + 0.5f,
                        (float) target.pos.getZ() + 0.5f,
                        SoundSystemConfig.ATTENUATION_LINEAR,
                        radius);
            } catch (URISyntaxException | MalformedURLException e) {
                continue;
            }
            snd.play(sourceId);
            snd.setVolume(sourceId, vol *
                    Minecraft.getMinecraft().gameSettings.getSoundLevel(net.minecraft.util.SoundCategory.RECORDS));
        }
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeInt(rate);
        buf.writeInt(radius);

        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);

        buf.writeInt(targets.size());
        for (var target : targets) {
            buf.writeInt(target.sourceId.length());
            buf.writeString(target.sourceId);
            buf.writeBlockPos(target.pos);
        }
    }

    @Override
    public void decode(PacketBuffer buf) {
        rate = buf.readInt();
        radius = buf.readInt();

        int dataLen = buf.readInt();
        bytes = new byte[dataLen];
        buf.readBytes(bytes);

        int count = buf.readInt();
        targets = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            var target = new Target();
            target.sourceId = buf.readString(buf.readInt());
            target.pos = buf.readBlockPos();
            targets.add(target);
        }
    }
}
