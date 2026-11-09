package supersymmetry.common.worldgen;

import net.minecraft.util.math.BlockPos;

import ivorius.reccomplex.world.gen.feature.structure.generic.placement.StructurePlaceContext;

public final class SurfaceCoordinates {

    private SurfaceCoordinates() {}

    public static BlockPos worldPosition(StructurePlaceContext context, BlockPos local) {
        int x = context.boundingBox.getXSize(), y = context.boundingBox.getYSize(), z = context.boundingBox.getZSize();
        // Bounding box dimensions are already transformed; restore the original template dimensions for apply().
        int[] template = context.transform.resultSwitchesXZ() ? new int[] { z, y, x } : new int[] { x, y, z };
        return context.transform.apply(local, template).add(context.boundingBox.minX,
                context.boundingBox.minY, context.boundingBox.minZ);
    }
}
