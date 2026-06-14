package supersymmetry.common.tileentities;

import net.minecraft.nbt.NBTTagCompound;
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

public class TileEntitySpeaker extends TileEntity implements SimpleComponent {

    private static final int MIN_RATE = 32;
    // TODO put this into SusyConfig.java
    private static final int MAX_RATE = 11025;
    // TODO put this into SusyConfig.java
    private static final double MAX_DURATION = 1.5;
    // TODO put this into SusyConfig.java
    private static final double MIN_DURATION = 0.05;
    private static final int MAX_AUDIO_SIZE = (int) (MAX_RATE * MAX_DURATION * 2);

    private long playbackEnd;
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

    private Object[] playSound(Context ctx, Arguments args, boolean async) {
        if (System.currentTimeMillis() < playbackEnd) {
            throw new IllegalStateException("this speaker is already playing!");
        }
        var data = args.checkByteArray(1);
        var rate = args.checkInteger(0);
        if (rate < MIN_RATE || rate > MAX_RATE) {
            throw new IllegalArgumentException("invalid rate");
        }
        if ((data.length & 1) != 0) {
            throw new IllegalArgumentException(
                    "data length must be even for AL_FORMAT_MONO16 (you need 16 bit chunks of audio)");
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

        var node = ((Environment) this).node();

        GregTechAPI.networkHandler.sendToAllAround(
                new SPacketSpeakerAudio(node.address(), rate, this.getPos(), data),
                new TargetPoint(
                        this.getWorld().provider.getDimension(),
                        this.getPos().getX(),
                        this.getPos().getY(),
                        this.getPos().getZ(),
                        this.type.getRadius()));

        int len = data.length & ~1;
        long time_till_sound_stops_ms = (long) (len / (2.0 * rate) * 1000) + 1;
        playbackEnd = System.currentTimeMillis() + time_till_sound_stops_ms;

        if (async) {
            ctx.pause(0.05);
            return new Object[] { time_till_sound_stops_ms };
        } else {
            ctx.pause((double) (playbackEnd + 1) / 1000.0);
            return new Object[] {};
        }
    }

    @Callback(doc = "playSoundBlocking(rate:int,data:string) -- plays a sound and blocks until it finishes")
    public Object[] playSoundBlocking(Context ctx, Arguments args) {
        return playSound(ctx, args, false);
    }

    @Callback(doc = "playSoundAsync(rate:int,data:string) -- plays a sound and returns immediately with the duration in ms")
    public Object[] playSoundAsync(Context ctx, Arguments args) {
        return playSound(ctx, args, true);
    }

    @Callback(doc = "stopSound() -- stops the currently playing sound on this speaker")
    public Object[] stopSound(Context ctx, Arguments args) {
        if (System.currentTimeMillis() >= playbackEnd) {
            throw new IllegalStateException("speaker is not playing");
        }

        var node = ((Environment) this).node();

        GregTechAPI.networkHandler.sendToAllAround(
                new SPacketSpeakerStop(node.address()),
                new TargetPoint(
                        this.getWorld().provider.getDimension(),
                        this.getPos().getX(),
                        this.getPos().getY(),
                        this.getPos().getZ(),
                        this.type.getRadius()));

        playbackEnd = 0;
        ctx.pause(0.05);
        return new Object[] {};
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("type")) {
            type = BlockSpeaker.BlockSpeakerType.valueOf(nbt.getString("type"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (type != null) {
            nbt.setString("type", type.name());
        }
        return nbt;
    }
}
