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

    private static final int MIN_RATE = 32;
    // TODO put this into SusyConfig.java
    private static final int MAX_RATE = 11025;
    // TODO put this into SusyConfig.java
    private static final double MAX_DURATION = 1.5;
    // TODO put this into SusyConfig.java
    private static final double MIN_DURATION = 0.05;
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
            throw new IllegalArgumentException("invalid rate");
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

    protected Object[] playSound(Context ctx, Arguments args, boolean async) {
        var data = args.checkByteArray(1);
        var rate = args.checkInteger(0);
        validateAudio(rate, data);

        int len = data.length & ~1;
        long time_till_sound_stops_ms = (long) (len / (2.0 * rate) * 1000) - 1;

        // dumb spinlock to prevent a race condition caused by 2 oc lua threads making a call at the exact same time
        // dont even know if thats a possible fail case because oliwier throws the "wouldnt you like to know" line
        // when asked
        while (true) {
            long prev = playbackEnd.get();
            if (System.currentTimeMillis() < prev) {
                throw new IllegalStateException("this speaker is already playing!");
            }
            if (playbackEnd.compareAndSet(prev, System.currentTimeMillis() + time_till_sound_stops_ms)) {
                break;
            }
        }

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
            return new Object[] {};
        }
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

    protected Object[] stopSound(Context ctx) {
        long prev = playbackEnd.get();
        if (System.currentTimeMillis() >= prev) {
            throw new IllegalStateException("speaker is not playing");
        }
        if (!playbackEnd.compareAndSet(prev, 0)) {
            throw new IllegalStateException("speaker playback state changed");
        }
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
