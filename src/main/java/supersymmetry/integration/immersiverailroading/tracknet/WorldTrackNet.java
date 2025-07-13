package supersymmetry.integration.immersiverailroading.tracknet;

import cam72cam.immersiverailroading.tile.TileRail;
import gregtech.api.util.world.DummyWorld;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.SusyLog;
import supersymmetry.integration.immersiverailroading.util.TrackUtil;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

// Partly based on the GT nets
public class WorldTrackNet extends WorldSavedData {
    private WeakReference<World> worldRef = new WeakReference(null);

    private static final String DATA_ID_BASE = "susy.track_net";

    private Map<Vec3d, TrackNode> nodes = new HashMap<>();
    private Map<TileRail, TrackSection> tileRailToTrackSection = new HashMap<>();


    public WorldTrackNet(String name) {
        super(name);
    }

    public World getWorld() {
        return this.worldRef.get();
    }

    protected void setWorldAndInit(World world) {
        if (world != this.getWorld()) {
            this.worldRef = new WeakReference(world);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {

    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return null;
    }

    public void handleNewTrack(TileRail rail) {

        Vec3d frontPos = TrackUtil.getRailEnd(rail, false).internal();
        Vec3d backPos = TrackUtil.getRailEnd(rail, true).internal();

        TrackNode frontNode = nodes.get(frontPos);
        TrackNode backNode = nodes.get(backPos);



        if (frontNode != null && backNode != null && frontNode.mergeable() && backNode.mergeable()) {
            TrackSection frontSection = frontNode.trackSections.get(0);
            TrackSection backSection = backNode.trackSections.get(0);

            TrackNode start = frontSection.oppositeNode(frontNode);
            TrackNode end = backSection.oppositeNode(backNode);

            TrackSection merged = new TrackSection(start, end);
            merged.rails.addAll(frontSection.rails);
            merged.rails.add(rail);
            merged.rails.addAll(backSection.rails);

            nodes.remove(frontPos);
            nodes.remove(backPos);
            start.trackSections.remove(frontSection);
            end.trackSections.remove(backSection);
            for (TileRail r :
                    merged.rails) {
                tileRailToTrackSection.put(r, merged);
            }

            start.trackSections.add(merged);
            end.trackSections.add(merged);
        } else if (frontNode != null && frontNode.mergeable()) {
            TrackSection section = frontNode.trackSections.get(0);
            section.rails.add(rail);
            tileRailToTrackSection.put(rail, section);

            frontNode.setPosition(backPos);
            nodes.remove(frontPos);
            nodes.put(backPos, frontNode);
        } else if (backNode != null && backNode.mergeable()) {
            TrackSection section = backNode.trackSections.get(0);
            section.rails.add(rail);
            tileRailToTrackSection.put(rail, section);

            backNode.setPosition(frontPos);
            nodes.remove(backPos);
            nodes.put(frontPos, backNode);
        } else if(backNode == null && frontNode == null) {
            frontNode = new TrackNode(frontPos);
            backNode = new TrackNode(backPos);

            TrackSection newSection = new TrackSection(frontNode, backNode);
            frontNode.trackSections.add(newSection);
            backNode.trackSections.add(newSection);

            nodes.put(frontPos, frontNode);
            nodes.put(backPos, backNode);

            tileRailToTrackSection.put(rail, newSection);
            newSection.rails.add(rail);

        }


    }

    public void handleTrackRemoved(TileRail rail) {

    }
    public static WorldTrackNet getWorldTrackNet(World world) {
        String DATA_ID = getDataID(DATA_ID_BASE, world);
        if (world instanceof DummyWorld) return null;
        WorldTrackNet worldTrackData = (WorldTrackNet) world.loadData(WorldTrackNet.class, DATA_ID);
        if (worldTrackData == null) {
            worldTrackData = new WorldTrackNet(DATA_ID);
            world.setData(DATA_ID, worldTrackData);
        }

        worldTrackData.setWorldAndInit(world);
        return worldTrackData;
    }

    public static @NotNull String getDataID(@NotNull String baseID, @NotNull World world) {
        if (world == null || world.isRemote) {
            SusyLog.logger.error("WorldPipeNet should only be created on the server!", new Throwable());
            if (world == null) {
                return baseID;
            }
        }

        int dimension = world.provider.getDimension();
        return dimension == 0 ? baseID : baseID + '.' + dimension;
    }
}
