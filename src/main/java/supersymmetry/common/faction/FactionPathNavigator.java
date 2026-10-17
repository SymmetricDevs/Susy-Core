package supersymmetry.common.faction;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class FactionPathNavigator extends PathNavigateGround {

    private BlockPos lastTargetPos = null;
    private long lastPathCalculationTime = 0;
    private long lastFailedPathTime = -9999;
    private static final long REPATH_COOLDOWN_TICKS = 100;
    private static final long FAILED_REPATH_COOLDOWN_TICKS = 20;
    private long budgetDeniedTime = -9999;
    private static final long BUDGET_RETRY_TICKS = 5;

    private FactionAStar astar;

    public FactionPathNavigator(EntityLiving entity, World world) {
        super(entity, world);
    }

    @Override
    protected PathFinder getPathFinder() {
        this.nodeProcessor = new FactionNodeProcessor();
        this.nodeProcessor.setCanEnterDoors(true);
        this.nodeProcessor.setCanOpenDoors(true);
        this.nodeProcessor.setCanSwim(false);
        return new PathFinder(this.nodeProcessor);
    }

    @Override
    protected boolean canNavigate() {
        return true;
    }

    private void primeProcessor() {
        NodeProcessor np = this.nodeProcessor;
        if (np instanceof FactionNodeProcessor) {
            ((FactionNodeProcessor) np).primeForWorld(this.world, this.entity);
        }
    }

    private FactionAStar getAstar() {
        if (this.astar == null) {
            this.astar = new FactionAStar(this.world, this.entity);
        }
        return this.astar;
    }

    @Override
    public Path getPathToEntityLiving(Entity entityIn) {
        long gameTime = this.world.getTotalWorldTime();
        EntityPlayer player = entityIn instanceof EntityPlayer ? (EntityPlayer) entityIn : null;

        // Known-unreachable player and no line of sight: don't wallhack toward them.
        if (player != null && FactionUnreachableTargets.isIgnored(player, gameTime) &&
                !this.entity.canEntityBeSeen(player)) {
            return null;
        }

        // The global per-tick budget said no recently; wait a few ticks instead of asking every tick.
        if ((gameTime - this.budgetDeniedTime) < BUDGET_RETRY_TICKS) {
            return this.currentPath;
        }

        BlockPos targetPos = new BlockPos(
                MathHelper.floor(entityIn.posX),
                MathHelper.floor(entityIn.posY),
                MathHelper.floor(entityIn.posZ));

        // If we recently failed to find a path, do not hammer A* again.
        if ((gameTime - this.lastFailedPathTime) < FAILED_REPATH_COOLDOWN_TICKS) {
            return this.currentPath;
        }

        if (this.currentPath != null && !this.noPath()) {
            boolean targetMoved = this.lastTargetPos == null || this.lastTargetPos.distanceSq(targetPos) > 9.0;
            boolean cooldownActive = (gameTime - this.lastPathCalculationTime) < REPATH_COOLDOWN_TICKS;

            if (!targetMoved && cooldownActive) {
                return this.currentPath;
            }
        }

        primeProcessor();
        BlockPos start = new BlockPos(
                MathHelper.floor(this.entity.posX),
                MathHelper.floor(this.entity.posY),
                MathHelper.floor(this.entity.posZ));

        // Tolerance of 1 horizontal / 2 vertical so jumping or swimming players count as reached.
        FactionAStar astar = getAstar();
        Path path = astar.findPath(start, targetPos, 1, 2);

        // findPath only returns null when the global budget denied the search.
        // That is not a path failure, so don't start the failed-path cooldown or flag the player.
        if (path == null) {
            this.budgetDeniedTime = gameTime;
            return this.currentPath;
        }

        // Goal not reached and barely any progress toward it: flag the player as unreachable.
        if (player != null && astar.lastSearchFailed()) {
            FactionUnreachableTargets.mark(player, gameTime);
        }

        if (path.getCurrentPathLength() == 0) {
            this.lastFailedPathTime = gameTime;
            return null;
        }

        this.lastTargetPos = targetPos;
        this.lastPathCalculationTime = gameTime;

        return path;
    }

    @Override
    public Path getPathToPos(BlockPos pos) {
        long gameTime = this.world.getTotalWorldTime();

        // If we recently failed to find a path, do not hammer A* again.
        if ((gameTime - this.lastFailedPathTime) < FAILED_REPATH_COOLDOWN_TICKS) {
            return this.currentPath;
        }

        if (this.currentPath != null && !this.noPath()) {
            boolean targetMoved = this.lastTargetPos == null || this.lastTargetPos.distanceSq(pos) > 9.0;
            boolean cooldownActive = (gameTime - this.lastPathCalculationTime) < REPATH_COOLDOWN_TICKS;

            if (!targetMoved && cooldownActive) {
                return this.currentPath;
            }
        }

        primeProcessor();
        BlockPos start = new BlockPos(
                MathHelper.floor(this.entity.posX),
                MathHelper.floor(this.entity.posY),
                MathHelper.floor(this.entity.posZ));

        Path path = getAstar().findPath(start, pos);

        if (path == null || path.getCurrentPathLength() == 0) {
            this.lastFailedPathTime = gameTime;
            return null;
        }

        this.lastTargetPos = pos;
        this.lastPathCalculationTime = gameTime;

        return path;
    }

    @Override
    public boolean tryMoveToEntityLiving(Entity entityIn, double speedIn) {
        Path path = this.getPathToEntityLiving(entityIn);
        if (path != null) {
            return this.setPath(path, speedIn);
        }
        return false;
    }

    @Override
    public boolean tryMoveToXYZ(double x, double y, double z, double speedIn) {
        Path path = this.getPathToPos(new BlockPos(
                MathHelper.floor(x),
                MathHelper.floor(y),
                MathHelper.floor(z)));
        if (path != null) {
            return this.setPath(path, speedIn);
        }
        return false;
    }
}
