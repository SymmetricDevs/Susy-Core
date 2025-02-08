package supersymmetry.integration.immersiverailroading.tracknet;

import cam72cam.immersiverailroading.tile.TileRail;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import supersymmetry.integration.immersiverailroading.util.TrackUtil;

import java.util.ArrayList;
import java.util.List;

public class TrackSection /* extends INBTSerializable<NBTTagCompound>*/ {

    private TrackDirection direction;

    // Based on the direction of the first segment that was placed in a section, pretty much arbitrary
    private List<TrackSection> frontNeighbours;
    private List<TrackSection> backNeighbours;

    public Vec3i frontPos;
    public Vec3i backPos;

    public List<TrackSection> getNeighbours(TrackDirection leaveDirection) {
        switch (leaveDirection) {
            case FORWARD:
                return this.frontNeighbours;
            case BACKWARD:
                return this.backNeighbours;
        }
        return new ArrayList<>();
    }

    public TrackSection(TileRail rail) {

    }

    public void merge(TrackSection other, TileRail rail) {
        Vec3d frontMergePos = TrackUtil.getRailEnd(rail, false).internal();
        TrackSectionBorder newBorderFront;
        if(frontSection.border_front.closer(frontSection.border_back, frontMergePos)) {
            newBorderFront = frontSection.border_back;
        } else {
            newBorderFront = frontSection.border_front;
        }

        Vec3d backMergePos = TrackUtil.getRailEnd(rail, true).internal();
        TrackSectionBorder newBorderBack;
        if(backSection.border_front.closer(backSection.border_back, backMergePos)) {
            newBorderBack = backSection.border_back;
        } else {
            newBorderBack = backSection.border_front;
        }

        frontSection.border_front = newBorderFront;
        frontSection.border_back = newBorderBack;

        return frontSection;

        }

    private enum TrackDirection {
        // border_back -> border_front
        FORWARD,
        BACKWARD,
        BOTH
    }
}
