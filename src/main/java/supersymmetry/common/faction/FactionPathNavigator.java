package supersymmetry.common.faction;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
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
    private static final long REPATH_COOLDOWN_TICKS = 100;

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

    @Override
    public Path getPathToEntityLiving(Entity entityIn) {
        BlockPos targetPos = new BlockPos(
                MathHelper.floor(entityIn.posX),
                MathHelper.floor(entityIn.posY),
                MathHelper.floor(entityIn.posZ));

        long gameTime = this.world.getTotalWorldTime();

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

        FactionAStar astar = new FactionAStar(this.world, this.entity);
        Path path = astar.findPath(start, targetPos);

        this.lastTargetPos = targetPos;
        this.lastPathCalculationTime = gameTime;

        return path;
    }

    @Override
    public Path getPathToPos(BlockPos pos) {
        long gameTime = this.world.getTotalWorldTime();

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

        FactionAStar astar = new FactionAStar(this.world, this.entity);
        Path path = astar.findPath(start, pos);

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
