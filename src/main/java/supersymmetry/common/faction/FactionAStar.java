package supersymmetry.common.faction;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.*;

/**
 custom A* zalgorithm, the default vanilla algorithm does not work for our purposes
 the vanilla climber algorithm does not actually path towards the target in 3D, it just uses the ground algorithm
 with the ability to climb up walls and approach the target in a straight line after doing so, instead of the intricate 3D movements
 that we are trying to achieve here.
 **/

public class FactionAStar {

    private static final int MAX_NODES = 4096;
    public static final int MAX_CLIMB_HEIGHT = 8; //misleading, not actually max climb height, more like how high does the check run. too lazy to refactor name good luck everyone else
    public static final int MAX_DROP_HEIGHT = 8;

    private final IBlockAccess world;
    private final EntityLiving entity;
    private final int entityHeight;

    public FactionAStar(IBlockAccess world, EntityLiving entity) {
        this.world = world;
        this.entity = entity;
        this.entityHeight = (int) Math.ceil(entity.height);
    }

    public Path findPath(BlockPos start, BlockPos end) {
        Map<BlockPos, Node> allNodes = new HashMap<>();
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Set<BlockPos> closed = new HashSet<>();

        Node startNode = new Node(start, null, 0, heuristic(start, end));
        allNodes.put(start, startNode);
        open.add(startNode);

        int iterations = 0;
        Node closest = startNode;

        while (!open.isEmpty() && iterations++ < MAX_NODES) {
            Node current = open.poll();

            if (closed.contains(current.pos)) continue;
            closed.add(current.pos);

            if (current.heuristic < closest.heuristic) {
                closest = current;
            }

            if (current.pos.equals(end)) {
                return buildPath(current);
            }

            for (BlockPos neighbor : getNeighbors(current.pos)) {
                if (closed.contains(neighbor)) continue;

                double moveCost = current.g + moveCost(current.pos, neighbor);
                Node existing = allNodes.get(neighbor);

                if (existing == null || moveCost < existing.g) {
                    Node next = new Node(neighbor, current, moveCost, heuristic(neighbor, end));
                    allNodes.put(neighbor, next);
                    open.add(next);
                }
            }
        }

        return buildPath(closest);
    }

    private List<BlockPos> getNeighbors(BlockPos pos) {
        List<BlockPos> neighbors = new ArrayList<>();
        int[][] cardinals = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        if (isAdjacentToWall(pos) && isClearForEntity(pos.up())) {
            neighbors.add(pos.up());
        }

        for (int[] dir : cardinals) {
            int dx = dir[0], dz = dir[1];

            BlockPos flat = pos.add(dx, 0, dz);
            if (isWalkable(flat)) {
                neighbors.add(flat);
                continue;
            }

            BlockPos stepUp = pos.add(dx, 1, dz);
            if (isWalkable(stepUp) && isClearAt(pos.up(entityHeight))) {
                neighbors.add(stepUp);
                continue;
            }

            for (int h = 2; h <= MAX_CLIMB_HEIGHT; h++) {
                boolean headroomClear = true;
                for (int ch = 0; ch < h + entityHeight - 1; ch++) {
                    if (!isClearAt(pos.up(1 + ch))) {
                        headroomClear = false;
                        break;
                    }
                }
                if (!headroomClear) break;

                BlockPos wallTop = pos.add(dx, h, dz);
                if (isWalkable(wallTop)) {
                    neighbors.add(wallTop);
                    break;
                }
            }

            for (int drop = 1; drop <= MAX_DROP_HEIGHT; drop++) {
                BlockPos dropPos = pos.add(dx, -drop, dz);
                if (!isClearForEntity(pos.add(dx, -drop + 1, dz))) break;

                if (isWalkable(dropPos)) {
                    neighbors.add(dropPos);
                    break;
                }
            }
        }

        if (isLadderAt(pos) || isLadderAt(pos.up())) {
            BlockPos up = pos.up();
            if (isClearAt(up)) neighbors.add(up);
        }
        if (isLadderAt(pos) || isLadderAt(pos.down())) {
            BlockPos down = pos.down();
            if (!isSolidAt(down)) neighbors.add(down);
        }

        return neighbors;
    }

    private boolean isAdjacentToWall(BlockPos pos) {
        IBlockState state;
        return (state = world.getBlockState(pos.north())).getMaterial().isSolid() ||
                (state = world.getBlockState(pos.south())).getMaterial().isSolid() ||
                (state = world.getBlockState(pos.east())).getMaterial().isSolid()  ||
                (state = world.getBlockState(pos.west())).getMaterial().isSolid();
    }

    private boolean isWalkable(BlockPos pos) {
        return isSolidAt(pos.down()) && isClearForEntity(pos);
    }

    private boolean isClearForEntity(BlockPos pos) {
        for (int i = 0; i < entityHeight; i++) {
            if (!isPassable(pos.up(i))) return false;
        }
        return true;
    }

    private boolean isClearAt(BlockPos pos) {
        return isPassable(pos);
    }

    private boolean isPassable(BlockPos pos) {
        PathNodeType hazard = FactionTrapHazardEvaluator.getHazardNodeType(world, pos);
        if (hazard == PathNodeType.BLOCKED) return false;

        if (isDoor(pos)) return true;

        return !world.getBlockState(pos).getMaterial().isSolid();
    }

    private boolean isDoor(BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof BlockDoor;
    }

    private boolean isSolidAt(BlockPos pos) {
        PathNodeType hazard = FactionTrapHazardEvaluator.getHazardNodeType(world, pos);
        if (hazard == PathNodeType.BLOCKED) return true;
        return world.getBlockState(pos).getMaterial().isSolid();
    }

    private boolean isLadderAt(BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock().isLadder(state, world, pos, null);
    }

    private double moveCost(BlockPos from, BlockPos to) {
        int dx = Math.abs(to.getX() - from.getX());
        int dy = to.getY() - from.getY();
        int dz = Math.abs(to.getZ() - from.getZ());

        double cost = dx + dz;

        if (dy > 0) {
            cost += dy * 1.2;
        } else if (dy < 0) {
            cost += Math.abs(dy) * 0.5;
        }

        if (isDoor(to) || isDoor(to.down())) cost += 2.0;

        return cost;
    }

    private double heuristic(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX())
                + Math.abs(a.getY() - b.getY())
                + Math.abs(a.getZ() - b.getZ());
    }

    private Path buildPath(Node end) {
        List<Node> nodes = new ArrayList<>();
        Node current = end;
        while (current != null) {
            nodes.add(current);
            current = current.parent;
        }
        Collections.reverse(nodes);

        PathPoint[] points = new PathPoint[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            BlockPos p = nodes.get(i).pos;
            PathPoint pp = new PathPoint(p.getX(), p.getY(), p.getZ());
            pp.index = i;
            points[i] = pp;
        }

        return new Path(points);
    }

    private static class Node {
        final BlockPos pos;
        final Node parent;
        final double g;
        final double heuristic;
        final double f;

        Node(BlockPos pos, Node parent, double g, double heuristic) {
            this.pos = pos;
            this.parent = parent;
            this.g = g;
            this.heuristic = heuristic;
            this.f = g + heuristic;
        }
    }
}
