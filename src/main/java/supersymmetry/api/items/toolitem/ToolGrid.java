package supersymmetry.api.items.toolitem;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

import gregtech.api.util.GTUtility;

/**
 * Coordinates for icons inside the wrench grid.
 * <p>
 * Ported from GTCEu-Reloaded 43140b63f (LGPL-3.0), adapted to this project's package layout.
 */
public final class ToolGrid {

    private ToolGrid() {}

    public static Vec3d pointOnFace(EnumFacing face, double right, double up, double outward) {
        switch (face) {
            case SOUTH:
                return new Vec3d(0.5 + right, 0.5 + up, 0.5 + outward);
            case NORTH:
                return new Vec3d(0.5 - right, 0.5 + up, 0.5 - outward);
            case WEST:
                return new Vec3d(0.5 - outward, 0.5 + up, 0.5 + right);
            case EAST:
                return new Vec3d(0.5 + outward, 0.5 + up, 0.5 - right);
            case UP:
                return new Vec3d(0.5 + right, 0.5 + outward, 0.5 - up);
            case DOWN:
            default:
                return new Vec3d(0.5 + right, 0.5 - outward, 0.5 + up);
        }
    }

    public static EnumFacing sideAtCell(EnumFacing face, int column, int row) {
        if (column < -1 || column > 1 || row < -1 || row > 1) {
            throw new IllegalArgumentException("Grid cell must be between -1 and 1");
        }
        Vec3d point = pointOnFace(face, column * 0.375, row * 0.375, 0.5);
        return GTUtility.determineWrenchingSide(face, (float) point.x, (float) point.y, (float) point.z);
    }
}
