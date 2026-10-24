package supersymmetry.common.faction;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

/**
 * custom A* zalgorithm, the default vanilla algorithm does not work for our purposes
 * the vanilla climber algorithm does not actually path towards the target in 3D, it just uses the ground algorithm
 * with the ability to climb up walls and approach the target in a straight line after doing so, instead of the
 * intricate 3D movements
 * that we are trying to achieve here.
 **/

public class FactionAStar {

    private static final int MAX_NODES = 2048;
    private static final int HEAP_CAPACITY = MAX_NODES * 4;
    private static final int MAX_NEIGHBORS = 32;

    public static final int MAX_CLIMB_HEIGHT = 8;
    public static final int MAX_DROP_HEIGHT = 8;

    private static final int[] CARDINAL_DX = { 1, -1, 0, 0 };
    private static final int[] CARDINAL_DZ = { 0, 0, 1, -1 };

    private final IBlockAccess world;
    private final EntityLiving entity;
    private final int entityHeight;

    private final Long2IntOpenHashMap indexByPos = new Long2IntOpenHashMap(MAX_NODES);
    private final int[] nodeX = new int[MAX_NODES];
    private final int[] nodeY = new int[MAX_NODES];
    private final int[] nodeZ = new int[MAX_NODES];
    private final int[] nodeParent = new int[MAX_NODES];
    private final double[] nodeG = new double[MAX_NODES];
    private final double[] nodeH = new double[MAX_NODES];
    private final double[] nodeF = new double[MAX_NODES];
    private final boolean[] nodeClosed = new boolean[MAX_NODES];
    private int nodeCount;

    private final int[] heapIdx = new int[HEAP_CAPACITY];
    private final double[] heapF = new double[HEAP_CAPACITY];
    private int heapSize;

    private final int[] neighborX = new int[MAX_NEIGHBORS];
    private final int[] neighborY = new int[MAX_NEIGHBORS];
    private final int[] neighborZ = new int[MAX_NEIGHBORS];
    private int neighborCount;

    private static final double MIN_PROGRESS = 3.0D;
    private boolean searchFailed;

    public FactionAStar(IBlockAccess world, EntityLiving entity) {
        this.world = world;
        this.entity = entity;
        this.entityHeight = (int) Math.ceil(entity.height);
        this.indexByPos.defaultReturnValue(-1);
    }

    public Path findPath(BlockPos start, BlockPos end) {
        return findPath(start, end, 0, 0);
    }

    public Path findPath(BlockPos start, BlockPos end, int horizTol, int vertTol) {
        searchFailed = false;                // stays false if the budget denies the search
        if (!FactionPathBudget.tryAcquire(entity.world)) return null;

        reset();

        double startH = heuristic(start.getX(), start.getY(), start.getZ(), end);
        int startIdx = createNode(start.getX(), start.getY(), start.getZ(), -1, 0.0, startH);
        heapPush(startIdx, nodeF[startIdx]);

        int bestIdx = startIdx;
        int iterations = 0;

        while (heapSize > 0 && iterations++ < MAX_NODES) {
            int currentIdx = heapPop();
            if (nodeClosed[currentIdx]) continue;
            nodeClosed[currentIdx] = true;

            if (nodeH[currentIdx] < nodeH[bestIdx]) bestIdx = currentIdx;

            int cx = nodeX[currentIdx], cy = nodeY[currentIdx], cz = nodeZ[currentIdx];

            if (Math.abs(cx - end.getX()) <= horizTol &&
                    Math.abs(cz - end.getZ()) <= horizTol &&
                    Math.abs(cy - end.getY()) <= vertTol) {
                return buildPath(currentIdx);
            }

            // ... collectNeighbors + neighbor loop exactly as before ...
        }

        searchFailed = (startH - nodeH[bestIdx]) < MIN_PROGRESS;
        return buildPath(bestIdx);
    }

    private void reset() {
        indexByPos.clear();
        nodeCount = 0;
        heapSize = 0;
        neighborCount = 0;
    }

