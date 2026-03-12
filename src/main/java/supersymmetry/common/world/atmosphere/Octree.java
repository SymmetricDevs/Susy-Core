package supersymmetry.common.world.atmosphere;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

public class Octree implements Iterable<BlockPos> {

    private static final int MIN_SIZE = 1;

    private enum State { EMPTY, FULL, MIXED }

    private State state;
    private Octree[] children;
    private final int originX, originY, originZ;
    private final int size;
    private int count;
    private Octree root;

    public Octree(BlockPos origin, int size) {
        this(origin.getX(), origin.getY(), origin.getZ(), size);
    }

    public Octree(int originX, int originY, int originZ, int size) {
        if (size < 1 || (size & (size - 1)) != 0) {
            throw new IllegalArgumentException("size must be a power of 2, got " + size);
        }
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.size = size;
        this.state = State.EMPTY;
        this.count = 0;
        this.root = this;
    }

    private Octree(int originX, int originY, int originZ, int size, Octree root) {
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.size = size;
        this.state = State.EMPTY;
        this.count = 0;
        this.root = root;
    }

    public boolean insert(BlockPos pos) { return insert(pos.getX(), pos.getY(), pos.getZ()); }
    public boolean remove(BlockPos pos) { return remove(pos.getX(), pos.getY(), pos.getZ()); }
    public boolean contains(BlockPos pos) { return contains(pos.getX(), pos.getY(), pos.getZ()); }
    public int size() { return count; }
    public boolean isEmpty() { return state == State.EMPTY; }

    public Set<BlockPos> getBoundaryPositions() { return getBoundaryPositions(pos -> false); }

    public Set<BlockPos> getBoundaryPositions(Predicate<BlockPos> isSolid) {
        Set<BlockPos> result = new HashSet<>();
        collectBoundary(result, isSolid);
        return result;
    }

    public List<BlockPos> getAllPositions() {
        List<BlockPos> result = new ArrayList<>(count);
        collectAll(result);
        return result;
    }

    @Override
    public Iterator<BlockPos> iterator() { return new OctreeIterator(this); }

    private boolean insert(int px, int py, int pz) {
        if (!inBounds(px, py, pz)) return false;
        if (state == State.FULL) return false;
        if (size == MIN_SIZE) { state = State.FULL; count = 1; return true; }
        if (state == State.EMPTY) { expand(); }
        int idx = childIndex(px, py, pz);
        boolean added = children[idx].insert(px, py, pz);
        if (added) { count++; tryCollapse(); }
        return added;
    }

    private boolean remove(int px, int py, int pz) {
        if (!inBounds(px, py, pz)) return false;
        if (state == State.EMPTY) return false;
        if (size == MIN_SIZE) {
            if (state == State.FULL) { state = State.EMPTY; count = 0; return true; }
            return false;
        }
        if (state == State.FULL) { splitFull(); }
        int idx = childIndex(px, py, pz);
        boolean removed = children[idx].remove(px, py, pz);
        if (removed) { count--; tryCollapse(); }
        return removed;
    }

    private boolean contains(int px, int py, int pz) {
        if (!inBounds(px, py, pz)) return false;
        if (state == State.FULL) return true;
        if (state == State.EMPTY) return false;
        return children[childIndex(px, py, pz)].contains(px, py, pz);
    }

    private void collectBoundary(Set<BlockPos> result, Predicate<BlockPos> isSolid) {
        if (state == State.EMPTY) return;
        if (state == State.FULL && size == MIN_SIZE) {
            BlockPos pos = new BlockPos(originX, originY, originZ);
            for (BlockPos nb : BlockPosUtil.neighbors(pos)) {
                if (!root.contains(nb) || isSolid.test(nb)) { result.add(pos); return; }
            }
            return;
        }
        if (state == State.FULL) { collectFullBoundary(result, isSolid); return; }
        for (Octree child : children) { child.collectBoundary(result, isSolid); }
    }

    private void collectFullBoundary(Set<BlockPos> result, Predicate<BlockPos> isSolid) {
        for (int a = originX; a < originX + size; a++) {
            for (int b = originY; b < originY + size; b++) {
                for (int c = originZ; c < originZ + size; c++) {
                    boolean onFace = (a == originX || a == originX + size - 1 ||
                                      b == originY || b == originY + size - 1 ||
                                      c == originZ || c == originZ + size - 1);
                    if (!onFace) continue;
                    BlockPos pos = new BlockPos(a, b, c);
                    for (BlockPos nb : BlockPosUtil.neighbors(pos)) {
                        if (!root.contains(nb) || isSolid.test(nb)) { result.add(pos); break; }
                    }
                }
            }
        }
    }

    private void collectAll(List<BlockPos> result) {
        if (state == State.EMPTY) return;
        if (state == State.FULL) {
            if (size == MIN_SIZE) { result.add(new BlockPos(originX, originY, originZ)); }
            else {
                for (int x = originX; x < originX + size; x++)
                    for (int y = originY; y < originY + size; y++)
                        for (int z = originZ; z < originZ + size; z++)
                            result.add(new BlockPos(x, y, z));
            }
            return;
        }
        for (Octree child : children) { child.collectAll(result); }
    }

    private int childIndex(int px, int py, int pz) {
        int half = size / 2;
        int ix = (px - originX) < half ? 0 : 1;
        int iy = (py - originY) < half ? 0 : 1;
        int iz = (pz - originZ) < half ? 0 : 1;
        return ix | (iy << 1) | (iz << 2);
    }

