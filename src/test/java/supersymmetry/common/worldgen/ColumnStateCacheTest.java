package supersymmetry.common.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ColumnStateCacheTest {

    @Test
    public void promotionCopiesAllValuesIntoTheOriginalLinearLayout() {
        int w = 7, h = 31, d = 11;
        ColumnStateCache<String> c = new ColumnStateCache<>(w, h, d);
        String[] oracle = new String[w * h * d];
        java.util.Random r = new java.util.Random(217);
        for (int i = 0; i < 2000; i++) {
            int x = r.nextInt(w), y = r.nextInt(h), z = r.nextInt(d);
            String value = "state" + i;
            c.put(x, y, z, value);
            oracle[(x * h + y) * d + z] = value;
        }
        String[] promoted = new String[w * h * d];
        c.copyInto(promoted);
        assertArrayEquals(oracle, promoted);
    }

    @Test
    public void sparseAccessStaysLazyAndDenseAccessPromotes() {
        ColumnStateCache<String> c = new ColumnStateCache<>(64, 256, 64);
        for (int x = 0; x < 64; x += 8) for (int z = 0; z < 64; z += 8) c.put(x, 20, z, "stone");
        assertFalse(c.shouldPromote());
        for (int x = 0; x < 8; x++) for (int z = 0; z < 64; z++) c.put(x, 30, z, "stone");
        assertTrue(c.shouldPromote());
    }

    @Test
    public void invalidOrOverflowingVolumesDelegateToTheOriginalCache() {
        assertTrue(ColumnStateCache.fitsDenseVolume(64, 256, 64));
        assertFalse(ColumnStateCache.fitsDenseVolume(0, 256, 64));
        assertFalse(ColumnStateCache.fitsDenseVolume(-1, 256, 64));
        assertFalse(ColumnStateCache.fitsDenseVolume(Integer.MAX_VALUE, 256, 64));
        assertFalse(ColumnStateCache.fitsDenseVolume(64, 256, 0));
    }

    @Test
    public void sparseReadsAllocateOnlyTouchedColumns() {
        ColumnStateCache<String> c = new ColumnStateCache<>(128, 256, 128);
        assertNull(c.get(100, 200, 120));
        assertEquals(0, c.allocatedColumns());
        c.put(100, 200, 120, "stone");
        c.put(100, 201, 120, "air");
        assertEquals(1, c.allocatedColumns());
        assertEquals("stone", c.get(100, 200, 120));
        assertEquals(128L * 128 + 256, c.referenceSlots());
    }

    @Test
    public void randomReadsAndWritesMatchDenseCache() {
        int w = 7, h = 31, d = 11;
        String[][][] oracle = new String[w][h][d];
        ColumnStateCache<String> c = new ColumnStateCache<>(w, h, d);
        java.util.Random r = new java.util.Random(916);
        for (int i = 0; i < 20000; i++) {
            int x = r.nextInt(w), y = r.nextInt(h), z = r.nextInt(d);
            assertEquals(oracle[x][y][z], c.get(x, y, z));
            if (r.nextBoolean()) {
                String value = "state" + i;
                oracle[x][y][z] = value;
                c.put(x, y, z, value);
            }
        }
    }

    @Test
    public void boundsNeverAliasAnotherColumn() {
        assertThrows(IndexOutOfBoundsException.class, () -> new ColumnStateCache<String>(4, 4, 4).get(4, 0, 0));
    }
}
