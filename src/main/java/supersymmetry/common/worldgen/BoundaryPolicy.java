package supersymmetry.common.worldgen;

/** Deliberately independent of Minecraft so boundary behavior is exhaustively testable. */
public final class BoundaryPolicy {

    private BoundaryPolicy() {}

    public static boolean shouldDefer(int sourceX, int sourceZ, int blockX, int blockZ,
                                      boolean loaded, boolean populated) {
        return (sourceX != (blockX >> 4) || sourceZ != (blockZ >> 4)) && (!loaded || !populated);
    }

    public static long chunkKey(int x, int z) {
        return ((long) x << 32) | (z & 0xffffffffL);
    }
}
