package supersymmetry.common.world.atmosphere;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Handles lazy revalidation of regions.
 *
 * <p>
 * When a wall is built that might split a region, the revalidator
 * re-floods from the disperser source, diffs against the existing octree,
 * and splits disconnected portions into separate regions (or removes them).
 *
 * <p>
 * Revalidation is budgeted: each call to {@link #tick} processes up to
 * a configurable number of blocks, so it can run across multiple game ticks.
 */
public class AtmosphereRevalidator {

    private static class Job {

        final AtmosphereRegion region;

        final Deque<BlockPos> frontier = new ArrayDeque<>();
        final Set<BlockPos> reached = new HashSet<>();
        boolean floodDone = false;

        Job(AtmosphereRegion region) {
            this.region = region;
            for (BlockPos src : region.getFloodSources()) {
                frontier.add(src);
                reached.add(src);
            }
        }
    }

    private final List<Job> pendingJobs = new ArrayList<>();
    private final int budgetPerTick;

    public AtmosphereRevalidator(int budgetPerTick) {
        this.budgetPerTick = budgetPerTick;
    }

    public void schedule(AtmosphereRegion region) {
        for (int i = 0; i < pendingJobs.size(); i++) {
            if (pendingJobs.get(i).region == region) {
                // Restart the job — world state has changed since it was scheduled
                pendingJobs.set(i, new Job(region));
                return;
            }
        }
        pendingJobs.add(new Job(region));
    }

    public boolean hasPendingWork() {
        return !pendingJobs.isEmpty();
    }

    public void tick(World world, SplitCallback onSplit) {
        if (pendingJobs.isEmpty()) return;

        int remaining = budgetPerTick;

        Iterator<Job> it = pendingJobs.iterator();
        while (it.hasNext() && remaining > 0) {
            Job job = it.next();

            if (!job.floodDone) {
                remaining = processFlood(job, world, remaining);
            }

            if (job.floodDone) {
                Set<BlockPos> disconnected = new HashSet<>();
                for (BlockPos pos : job.region.getAllPositions()) {
                    if (!job.reached.contains(pos)) {
                        disconnected.add(pos);
                    }
                }

                for (BlockPos pos : disconnected) {
                    job.region.getOctree().remove(pos);
                }

                if (!disconnected.isEmpty() && onSplit != null) {
                    onSplit.onDisconnected(job.region, disconnected);
                }

                it.remove();
            }
        }
    }

    private int processFlood(Job job, World world, int budget) {
        int processed = 0;
        while (!job.frontier.isEmpty() && processed < budget) {
            BlockPos pos = job.frontier.poll();
            processed++;

            if (!job.region.contains(pos) && !job.region.getFloodSources().contains(pos)) {
                continue;
            }

            for (BlockPos nb : BlockPosUtil.neighbors(pos)) {
                if (!job.reached.contains(nb) && !world.isBlockFullCube(nb) && job.region.contains(nb)) {
                    job.reached.add(nb);
                    job.frontier.add(nb);
                }
            }
        }

        if (job.frontier.isEmpty()) {
            job.floodDone = true;
        }

        return budget - processed;
    }

    @FunctionalInterface
    public interface SplitCallback {

        void onDisconnected(AtmosphereRegion region, Set<BlockPos> disconnected);
    }
}
