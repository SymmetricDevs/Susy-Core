package supersymmetry.common.tileentities;

import java.util.concurrent.atomic.AtomicLong;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import gregtech.api.GregTechAPI;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.SimpleComponent;
import supersymmetry.common.blocks.BlockSpeaker;
import supersymmetry.common.network.SPacketSpeakerAudio;
import supersymmetry.common.network.SPacketSpeakerStop;
import supersymmetry.common.network.SpeakerCodec;

public class TileEntitySpeaker extends TileEntity implements SimpleComponent {

    // TODO put this into SusyConfig.java
    private static final int MIN_RATE = 128;
    // TODO put this into SusyConfig.java
    private static final int MAX_RATE = 22050;
    // TODO put this into SusyConfig.java
    private static final double MAX_DURATION = 2.0;
    // TODO put this into SusyConfig.java
    private static final double MIN_DURATION = 0.03;

    public static final int MAX_AUDIO_SIZE = (int) (MAX_RATE * MAX_DURATION * 2);

    protected final AtomicLong playbackEnd = new AtomicLong();
    protected String nodeAddress;
    public BlockSpeaker.BlockSpeakerType type;

    public TileEntitySpeaker() {
        super();
    }

    public TileEntitySpeaker(BlockSpeaker.BlockSpeakerType type) {
        this();
        this.type = type;
    }

    @Override
    public String getComponentName() {
        return String.format("speaker_%s", type.name().toLowerCase());
    }

    protected void validateAudio(int rate, byte[] data) {
        if (rate < MIN_RATE || rate > MAX_RATE) {
            throw new IllegalArgumentException(String.format("invalid rate, allowed: (%d..%d)", MIN_RATE, MAX_RATE));
        }
        if ((data.length & 1) != 0) {
            throw new IllegalArgumentException(
                    "data length must be even for AL_FORMAT_MONO16 (you need 16 bit chunks of audio, you are probably reading the .wav wrong)");
        }
        if (data.length > MAX_AUDIO_SIZE) {
            throw new IllegalArgumentException(
                    String.format(
                            "too much data for a single audio packet (max %.1fs of MONO16 at %dHz), split your sound into multiple chunks and play them sequentially",
                            MAX_DURATION, MAX_RATE));
        }
        // the *2 is here because data is [u8] but the audio playback takes in [u16]
        int maxSize = (int) (rate * MAX_DURATION * 2);
        if (data.length > maxSize) {
            throw new IllegalArgumentException(
                    String.format(
                            "too much data (max %.1fs of MONO16), split your sound into multiple chunks and play them sequentially",
                            MAX_DURATION, rate));
        }
        int minSize = Math.max(32, (int) (rate * MIN_DURATION * 2));
        if (data.length < minSize) {
            throw new IllegalArgumentException(
                    String.format("not enough data (min %.0fms of MONO16)", MIN_DURATION * 1000));
        }
    }

    protected synchronized Object[] playSound(Context ctx, Arguments args, boolean async) {
        var data = args.checkByteArray(1);
        var rate = args.checkInteger(0);
        validateAudio(rate, data);

        int len = data.length & ~1;
        long time_till_sound_stops_ms = (long) (len / (2.0 * rate) * 1000) - 1;

        long now = System.currentTimeMillis();
        if (now < playbackEnd.get()) {
            throw new IllegalStateException("this speaker is already playing!");
        }
        playbackEnd.set(now + time_till_sound_stops_ms);

        var node = ((Environment) this).node();
        if (nodeAddress == null) {
            nodeAddress = node.address();
        }

        GregTechAPI.networkHandler.sendToAllAround(
                new SPacketSpeakerAudio(node.address(), rate, this.getPos(), data, this.type.getRadius()),
                new TargetPoint(
                        this.getWorld().provider.getDimension(),
                        this.getPos().getX(),
                        this.getPos().getY(),
                        this.getPos().getZ(),
                        this.type.getRadius()));

        if (async) {
            ctx.pause(0.05);
            return new Object[] { time_till_sound_stops_ms };
        } else {
            ctx.pause((double) (time_till_sound_stops_ms) / 1000.0);
            return new Object[] { time_till_sound_stops_ms };
        }
    }

    @Callback(direct = true)
    public Object[] getMinRate(Context ctx, Arguments args) {
        return new Object[] { MIN_RATE };
    }

    @Callback(direct = true)
    public Object[] getMaxRate(Context ctx, Arguments args) {
        return new Object[] { MAX_RATE };
    }

    @Callback(direct = true)
    public Object[] getMinDuration(Context ctx, Arguments args) {
        return new Object[] { MIN_DURATION };
    }

    @Callback(direct = true)
    public Object[] getMaxDuration(Context ctx, Arguments args) {
        return new Object[] { MAX_DURATION };
    }


    @Callback(doc = "playSoundBlocking(rate:int,data:string) -- plays a sound from a MONO16 wave format")
    public Object[] playSoundBlocking(Context ctx, Arguments args) {
        return playSound(ctx, args, false);
    }

    @Callback(doc = "playSoundAsync(rate:int,data:string) -- same as the blocking version except it only blocks for 0.05s and returns the amount of time until the sound stops playing in ms")
    public Object[] playSoundAsync(Context ctx, Arguments args) {
        return playSound(ctx, args, true);
    }

    @Callback(doc = "stopSound() -- stops the currently playing sound on this speaker")
    public Object[] stopSound(Context ctx, Arguments args) {
        return stopSound(ctx);
    }

    protected synchronized Object[] stopSound(Context ctx) {
        if (System.currentTimeMillis() >= playbackEnd.get()) {
            throw new IllegalStateException("speaker is not playing");
        }
        playbackEnd.set(0);
        var node = ((Environment) this).node();
        if (nodeAddress == null) {
            // if node.address is not constant this will blow up
            nodeAddress = node.address();
        }

        GregTechAPI.networkHandler.sendToAllAround(
                new SPacketSpeakerStop(node.address()),
                new TargetPoint(
                        this.getWorld().provider.getDimension(),
                        this.getPos().getX(),
                        this.getPos().getY(),
                        this.getPos().getZ(),
                        this.type.getRadius()));
        ctx.pause(0.05);
        return new Object[] {};
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (nodeAddress != null) {
            SpeakerCodec.remove(nodeAddress);
        }
    }
}
