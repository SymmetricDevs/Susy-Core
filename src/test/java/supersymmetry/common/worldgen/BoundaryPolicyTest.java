package supersymmetry.common.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class BoundaryPolicyTest {

    @Test
    public void ownChunkAlwaysDelegatesIncludingUnpopulatedChunk() {
        for (int x = -8; x <= 8; x++) for (int z = -8; z <= 8; z++) for (int offset : new int[] { 0, 7, 15 })
            assertFalse(BoundaryPolicy.shouldDefer(x, z, x * 16 + offset, z * 16 + offset, false, false));
    }

    @Test
    public void unloadedRemoteAndUnpopulatedRemoteWait() {
        assertTrue(BoundaryPolicy.shouldDefer(0, 0, 16, 0, false, false));
        assertTrue(BoundaryPolicy.shouldDefer(0, 0, -1, 0, true, false));
        assertFalse(BoundaryPolicy.shouldDefer(0, 0, -1, 0, true, true));
    }

    @Test
    public void negativeCoordinateBoundaryUsesFloorNotTruncation() {
        assertFalse(BoundaryPolicy.shouldDefer(-1, -1, -16, -1, false, false));
        assertTrue(BoundaryPolicy.shouldDefer(0, 0, -1, -1, false, false));
    }

    @Test
    public void chunkKeysDoNotCollideAcrossSignedAxes() {
        java.util.Set<Long> keys = new java.util.HashSet<>();
        for (int x : new int[] { Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE })
            for (int z : new int[] { Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE })
                assertTrue(keys.add(BoundaryPolicy.chunkKey(x, z)));
    }
}
