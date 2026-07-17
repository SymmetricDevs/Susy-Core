package supersymmetry.common.tileentities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import supersymmetry.SusyConfig;
import supersymmetry.integration.opencomputers.ComponentSpeaker;

public class TileEntitySpeaker extends TileEntity implements Environment, EnvironmentHost {

    public static int getMaxAudioSize() {
        return (int) (SusyConfig.speakerMaxRate * SusyConfig.speakerMaxDuration * 2);
    }

    public static void validateAudio(int rate, byte[] data) {
        if (rate < SusyConfig.speakerMinRate || rate > SusyConfig.speakerMaxRate) {
            throw new IllegalArgumentException(String.format("invalid rate, allowed: (%d..%d)",
                    SusyConfig.speakerMinRate, SusyConfig.speakerMaxRate));
        }
        if ((data.length & 1) != 0) {
            throw new IllegalArgumentException(
                    "data length must be even for AL_FORMAT_MONO16 (you need 16 bit chunks of audio, you are probably reading the .wav wrong)");
        }
        if (data.length > getMaxAudioSize()) {
            throw new IllegalArgumentException(
                    String.format(
                            "too much data for a single audio packet (max %.1fs of MONO16 at %dHz), split your sound into multiple chunks and play them sequentially",
                            SusyConfig.speakerMaxDuration, SusyConfig.speakerMaxRate));
        }
        int maxSize = (int) (rate * SusyConfig.speakerMaxDuration * 2);
        if (data.length > maxSize) {
            throw new IllegalArgumentException(
                    String.format(
                            "too much data (max %.1fs of MONO16), split your sound into multiple chunks and play them sequentially",
                            SusyConfig.speakerMaxDuration, rate));
        }
        int minSize = Math.max(32, (int) (rate * SusyConfig.speakerMinDuration * 2));
        if (data.length < minSize) {
            throw new IllegalArgumentException(
                    String.format("not enough data (min %.0fms of MONO16)", SusyConfig.speakerMinDuration * 1000));
        }
    }

    protected ComponentSpeaker speaker;

    public TileEntitySpeaker() {
        speaker = new ComponentSpeaker(this, "speaker_single", 32);
    }

    // Environment delegation

    @Override
    public Node node() {
        return speaker.node();
    }

    @Override
    public void onConnect(Node node) {
        speaker.onConnect(node);
    }

    @Override
    public void onDisconnect(Node node) {
        speaker.onDisconnect(node);
    }

    @Override
    public void onMessage(Message message) {
        speaker.onMessage(message);
    }

    // EnvironmentHost

    @Override
    public double xPosition() {
        return getPos().getX() + 0.5;
    }

    @Override
    public double yPosition() {
        return getPos().getY() + 0.5;
    }

    @Override
    public double zPosition() {
        return getPos().getZ() + 0.5;
    }

    @Override
    public World world() {
        return getWorld();
    }

    @Override
    public void markChanged() {
        markDirty();
    }

    @Override
    public void onLoad() {
        Network.joinOrCreateNetwork(this);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (speaker.node() != null) {
            speaker.node().remove();
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (speaker.node() != null) {
            speaker.node().remove();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        speaker.load(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        speaker.save(nbt);
        return nbt;
    }
}
