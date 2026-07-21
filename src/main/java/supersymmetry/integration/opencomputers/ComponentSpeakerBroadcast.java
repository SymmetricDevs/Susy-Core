package supersymmetry.integration.opencomputers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;

import gregtech.api.GregTechAPI;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.EnvironmentHost;
import supersymmetry.common.network.SPacketSpeakerBroadcastAudio;
import supersymmetry.common.network.SPacketSpeakerBroadcastStop;
import supersymmetry.common.tileentities.TileEntitySpeaker;

public class ComponentSpeakerBroadcast extends ComponentSpeaker {

    public ComponentSpeakerBroadcast(EnvironmentHost host) {
        super(host, "speaker_broadcast", 64);
    }

    @Override
    protected synchronized Object[] playSound(Context ctx, Arguments args, boolean async) {
        var data = args.checkByteArray(1);
        var rate = args.checkInteger(0);
        TileEntitySpeaker.validateAudio(rate, data);

        int len = data.length & ~1;
        long time_till_sound_stops_ms = (long) (len / (2.0 * rate) * 1000) - 1;

        var peers = findBroadcastPeers();
        if (peers.isEmpty()) {
            throw new IllegalStateException("broadcast speaker is not connected to a network");
        }

        long now = System.currentTimeMillis();
        long end = now + time_till_sound_stops_ms;

        for (var peer : peers) {
            if (now < peer.playbackEnd.get() - 50) {
                throw new IllegalStateException("a broadcast speaker on this network is already playing audio");
            }
        }

        var targets = new ArrayList<SPacketSpeakerBroadcastAudio.Target>();
        var union = new AxisAlignedBB(getBlockPos()).grow(radius, radius, radius);

        for (var peer : peers) {
            peer.playbackEnd.set(end);
            if (peer.nodeAddress == null) {
                peer.nodeAddress = peer.node().address();
            }
            var pos = peer.getBlockPos();
            double px = pos.getX() + 0.5;
            double py = pos.getY() + 0.5;
            double pz = pos.getZ() + 0.5;
            targets.add(new SPacketSpeakerBroadcastAudio.Target(peer.nodeAddress, px, py, pz));
            union = union.union(new AxisAlignedBB(pos).grow(radius, radius, radius));
        }

        var packet = new SPacketSpeakerBroadcastAudio(rate, data, radius, targets);
        for (var player : host.world().playerEntities) {
            if (player instanceof EntityPlayerMP mp && union.intersects(mp.getEntityBoundingBox())) {
                GregTechAPI.networkHandler.sendTo(packet, mp);
            }
        }

        if (async) {
            ctx.pause(0.05);
            return new Object[] { time_till_sound_stops_ms };
        } else {
            ctx.pause((double) time_till_sound_stops_ms / 1000.0);
            return new Object[] { time_till_sound_stops_ms };
        }
    }

    @Override
    protected synchronized Object[] stopSound(Context ctx) {
        var peers = findBroadcastPeers();
        if (peers.isEmpty()) {
            throw new IllegalStateException("broadcast speaker is not connected to a network");
        }

        long now = System.currentTimeMillis();
        var anyPlaying = false;
        for (var peer : peers) {
            if (now < peer.playbackEnd.get()) {
                anyPlaying = true;
                break;
            }
        }
        if (!anyPlaying) {
            throw new IllegalStateException("no broadcast speaker is playing");
        }

        var sourceIds = new ArrayList<String>();
        var union = new AxisAlignedBB(getBlockPos()).grow(radius, radius, radius);

        for (var peer : peers) {
            if (now < peer.playbackEnd.get()) {
                peer.playbackEnd.set(0);
            }
            if (peer.nodeAddress == null) {
                peer.nodeAddress = peer.node().address();
            }
            sourceIds.add(peer.nodeAddress);
            union = union.union(new AxisAlignedBB(peer.getBlockPos()).grow(radius, radius, radius));
        }

        var packet = new SPacketSpeakerBroadcastStop(sourceIds);
        for (var player : host.world().playerEntities) {
            if (player instanceof EntityPlayerMP mp && union.intersects(mp.getEntityBoundingBox())) {
                GregTechAPI.networkHandler.sendTo(packet, mp);
            }
        }

        ctx.pause(0.05);
        return new Object[] {};
    }

    private List<ComponentSpeakerBroadcast> findBroadcastPeers() {
        var peers = new ArrayList<ComponentSpeakerBroadcast>();
        var network = node().network();
        if (network == null) return peers;
        for (var node : network.nodes()) {
            if (node.host() instanceof ComponentSpeakerBroadcast broadcast) {
                peers.add(broadcast);
            }
        }
        return peers;
    }
}