    private int createNode(int x, int y, int z, int parent, double g, double h) {
        int idx = nodeCount++;
        nodeX[idx] = x;
        nodeY[idx] = y;
        nodeZ[idx] = z;
        nodeParent[idx] = parent;
        nodeG[idx] = g;
        nodeH[idx] = h;
        nodeF[idx] = g + h;
        nodeClosed[idx] = false;
        indexByPos.put(packPos(x, y, z), idx);
        return idx;
    }

    private void heapPush(int idx, double f) {
        if (heapSize >= HEAP_CAPACITY) return;
        int i = heapSize++;
        heapIdx[i] = idx;
        heapF[i] = f;
        while (i > 0) {
            int parent = (i - 1) >> 1;
            if (heapF[parent] <= heapF[i]) break;
            swapHeap(i, parent);
            i = parent;
        }
    }

    private int heapPop() {
        int result = heapIdx[0];
        heapSize--;
        if (heapSize > 0) {
            heapIdx[0] = heapIdx[heapSize];
            heapF[0] = heapF[heapSize];
            int i = 0;
            while (true) {
                int left = (i << 1) + 1;
                int right = left + 1;
                int smallest = i;
                if (left < heapSize && heapF[left] < heapF[smallest]) smallest = left;
                if (right < heapSize && heapF[right] < heapF[smallest]) smallest = right;
                if (smallest == i) break;
                swapHeap(i, smallest);
                i = smallest;
            }
        }
        return result;
    }

    private void swapHeap(int a, int b) {
        int ti = heapIdx[a];
        heapIdx[a] = heapIdx[b];
        heapIdx[b] = ti;
        double tf = heapF[a];
        heapF[a] = heapF[b];
        heapF[b] = tf;
    }

    private void collectNeighbors(int cx, int cy, int cz) {
        neighborCount = 0;

        // Climb up if adjacent to a wall.
        if (isAdjacentToWall(cx, cy, cz) && isClearForEntity(cx, cy + 1, cz)) {
            addNeighbor(cx, cy + 1, cz);
        }

        for (int d = 0; d < 4; d++) {
            int dx = CARDINAL_DX[d];
            int dz = CARDINAL_DZ[d];

            // Flat walk
            if (isWalkable(cx + dx, cy, cz + dz)) {
                addNeighbor(cx + dx, cy, cz + dz);
                continue;
            }

            // Step up (1 block)
            if (isWalkable(cx + dx, cy + 1, cz + dz) && isClearAt(cx, cy + entityHeight, cz)) {
                addNeighbor(cx + dx, cy + 1, cz + dz);
                continue;
            }

            // Wall climb
            for (int h = 2; h <= MAX_CLIMB_HEIGHT; h++) {
                boolean headroomClear = true;
                for (int ch = 0; ch < h + entityHeight - 1; ch++) {
                    if (!isClearAt(cx, cy + 1 + ch, cz)) {
                        headroomClear = false;
                        break;
                    }
                }
                if (!headroomClear) break;

                if (isWalkable(cx + dx, cy + h, cz + dz)) {
                    addNeighbor(cx + dx, cy + h, cz + dz);
                    break;
                }
            }

            // Drop
            for (int drop = 1; drop <= MAX_DROP_HEIGHT; drop++) {
                if (!isClearForEntity(cx + dx, cy - drop + 1, cz + dz)) break;
                if (isWalkable(cx + dx, cy - drop, cz + dz)) {
                    addNeighbor(cx + dx, cy - drop, cz + dz);
                    break;
                }
            }
        }

        // Ladders
        boolean ladderHere = isLadderAt(cx, cy, cz);
        if (ladderHere || isLadderAt(cx, cy + 1, cz)) {
            if (isClearAt(cx, cy + 1, cz)) {
                addNeighbor(cx, cy + 1, cz);
            }
        }
        if (ladderHere || isLadderAt(cx, cy - 1, cz)) {
            if (!isSolidAt(cx, cy - 1, cz)) {
                addNeighbor(cx, cy - 1, cz);
            }
        }
    }

