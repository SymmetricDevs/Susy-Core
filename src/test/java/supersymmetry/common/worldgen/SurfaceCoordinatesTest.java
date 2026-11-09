package supersymmetry.common.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import org.junit.jupiter.api.Test;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.StructurePlaceContext;

public class SurfaceCoordinatesTest {

    @Test
    public void templateCoordinatesMatchPlacementForAllRotationsMirrorsAndNegativeOrigins() {
        for (int rotation = 0; rotation < 4; rotation++) for (boolean mirror : new boolean[] { false, true })
            for (int[] size : new int[][] { { 3, 4, 7 }, { 16, 2, 32 }, { 1, 1, 9 } }) {
                AxisAlignedTransform2D t = new AxisAlignedTransform2D(rotation, mirror);
                int width = t.resultSwitchesXZ() ? size[2] : size[0], depth = t.resultSwitchesXZ() ? size[0] : size[2];
                StructureBoundingBox box = new StructureBoundingBox(-101, 7, 203, -101 + width - 1, 7 + size[1] - 1,
                        203 + depth - 1);
                StructurePlaceContext context = new StructurePlaceContext(new java.util.Random(1), null, t, box);
                for (int x = 0; x < size[0]; x++) for (int z = 0; z < size[2]; z++) {
                    BlockPos p = new BlockPos(x, 0, z);
                    BlockPos oracle = t.apply(p, size).add(-101, 7, 203);
                    assertEquals(oracle, SurfaceCoordinates.worldPosition(context, p));
                }
            }
    }
}
