package supersymmetry.common.faction;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

public class FactionNodeProcessor extends WalkNodeProcessor {

    public void primeForWorld(IBlockAccess world, EntityLiving entity) {
        this.blockaccess = world;
        this.entity = entity;
        this.entitySizeX = MathHelper_ceil(entity.width + 1.0F);
        this.entitySizeY = MathHelper_ceil(entity.height + 1.0F);
        this.entitySizeZ = this.entitySizeX;
        // Re-apply flags after priming
        this.setCanEnterDoors(true);
        this.setCanOpenDoors(true);
    }

    private static int MathHelper_ceil(float value) {
        int i = (int) value;
        return value > (float) i ? i + 1 : i;
    }

    @Override
    public PathNodeType getPathNodeType(IBlockAccess blockaccess, int x, int y, int z,
                                        EntityLiving entity, int sizeX, int sizeY, int sizeZ,
                                        boolean canBreakDoors, boolean canEnterDoors) {
        BlockPos pos = new BlockPos(x, y, z);
        PathNodeType hazard = FactionTrapHazardEvaluator.getHazardNodeType(blockaccess, pos);
        if (hazard != null) return hazard;

        PathNodeType result = super.getPathNodeType(blockaccess, x, y, z, entity,
                sizeX, sizeY, sizeZ, canBreakDoors, canEnterDoors);

        return result;
    }

    @Override
    public void init(IBlockAccess sourceIn, EntityLiving mob) {
        super.init(sourceIn, mob);
    }

    @Override
    public PathPoint getStart() {
        return super.getStart();
    }

    @Override
    public int findPathOptions(PathPoint[] pathOptions, PathPoint currentPoint,
                               PathPoint targetPoint, float maxDistance) {
        int count = super.findPathOptions(pathOptions, currentPoint, targetPoint, maxDistance);

        BlockPos pos = new BlockPos(currentPoint.x, currentPoint.y, currentPoint.z);

        boolean canClimbUp = isClimbableAt(pos) || isClimbableAt(pos.up());
        boolean canClimbDown = isClimbableAt(pos) || isClimbableAt(pos.down());

        if (canClimbUp) {
            count = addVerticalNeighbor(pathOptions, currentPoint, targetPoint, maxDistance, count, +1);
        }
        if (canClimbDown) {
            count = addVerticalNeighbor(pathOptions, currentPoint, targetPoint, maxDistance, count, -1);
        }

        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] dir : directions) {
            int dx = dir[0];
            int dz = dir[1];

            for (int wallHeight = 1; wallHeight <= FactionAStar.MAX_CLIMB_HEIGHT; wallHeight++) {
                BlockPos wallTop = pos.add(dx, wallHeight, dz);

                if (!isHeadroomClear(pos, wallHeight)) {
                    break;
                }

                BlockPos floorBelowLanding = wallTop.down();
                IBlockState floorState = this.blockaccess.getBlockState(floorBelowLanding);
                if (!floorState.getMaterial().isSolid()) {
                    continue;
                }

                if (isPositionClear(wallTop)) {
                    count = addClimbNeighbor(pathOptions, wallTop, targetPoint, maxDistance, count);
                    break;
                }
            }
        }

        return count;
    }

    private boolean isHeadroomClear(BlockPos pos, int height) {
        for (int i = 1; i <= height + 1; i++) {
            IBlockState state = this.blockaccess.getBlockState(pos.up(i));
            if (state.getMaterial().isSolid()) {
                return false;
            }
        }
        return true;
    }

    private boolean isPositionClear(BlockPos pos) {
        for (int i = 0; i < this.entitySizeY; i++) {
            IBlockState state = this.blockaccess.getBlockState(pos.up(i));
            if (state.getMaterial().isSolid()) {
                return false;
            }
        }
        return true;
    }

    private int addVerticalNeighbor(PathPoint[] pathOptions, PathPoint current,
                                    PathPoint target, float maxDistance, int count, int dy) {
        if (count >= pathOptions.length) return count;

        int nx = current.x;
        int ny = current.y + dy;
        int nz = current.z;

        if (ny < 0 || ny >= 256) return count;

        PathNodeType destType = this.getPathNodeType(this.blockaccess, nx, ny, nz);
        if (destType == PathNodeType.BLOCKED || destType == PathNodeType.FENCE) return count;

        PathPoint point = this.openPoint(nx, ny, nz);
        if (point == null || point.visited) return count;
        if (point.distanceTo(target) >= maxDistance) return count;

        point.nodeType = PathNodeType.OPEN;
        point.costMalus = 0.0f;
        pathOptions[count++] = point;
        return count;
    }

    private int addClimbNeighbor(PathPoint[] pathOptions, BlockPos targetPos,
                                 PathPoint target, float maxDistance, int count) {
        if (count >= pathOptions.length) return count;

        if (targetPos.getY() < 0 || targetPos.getY() >= 256) return count;

        PathPoint point = this.openPoint(targetPos.getX(), targetPos.getY(), targetPos.getZ());
        if (point == null || point.visited) return count;
        if (point.distanceTo(target) >= maxDistance) return count;

        point.nodeType = PathNodeType.OPEN;
        point.costMalus = 1.0f;
        pathOptions[count++] = point;
        return count;
    }

    private boolean isClimbableAt(BlockPos pos) {
        if (this.blockaccess == null) return false;
        IBlockState state = this.blockaccess.getBlockState(pos);
        return state.getBlock().isLadder(state, this.blockaccess, pos, null);
    }

    @Override
    public PathPoint getPathPointToCoords(double x, double y, double z) {
        int bx = MathHelper.floor(x);
        int by = MathHelper.floor(y);
        int bz = MathHelper.floor(z);
        PathPoint result = this.openPoint(bx, by, bz);
        return result;
    }
}
