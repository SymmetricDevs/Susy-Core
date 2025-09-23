package supersymmetry.integration.immersiverailroading.control;

import static cam72cam.immersiverailroading.IRItems.*;

import java.util.ArrayList;
import java.util.List;

import cam72cam.immersiverailroading.items.nbt.RailSettings;
import cam72cam.immersiverailroading.library.TrackDirection;
import cam72cam.immersiverailroading.util.PlacementInfo;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.math.Vec3d;

/*
 * Helper for manipulating and placing track segments.
 */

public class TrackSegment {

    public PlacementInfo info;
    public RailSettings settings;

    public TrackSegment(RailSettings settings, PlacementInfo info) {
        this.info = info;
        this.settings = settings;
    }

    public TrackSegment(RailSettings settings, float yawHead, Vec3d hit) {
        ItemStack stack = new ItemStack(ITEM_TRACK_BLUEPRINT, 0);
        settings.write(stack);
        this.info = new PlacementInfo(stack, yawHead, hit);
        this.settings = settings;
    }

    public TrackSegment(RailSettings settings, Vec3d placementPosition, TrackDirection direction, float yaw,
                        Vec3d control) {
        this.settings = settings;
        this.info = new PlacementInfo(placementPosition, direction, yaw, control);
    }

    public TrackSegment(RailSettings settings, Vec3d placementPosition, TrackDirection direction, float yaw) {
        this.settings = settings;
        this.info = new PlacementInfo(placementPosition, direction, yaw, null);
    }

    public List<TrackSegment> split(int segmentLength) {
        if (segmentLength >= settings.length) {
            return null;
        }

        switch (settings.type) {
            case STRAIGHT:
                return splitStraight(segmentLength);
            default:
                return null;
        }
    }

    public List<TrackSegment> splitStraight(int segmentLength) {
        RailSettings.Mutable mutable = this.settings.mutable();
        List<TrackSegment> trackSegments = new ArrayList<>();
        TrackSegment prevSegment = null;
        for (int lengthToGo = this.settings.length; lengthToGo >
                0; lengthToGo = Math.max(0, lengthToGo - segmentLength)) {
            int length = Math.min(segmentLength, lengthToGo);
            mutable.length = length;
            RailSettings segmentSettings = mutable.immutable();
            PlacementInfo segmentInfo = this.info;
            if (prevSegment != null) {
                segmentInfo = new PlacementInfo(this.nextPos(), this.info.direction, this.info.yaw, null);
            }
            prevSegment = new TrackSegment(segmentSettings, segmentInfo);
            trackSegments.add(prevSegment);
        }
        return null;
    }

    public Vec3d nextPos() {
        switch (this.settings.type) {
            case STRAIGHT:
                return this.info.placementPosition
                        .add(new Vec3d(0, 0, this.settings.length + 1).rotateYaw(this.info.yaw));
            default:
                return this.info.placementPosition;
        }
    }
}
