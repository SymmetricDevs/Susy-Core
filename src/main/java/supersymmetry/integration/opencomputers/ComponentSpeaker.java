package supersymmetry.integration.opencomputers;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import gregtech.api.GregTechAPI;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import supersymmetry.SusyConfig;
import supersymmetry.common.network.SPacketSpeakerAudio;
import supersymmetry.common.network.SPacketSpeakerStop;
import supersymmetry.common.network.SpeakerCodec;
import supersymmetry.common.tileentities.TileEntitySpeaker;

public class ComponentSpeaker extends AbstractManagedEnvironment {

    protected final EnvironmentHost host;
    protected final int radius;
    protected final AtomicLong playbackEnd = new AtomicLong();
    protected String nodeAddress;

    public ComponentSpeaker(EnvironmentHost host, String componentName, int radius) {
        this.host = host;
        this.radius = radius;
        setNode(Network.newNode(this, Visibility.Network)
                .withComponent(componentName)
                .create());
    }

    protected BlockPos getBlockPos() {
        return new BlockPos(host.xPosition(), host.yPosition(), host.zPosition());
    }

    @Callback(direct = true)
    public Object[] getMinRate(Context ctx, Arguments args) {
        return new Object[] { SusyConfig.speakerMinRate };
    }

    @Callback(direct = true)
    public Object[] getMaxRate(Context ctx, Arguments args) {
        return new Object[] { SusyConfig.speakerMaxRate };
    }

    @Callback(direct = true)
    public Object[] getMinDuration(Context ctx, Arguments args) {
        return new Object[] { SusyConfig.speakerMinDuration };
    }

    @Callback(direct = true)
    public Object[] getMaxDuration(Context ctx, Arguments args) {
        return new Object[] { SusyConfig.speakerMaxDuration };
    }

    @Callback(doc = "playSoundBlocking(rate:int,data:string) -- plays a sound from a byte array string, has to be MONO16")
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

    protected synchronized Object[] playSound(Context ctx, Arguments args, boolean async) {
        var data = args.checkByteArray(1);
        var rate = args.checkInteger(0);
        TileEntitySpeaker.validateAudio(rate, data);

        int len = data.length & ~1;
        long time_till_sound_stops_ms = (long) (len / (2.0 * rate) * 1000) - 1;

        long now = System.currentTimeMillis();
        if (now < playbackEnd.get()) {
            throw new IllegalStateException("this speaker is already playing!");
        }
        playbackEnd.set(now + time_till_sound_stops_ms);

        var node = node();
        if (nodeAddress == null) {
            nodeAddress = node.address();
        }

        double x = host.xPosition();
        double y = host.yPosition();
        double z = host.zPosition();

        UUID entityUuid = host instanceof Entity ? ((Entity) host).getUniqueID() : null;

        GregTechAPI.networkHandler.sendToAllAround(
                new SPacketSpeakerAudio(node.address(), rate, x, y, z, data, this.radius, entityUuid),
                new TargetPoint(
                        host.world().provider.getDimension(),
                        x, y, z,
                        this.radius));

        if (async) {
            ctx.pause(0.05);
            return new Object[] { time_till_sound_stops_ms };
        } else {
            ctx.pause((double) (time_till_sound_stops_ms) / 1000.0);
            return new Object[] { time_till_sound_stops_ms };
        }
    }

    protected synchronized Object[] stopSound(Context ctx) {
        var node = node();
        if (nodeAddress == null) {
            nodeAddress = node.address();
        }

        if (System.currentTimeMillis() >= playbackEnd.get()) {
            throw new IllegalStateException("speaker is not playing");
        }
        playbackEnd.set(0);

        GregTechAPI.networkHandler.sendToAllAround(
                new SPacketSpeakerStop(node.address()),
                new TargetPoint(
                        host.world().provider.getDimension(),
                        host.xPosition(), host.yPosition(), host.zPosition(),
                        this.radius));
        ctx.pause(0.05);
        return new Object[] {};
    }

    @Override
    public void onDisconnect(Node node) {
        super.onDisconnect(node);
        if (nodeAddress != null) {
            SpeakerCodec.remove(nodeAddress);
        }
    }
}
