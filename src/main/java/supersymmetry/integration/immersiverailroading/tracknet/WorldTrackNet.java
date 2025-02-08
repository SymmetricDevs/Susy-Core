package supersymmetry.integration.immersiverailroading.tracknet;

import cam72cam.immersiverailroading.library.TrackItems;
import cam72cam.immersiverailroading.tile.TileRail;
import cam72cam.immersiverailroading.tile.TileRailBase;
import cam72cam.mod.math.Vec3i;
import gregtech.api.util.world.DummyWorld;
import net.minecraft.nbt.NBTTagCompound;
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

    private Map<Vec3i, TrackSection> trackSectionByRailPos = new HashMap<>();

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

        // Is a switch
        if (rail.findSwitchParent() != null) {
            // When a switch is built,
            if (rail.info.settings.type.equals(TrackItems.TURN)) {
                return;
            }

            return;
        }

        TileRailBase frontRail = TrackUtil.nextRail(rail, false);
        TileRailBase backRail = TrackUtil.nextRail(rail, true);


        if(frontRail == null && backRail == null) {
            TrackSection newSection = new TrackSection(rail);
            trackSectionByRailPos.put(rail.getPos(), newSection);
            return;
        }

        TrackSection frontSection = null;

        if (frontRail != null) {
            frontSection = trackSectionByRailPos.get(frontRail.getPos());
        }

        if (backRail != null) {
            TrackSection backSection = trackSectionByRailPos.get(backRail.getPos());
            if (frontSection != null) {
                frontSection.merge(backSection, rail);
                trackSectionByRailPos.put(rail.getPos(), frontSection);
                replaceSection(backSection, frontSection);
            } else {
                trackSectionByRailPos.put(rail.getPos(), backSection);
            }
        } else {
            trackSectionByRailPos.put(rail.getPos(), frontSection);
        }

    }

    public void handleTrackRemoved(TileRail rail) {

    }

    public void replaceSection(TrackSection oldSection, TrackSection newSection) {
        trackSectionByRailPos.replaceAll((pos, section) -> section == oldSection ? newSection : section);
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
