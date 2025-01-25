package supersymmetry.integration.immersiverailroading.tracknet;

import cam72cam.immersiverailroading.tile.TileRail;
import net.minecraft.util.math.Vec3d;
import supersymmetry.integration.immersiverailroading.util.TrackUtil;

import java.util.List;

public class TrackSection /* extends INBTSerializable<NBTTagCompound>*/ {

    // This is arbitrary, since tracks don't have a preferred direction
    private TrackSectionBorder border_front;
    private TrackSectionBorder border_back;
    private TrackDirection direction;

    public List<TrackSection> getNeighboursInLeaveDirection(Vec3d enterPos) {
        return null;
    }

    public TrackSection(TileRail rail) {
        this.border_front = new TrackSectionBorder(TrackUtil.getRailEnd(rail, false).internal());
        this.border_back = new TrackSectionBorder(TrackUtil.getRailEnd(rail, true).internal());
    }

    public void addRail(TileRail rail, boolean back) {
        cam72cam.mod.math.Vec3d mergePos = TrackUtil.getRailEnd(rail, back);
        cam72cam.mod.math.Vec3d newBorderPos = TrackUtil.getRailEnd(rail, !back);

        if (border_back.closer(border_front, mergePos.internal())) {
            border_back.move(newBorderPos.internal());
        } else {
            border_front.move(newBorderPos.internal());
        }
    }


    public static TrackSection merge(TrackSection frontSection, TrackSection backSection, TileRail mergeRail) {
        Vec3d frontMergePos = TrackUtil.getRailEnd(mergeRail, false).internal();
        TrackSectionBorder newBorderFront;
        if(frontSection.border_front.closer(frontSection.border_back, frontMergePos)) {
            newBorderFront = frontSection.border_back;
        } else {
            newBorderFront = frontSection.border_front;
        }

        Vec3d backMergePos = TrackUtil.getRailEnd(mergeRail, true).internal();
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
        // border_front -> border_back
        FORWARD,
        BACKWARD,
        BOTH
    }
}