    private void expand() {
        state = State.MIXED;
        children = new Octree[8];
        int half = size / 2;
        for (int i = 0; i < 8; i++) {
            int cx = originX + ((i & 1) != 0 ? half : 0);
            int cy = originY + ((i & 2) != 0 ? half : 0);
            int cz = originZ + ((i & 4) != 0 ? half : 0);
            children[i] = new Octree(cx, cy, cz, half, root);
        }
    }

    private void splitFull() {
        state = State.MIXED;
        children = new Octree[8];
        int half = size / 2;
        int childVolume = half * half * half;
        for (int i = 0; i < 8; i++) {
            int cx = originX + ((i & 1) != 0 ? half : 0);
            int cy = originY + ((i & 2) != 0 ? half : 0);
            int cz = originZ + ((i & 4) != 0 ? half : 0);
            Octree child = new Octree(cx, cy, cz, half, root);
            child.state = State.FULL;
            child.count = childVolume;
            children[i] = child;
        }
    }

    private void tryCollapse() {
        if (children == null) return;
        boolean allFull = true, allEmpty = true;
        for (Octree c : children) {
            if (c.state != State.FULL) allFull = false;
            if (c.state != State.EMPTY) allEmpty = false;
        }
        if (allFull) { state = State.FULL; children = null; count = size * size * size; }
        else if (allEmpty) { state = State.EMPTY; children = null; count = 0; }
    }

    private boolean inBounds(int px, int py, int pz) {
        return px >= originX && px < originX + size &&
               py >= originY && py < originY + size &&
               pz >= originZ && pz < originZ + size;
    }

    public int getOriginX() { return originX; }
    public int getOriginY() { return originY; }
    public int getOriginZ() { return originZ; }
    public int getTreeSize() { return size; }

    // ---- Serialization ----

    /**
     * Serialize the octree structure as a compact byte array.
     * Encoding: 0=EMPTY, 1=FULL, 2=MIXED (followed by 8 children recursively).
     */
    public byte[] serialize() {
        // Estimate upper bound: each node is 1 byte, max nodes is bounded by tree structure
        List<Byte> bytes = new ArrayList<>();
        serializeNode(this, bytes);
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = bytes.get(i);
        }
        return result;
    }

    private static void serializeNode(Octree node, List<Byte> bytes) {
        if (node.state == State.EMPTY) {
            bytes.add((byte) 0);
        } else if (node.state == State.FULL) {
            bytes.add((byte) 1);
        } else {
            bytes.add((byte) 2);
            for (Octree child : node.children) {
                serializeNode(child, bytes);
            }
        }
    }

    /**
     * Deserialize an octree from a byte array produced by {@link #serialize()}.
     */
    public static Octree deserialize(int originX, int originY, int originZ, int size, byte[] data) {
        Octree tree = new Octree(originX, originY, originZ, size);
        int[] idx = {0};
        deserializeNode(tree, data, idx);
        return tree;
    }

    private static void deserializeNode(Octree node, byte[] data, int[] idx) {
        if (idx[0] >= data.length) return;
        byte b = data[idx[0]++];
        if (b == 0) {
            node.state = State.EMPTY;
            node.count = 0;
        } else if (b == 1) {
            node.state = State.FULL;
            node.count = node.size * node.size * node.size;
        } else {
            node.expand();
            int total = 0;
            for (int i = 0; i < 8; i++) {
                deserializeNode(node.children[i], data, idx);
                total += node.children[i].count;
            }
            node.count = total;
            node.tryCollapse();
        }
    }

    private static class OctreeIterator implements Iterator<BlockPos> {

        private static class Frame {
            final Octree node;
            int childIdx;
            int flatIdx;
            Frame(Octree node) { this.node = node; this.childIdx = 0; this.flatIdx = 0; }
        }

        private final List<Frame> stack = new ArrayList<>();
        private BlockPos next;

        OctreeIterator(Octree root) {
            if (root.state != State.EMPTY) { stack.add(new Frame(root)); }
            advance();
        }

        @Override public boolean hasNext() { return next != null; }

        @Override public BlockPos next() {
            if (next == null) throw new NoSuchElementException();
            BlockPos ret = next; advance(); return ret;
        }

        private void advance() {
            next = null;
            while (!stack.isEmpty()) {
                Frame frame = stack.get(stack.size() - 1);
                Octree node = frame.node;
                if (node.state == State.FULL) {
                    int vol = node.size * node.size * node.size;
                    if (frame.flatIdx < vol) {
                        int idx = frame.flatIdx++;
                        int lz = idx % node.size;
                        int ly = (idx / node.size) % node.size;
                        int lx = idx / (node.size * node.size);
                        next = new BlockPos(node.originX + lx, node.originY + ly, node.originZ + lz);
                        return;
                    }
                    stack.remove(stack.size() - 1); continue;
                }
                if (node.state == State.MIXED) {
                    boolean pushed = false;
                    while (frame.childIdx < 8) {
                        Octree child = node.children[frame.childIdx++];
                        if (child.state != State.EMPTY) { stack.add(new Frame(child)); pushed = true; break; }
                    }
                    if (!pushed) { stack.remove(stack.size() - 1); }
                    continue;
                }
                stack.remove(stack.size() - 1);
            }
        }
    }
}
