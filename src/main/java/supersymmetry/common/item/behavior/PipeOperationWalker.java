package supersymmetry.common.item.behavior;

import gregtech.api.pipenet.tile.IPipeTile;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.SusyLog;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/// This is so horrible ngl...
@ParametersAreNonnullByDefault
public class PipeOperationWalker<T extends IPipeTile<?, ?>> {

    private final World world;
    private final List<EnumFacing> nextPipeFacings = new ArrayList<>();
    private final List<T> nextPipes = new ArrayList<>();
    private final BlockPos.MutableBlockPos currentPos;
    /// Yippee Java generics
    private final Class<T> basePipeClass;
    private boolean reverse = false;
    /// Copied from normal pipe net walker class,
    /// I hate everything being private in ceu...
    private PipeOperationWalker<T> root;
    private Set<T> walked;
    private List<PipeOperationWalker<T>> walkers;
    private T currentPipe;
    private EnumFacing from = null;
    private int walkedBlocks;
    private boolean invalid;
    private boolean running;
    private boolean failed = false;
    /// Function to run on every pipe
    /// TODO: make this return boolean. If false is returned, then halt the walker
    private ITraverseOption option;
    /// Only exists for the root walker
    @Nullable
    private EnumFacing direction;

    private PipeOperationWalker(World world, BlockPos sourcePipe, int walkedBlocks, Class<T> basePipeClass) {
        this.world = world;
        this.walkedBlocks = walkedBlocks;
        this.currentPos = new BlockPos.MutableBlockPos(sourcePipe);
        this.basePipeClass = basePipeClass;
        this.root = this;
    }

    @SuppressWarnings("unchecked")
    public static <T extends IPipeTile<?, ?>> int collectPipeNet(World world, BlockPos sourcePipe, T pipe,
                                                                 EnumFacing direction, ITraverseOption option, int maxWalks) {
        var walker = new PipeOperationWalker<>(world, sourcePipe, 0, (Class<T>) pipe.getClass());
        {
            walker.currentPipe = pipe;
            walker.direction = direction;
            walker.option = option;
            walker.traverse(maxWalks);
        }
        return walker.failed ? 0 : walker.walkedBlocks;
    }

    private void traverse(int maxWalks) {
        if (invalid) throw new IllegalStateException("This walker already walked!");
        {
            this.root = this;
            this.walked = new ObjectOpenHashSet<>();
            this.running = true;
        }
        int i = 0;
        //noinspection StatementWithEmptyBody
        while (running && !step() && i++ < maxWalks) {
            /* Do nothing */
        }
        {
            this.walkedBlocks = i;
            this.running = false;
            this.walked = null;
        }
        if (walkedBlocks >= maxWalks) {
            SusyLog.logger.fatal("The walker reached the maximum amount of walks {}", walkedBlocks);
        }
        invalid = true;
    }

    private boolean step() {
        if (walkers == null) {
            if (!checkCurrent()) {
                this.root.failed = true;
                return true;
            }

            if (nextPipeFacings.isEmpty()) return true;
            if (nextPipeFacings.size() == 1) {

                var next = nextPipes.get(0);
                var into = nextPipeFacings.get(0);

                {
                    this.root.option.operate(into, currentPipe, next, reverse);

                    this.currentPos.setPos(next.getPipePos());
                    this.currentPipe = next;
                    this.from = into.getOpposite();
                    this.walkedBlocks++;
                }

                return !root.running;
            }

            walkers = new ArrayList<>();
            for (int i = 0; i < nextPipeFacings.size(); i++) {
                var into = nextPipeFacings.get(i);

                var walker = createSubWalker(world, into, currentPos.offset(into), walkedBlocks + 1);

                var nextPipe = nextPipes.get(i);

                root.option.operate(into, currentPipe, nextPipe, walker.reverse);

                walker.root = this.root;
                walker.currentPipe = nextPipe;
                walker.from = into.getOpposite();
                this.walkers.add(walker);
            }
        }

        walkers.removeIf(PipeOperationWalker::step);

        return !root.running || walkers.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private boolean checkCurrent() {
        this.nextPipeFacings.clear();
        this.nextPipes.clear();
        if (currentPipe == null) {
            var thisPipe = world.getTileEntity(currentPos);
            if (!(thisPipe instanceof IPipeTile)) {
                SusyLog.logger.fatal("PipeWalker expected a pipe, but found {} at {}", thisPipe, currentPos);
                return false;
            }
            if (!basePipeClass.isAssignableFrom(thisPipe.getClass())) {
                return false;
            }
            currentPipe = (T) thisPipe;
        }
        T pipeTile = currentPipe;

        this.root.walked.add(pipeTile);

        List<EnumFacing> facings = root.option.findNext(from != null ? from : direction, pipeTile);

        if (walkedBlocks == 0) {
            facings.add(direction); // Special case for the root node
        }

        for (EnumFacing side : facings) {
            var tile = pipeTile.getNeighbor(side);
            if (tile != null && basePipeClass.isAssignableFrom(tile.getClass())) {
                T otherPipe = (T) tile;
                if (!isWalked(otherPipe)) {
                    nextPipeFacings.add(side);
                    nextPipes.add(otherPipe);
                }
            }
        }
        return true;
    }

    private boolean isWalked(T pipe) {
        return root.walked.contains(pipe);
    }

    private PipeOperationWalker<T> createSubWalker(World world, EnumFacing facingToNextPos, BlockPos nextPos, int walkedBlocks) {
        boolean reverse = this.direction != null ? facingToNextPos != direction : this.reverse;
        var subWalker = new PipeOperationWalker<>(world, nextPos, walkedBlocks, this.basePipeClass);
        subWalker.reverse = reverse;
        return subWalker;
    }
}
