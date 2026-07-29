package supersymmetry.common.network;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.sound.sampled.AudioFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;

import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;
import paulscode.sound.SoundSystemConfig;
import supersymmetry.api.SusyLog;
import supersymmetry.common.tileentities.TileEntitySpeaker;

public class SPacketSpeakerAudio implements IPacket, IClientExecutor {

    static final Map<String, Entity> TRACKED = new ConcurrentHashMap<>();

    public static void tickTracked() {
        var snd = Minecraft.getMinecraft().getSoundHandler().sndManager.sndSystem;
        if (snd == null) return;
        TRACKED.entrySet().removeIf(e -> {
            var entity = e.getValue();
            if (entity == null || entity.isDead) return true;
            if (!snd.playing(e.getKey())) return true;
            snd.setPosition(e.getKey(),
                    (float) entity.posX, (float) entity.posY, (float) entity.posZ);
            return false;
        });
    }

    public String id;
    public int rate;
    public double posX;
    public double posY;
    public double posZ;
    public byte[] bytes;
    public int radius;
    public UUID entityUuid;

    public SPacketSpeakerAudio() {}

    public SPacketSpeakerAudio(String id, int rate, double posX, double posY, double posZ, byte[] bytes, int radius,
                               UUID entityUuid) {
        this.id = id;
        this.rate = rate;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.bytes = bytes;
        this.radius = radius;
        this.entityUuid = entityUuid;
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

        var tracked = trackEntity(id, entityUuid);
        float sx, sy, sz;
        if (tracked != null) {
            sx = (float) tracked.posX;
            sy = (float) tracked.posY;
            sz = (float) tracked.posZ;
        } else {
            sx = (float) posX;
            sy = (float) posY;
            sz = (float) posZ;
        }

        if (snd.playing(id)) {
            var decoder = SpeakerCodec.get(id);
            if (decoder != null) decoder.buffers.add(bytes);
            snd.setPosition(id, sx, sy, sz);
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
                    sx, sy, sz,
                    SoundSystemConfig.ATTENUATION_LINEAR,
                    radius);
        } catch (URISyntaxException | MalformedURLException e) {
            SusyLog.logger.error("failed to play speaker sound: {}", e.getMessage());
            return;
        }
        snd.play(id);
        snd.setVolume(id, 0.7f * Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.RECORDS));
    }

    private static Entity trackEntity(String sourceId, UUID uuid) {
        if (uuid == null) return null;
        var mc = Minecraft.getMinecraft();
        if (mc.world == null) return null;
        for (var e : mc.world.loadedEntityList) {
            if (e.getUniqueID().equals(uuid)) {
                TRACKED.put(sourceId, e);
                return e;
            }
        }
        return null;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeInt(id.length());
        buf.writeString(id);

        buf.writeInt(rate);
        buf.writeDouble(posX);
        buf.writeDouble(posY);
        buf.writeDouble(posZ);
        buf.writeInt(radius);

        buf.writeBoolean(entityUuid != null);
        if (entityUuid != null) {
            buf.writeUniqueId(entityUuid);
        }

        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    @Override
    public void decode(PacketBuffer buf) {
        id = buf.readString(buf.readInt());

        rate = buf.readInt();
        posX = buf.readDouble();
        posY = buf.readDouble();
        posZ = buf.readDouble();
        radius = buf.readInt();

        if (buf.readBoolean()) {
            entityUuid = buf.readUniqueId();
        } else {
            entityUuid = null;
        }

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
