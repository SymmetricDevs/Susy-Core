package supersymmetry.integration.immersiverailroading.util;

import cam72cam.immersiverailroading.tile.TileRail;
import cam72cam.immersiverailroading.tile.TileRailBase;
import cam72cam.immersiverailroading.track.BuilderBase;
import cam72cam.immersiverailroading.track.BuilderCubicCurve;
import cam72cam.immersiverailroading.track.CubicCurve;
import cam72cam.immersiverailroading.track.PosStep;
import cam72cam.immersiverailroading.util.VecUtil;
import cam72cam.mod.math.Vec3i;

public class TrackUtil {

    public static PosStep getRailEnd(TileRail rail, boolean back) {
        BuilderBase builderBase = rail.info.getBuilder(rail.getWorld());
        if (builderBase == null) {
            return null;
        }
        if (builderBase instanceof BuilderCubicCurve builderCubicCurve) {
            CubicCurve curve = builderCubicCurve.getCurve();
            Vec3i pos = new Vec3i(rail.info.placementInfo.placementPosition.add(rail.getPos()));
            return back ? new PosStep(curve.p1.add(pos), curve.angleStart() -180, 0) : new PosStep(curve.p2.add(pos), curve.angleStop(), 0);
        }

        return null;
    }

    public static TileRail nextRail(TileRail rail, boolean back) {
        PosStep railEnd = getRailEnd(rail, back);
        Vec3i pos = new Vec3i(railEnd.add(VecUtil.fromYaw(1.0, railEnd.yaw)));
        //TileRailBase railBase = rail.getWorld().getBlockEntity(pos, TileRailBase.class);
        TileRailBase railBase = rail.getWorld().getBlockEntity(new Vec3i(railEnd), TileRailBase.class);
        return railBase == null ? null : railBase.getParentTile();
    }

    public static TileRail nextRail(TileRail rail, PosStep railEnd) {
        Vec3i pos = new Vec3i(railEnd.add(VecUtil.fromYaw(1.0, railEnd.yaw)));
        //TileRailBase railBase = rail.getWorld().getBlockEntity(pos, TileRailBase.class);
        TileRailBase railBase = rail.getWorld().getBlockEntity(new Vec3i(railEnd), TileRailBase.class);
        return railBase == null ? null : railBase.getParentTile();
    }
}
