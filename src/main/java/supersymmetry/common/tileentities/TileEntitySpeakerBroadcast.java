package supersymmetry.common.tileentities;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;

import gregtech.api.GregTechAPI;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import supersymmetry.common.blocks.BlockSpeaker;
import supersymmetry.common.network.SPacketSpeakerBroadcastAudio;
import supersymmetry.common.network.SPacketSpeakerBroadcastStop;

public class TileEntitySpeakerBroadcast extends TileEntitySpeaker {

    public TileEntitySpeakerBroadcast() {
        super(BlockSpeaker.BlockSpeakerType.BROADCAST);
    }

    public TileEntitySpeakerBroadcast(BlockSpeaker.BlockSpeakerType type) {
        super(type);
    }

    @Override
    protected Object[] playSound(Context ctx, Arguments args, boolean async) {
        var data = args.checkByteArray(1);
        var rate = args.checkInteger(0);
        validateAudio(rate, data);

        int len = data.length & ~1;
        long time_till_sound_stops_ms = (long) (len / (2.0 * rate) * 1000) - 1;

        var peers = findBroadcastPeers();

        long now = System.currentTimeMillis();
        long end = now + time_till_sound_stops_ms;

        for (var peer : peers) {
            if (now < peer.playbackEnd.get()) {
                throw new IllegalStateException("a broadcast speaker on this network is already playing audio");
            }
        }

        int radius = this.type.getRadius();
        var targets = new ArrayList<SPacketSpeakerBroadcastAudio.Target>();
        // maybe thats a bad way of doing it?
        var union = new AxisAlignedBB(peers.get(0).getPos()).grow(radius, radius, radius);

        for (var peer : peers) {
            peer.playbackEnd.set(end);
            if (peer.nodeAddress == null) {
                peer.nodeAddress = ((Environment) peer).node().address();
            }
            targets.add(new SPacketSpeakerBroadcastAudio.Target(peer.nodeAddress, peer.getPos()));
            union = union.union(new AxisAlignedBB(peer.getPos()).grow(radius, radius, radius));
        }

        var packet = new SPacketSpeakerBroadcastAudio(rate, data, radius, targets);
        for (var player : this.getWorld().playerEntities) {
            if (player instanceof EntityPlayerMP mp && union.intersects(mp.getEntityBoundingBox())) {
                GregTechAPI.networkHandler.sendTo(packet, mp);
            }
        }

        if (async) {
            ctx.pause(0.05);
            return new Object[] { time_till_sound_stops_ms };
        } else {
            ctx.pause((double) time_till_sound_stops_ms / 1000.0);
            return new Object[] {};
        }
    }

    @Override
    protected Object[] stopSound(Context ctx) {
        var peers = findBroadcastPeers();

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

        int radius = this.type.getRadius();
        var sourceIds = new ArrayList<String>();
        var union = new AxisAlignedBB(peers.get(0).getPos()).grow(radius, radius, radius);

        for (var peer : peers) {
            if (now < peer.playbackEnd.get()) {
                peer.playbackEnd.set(0);
            }
            if (peer.nodeAddress == null) {
                peer.nodeAddress = ((Environment) peer).node().address();
            }
            sourceIds.add(peer.nodeAddress);
            union = union.union(new AxisAlignedBB(peer.getPos()).grow(radius, radius, radius));
        }

        var packet = new SPacketSpeakerBroadcastStop(sourceIds);
        for (var player : this.getWorld().playerEntities) {
            if (player instanceof EntityPlayerMP mp && union.intersects(mp.getEntityBoundingBox())) {
                GregTechAPI.networkHandler.sendTo(packet, mp);
            }
        }

        ctx.pause(0.05);
        return new Object[] {};
    }

    private List<TileEntitySpeakerBroadcast> findBroadcastPeers() {
        var peers = new ArrayList<TileEntitySpeakerBroadcast>();
        var network = ((Environment) this).node().network();
        if (network == null) return peers;
        for (var node : network.nodes()) {
            if (node.host() instanceof TileEntitySpeakerBroadcast broadcast) {
                peers.add(broadcast);
            }
        }
        return peers;
    }
}