    private void addNeighbor(int x, int y, int z) {
        if (neighborCount >= MAX_NEIGHBORS) return;
        neighborX[neighborCount] = x;
        neighborY[neighborCount] = y;
        neighborZ[neighborCount] = z;
        neighborCount++;
    }

    private boolean isAdjacentToWall(int x, int y, int z) {
        return isSolidBlock(x, y, z - 1) || isSolidBlock(x, y, z + 1) || isSolidBlock(x + 1, y, z) ||
                isSolidBlock(x - 1, y, z);
    }

    private boolean isSolidBlock(int x, int y, int z) {
        return world.getBlockState(new BlockPos(x, y, z)).getMaterial().isSolid();
    }

    private boolean isWalkable(int x, int y, int z) {
        return isSolidAt(x, y - 1, z) && isClearForEntity(x, y, z);
    }

    private boolean isClearForEntity(int x, int y, int z) {
        for (int i = 0; i < entityHeight; i++) {
            if (!isPassable(x, y + i, z)) return false;
        }
        return true;
    }

    private boolean isClearAt(int x, int y, int z) {
        return isPassable(x, y, z);
    }

    private boolean isPassable(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        IBlockState below = world.getBlockState(new BlockPos(x, y - 1, z));

        PathNodeType hazard = FactionTrapHazardEvaluator.getHazardNodeType(state, below);
        if (hazard == PathNodeType.BLOCKED) return false;

        if (isDoor(state)) return true;

        return !state.getMaterial().isSolid();
    }

    private boolean isSolidAt(int x, int y, int z) {
        IBlockState state = world.getBlockState(new BlockPos(x, y, z));
        IBlockState below = world.getBlockState(new BlockPos(x, y - 1, z));

        PathNodeType hazard = FactionTrapHazardEvaluator.getHazardNodeType(state, below);
        if (hazard == PathNodeType.BLOCKED) return true;

        return state.getMaterial().isSolid();
    }

    private boolean isDoor(IBlockState state) {
        Block block = state.getBlock();
        return block instanceof BlockDoor && block != Blocks.IRON_DOOR;
    }

    private boolean isLadderAt(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().isLadder(state, world, pos, null);
    }

    private double moveCost(int fx, int fy, int fz, int tx, int ty, int tz) {
        int dx = Math.abs(tx - fx);
        int dy = ty - fy;
        int dz = Math.abs(tz - fz);

        double cost = dx + dz;
        if (dy > 0) {
            cost += dy * 1.2;
        } else if (dy < 0) {
            cost += Math.abs(dy) * 0.5;
        }
        return cost;
    }

    private double heuristic(int ax, int ay, int az, BlockPos b) {
        return Math.abs(ax - b.getX()) + Math.abs(ay - b.getY()) + Math.abs(az - b.getZ());
    }

    private Path buildPath(int endIdx) {
        int length = 0;
        for (int cur = endIdx; cur >= 0; cur = nodeParent[cur]) length++;

        PathPoint[] points = new PathPoint[length];
        int cur = endIdx;
        for (int i = length - 1; i >= 0; i--) {
            PathPoint pp = new PathPoint(nodeX[cur], nodeY[cur], nodeZ[cur]);
            pp.index = i;
            points[i] = pp;
            cur = nodeParent[cur];
        }

        return new Path(points);
    }

    private static long packPos(int x, int y, int z) {
        long xi = (long) (x & 0x3FFFFFF);
        long yi = (long) (y & 0xFFF);
        long zi = (long) (z & 0x3FFFFFF);
        return xi << 38 | yi << 26 | zi;
    }

    public static final class FactionPathBudget {

        private static final int MAX_PER_TICK = 6;
        private static long tick = -1;
        private static int used = 0;

        public static boolean tryAcquire(World world) {
            long now = world.getTotalWorldTime();
            if (now != tick) {
                tick = now;
                used = 0;
            }
            if (used >= MAX_PER_TICK) return false;
            used++;
            return true;
        }
    }

    public boolean lastSearchFailed() {
        return searchFailed;
    }
}
