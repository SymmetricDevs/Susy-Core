package supersymmetry.common.worldgen;

/** Primitive indexing and lazily allocated columns avoid a whole-height dense array and Integer indices. */
public final class ColumnStateCache<T> {

    private final int width, height, depth;
    private final Object[][] columns;
    private int allocated;

    public ColumnStateCache(int width, int height, int depth) {
        if (width <= 0 || height <= 0 || depth <= 0) throw new IllegalArgumentException("Invalid cache bounds");
        this.width = width;
        this.height = height;
        this.depth = depth;
        columns = new Object[Math.multiplyExact(width, depth)][];
    }

    private int index(int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) throw new IndexOutOfBoundsException();
        return x * depth + z;
    }

    @SuppressWarnings("unchecked")
    public T get(int x, int y, int z) {
        Object[] column = columns[index(x, y, z)];
        return column == null ? null : (T) column[y];
    }

    public void put(int x, int y, int z, T value) {
        int index = index(x, y, z);
        Object[] column = columns[index];
        if (column == null) {
            column = new Object[height];
            columns[index] = column;
            allocated++;
        }
        column[y] = value;
    }

    public int allocatedColumns() {
        return allocated;
    }

    public long referenceSlots() {
        return (long) width * depth + (long) allocated * height;
    }

    public static boolean fitsDenseVolume(int width, int height, int depth) {
        return width > 0 && height > 0 && depth > 0 && (long) width * height <= Integer.MAX_VALUE / depth;
    }

    public boolean shouldPromote() {
        return allocated >= Math.max(1, (columns.length + 7L) / 8);
    }

    @SuppressWarnings("unchecked")
    public void copyInto(T[] dense) {
        if (dense.length != Math.multiplyExact(Math.multiplyExact(width, height), depth))
            throw new IllegalArgumentException("Dense cache bounds differ");
        for (int x = 0; x < width; x++) for (int z = 0; z < depth; z++) {
            Object[] column = columns[x * depth + z];
            if (column == null) continue;
            int index = x * height * depth + z;
            for (int y = 0; y < height; y++, index += depth) dense[index] = (T) column[y];
        }
    }
}
