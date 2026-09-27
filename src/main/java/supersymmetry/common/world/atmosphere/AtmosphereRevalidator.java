package supersymmetry.common.world.atmosphere;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Rebuilds regions by re-deriving their connected components from the world.
 *
 * <p>
 * A rebuild job floods outward from every cell and disperser of the regions being rebuilt, through passable blocks,
 * grouping everything it reaches into connected components:
 * <ul>
 * <li>Vents (sky-exposed positions, or porous blocks touching them) stop the flood and are recorded on the component
 * that reached them.</li>
 * <li>Reaching a cell of another region connects the two: that region is absorbed into the job.</li>
 * <li>Cells no longer reachable from each other end up in separate components (disconnection).</li>
 * </ul>
 * When the job finishes, each component becomes a new region whose pressure is the air the old regions held in it,
 * spread over its new volume. Components without a disperser whose pressure is negligible are dropped.
 *
 * <p>
 * Jobs are budgeted: each call to {@link #tick} processes a bounded number of blocks, so a rebuild can span
 * several game ticks. The old regions stay active until the job completes.
 */
public class AtmosphereRevalidator {

    /**
     * Upper bound on the volume of a single region; anything larger is treated as open to space.
     */
    private static final int MAX_REGION_VOLUME = 50_000;

    private static class Component {

        final OctreeVolume cells = new OctreeVolume();
        final Set<BlockPos> vents = new HashSet<>();
        final Deque<BlockPos> frontier = new ArrayDeque<>();
        int overflowVents = 0;
    }

    private static class Job {

        final Set<AtmosphereRegion> regions = new LinkedHashSet<>();
        final Deque<BlockPos> seeds = new ArrayDeque<>();
        final Set<BlockPos> assigned = new HashSet<>();
        final List<Component> components = new ArrayList<>();
        Component current;
        boolean dirty = false;
        boolean aborted = false;

        void addRegion(AtmosphereRegion region) {
            if (regions.add(region)) {
                seeds.addAll(region.getDispersers());
                for (BlockPos pos : region.getCells()) {
                    seeds.add(pos);
                }
            }
        }
    }

    private final List<Job> jobs = new ArrayList<>();
    private final Map<AtmosphereRegion, Job> jobByRegion = new HashMap<>();

    /**
     * Schedule a rebuild of the given regions, as a single job (so connections between them are resolved together).
     * Regions that are already being rebuilt cause their job to be re-run once it completes.
     */
    public void schedule(Collection<AtmosphereRegion> regions) {
        Job target = null;
        for (AtmosphereRegion region : regions) {
            Job existing = jobByRegion.get(region);
            if (existing == null)
                continue;
            if (target == null) {
                target = existing;
            } else if (existing != target) {
                mergeInto(target, existing);
            }
        }

        if (target == null) {
            target = new Job();
            jobs.add(target);
        } else {
            // World state changed under a running job: re-run it after it completes
            target.dirty = true;
        }

        for (AtmosphereRegion region : regions) {
            addToJob(target, region);
        }
    }

    public void schedule(AtmosphereRegion region) {
        schedule(Collections.singleton(region));
    }

    private void addToJob(Job job, AtmosphereRegion region) {
        Job existing = jobByRegion.get(region);
        if (existing == job)
            return;
        if (existing != null) {
            mergeInto(job, existing);
            return;
        }
        job.addRegion(region);
        jobByRegion.put(region, job);
        region.setRebuilding(true);
    }

    private void mergeInto(Job target, Job other) {
        jobs.remove(other);
        target.dirty |= other.dirty;
        for (AtmosphereRegion region : other.regions) {
            jobByRegion.remove(region);
            addToJob(target, region);
        }
    }

    /**
     * Process pending jobs within the given block budget.
     */
    public void tick(World world, List<AtmosphereRegion> allRegions, int budget, RebuildCallback onRebuilt) {
        while (budget > 0 && !jobs.isEmpty()) {
            Job job = jobs.get(0);
            budget = process(job, world, allRegions, budget);

            if (job.aborted) {
                // Part of the region is in an unloaded chunk; keep the old state and retry later
                finish(job);
            } else if (job.current == null && job.seeds.isEmpty()) {
                finish(job);
                onRebuilt.onRebuilt(job.regions, buildRegions(job, world), job.dirty);
            }
        }
    }

    private void finish(Job job) {
        jobs.remove(job);
        for (AtmosphereRegion region : job.regions) {
            jobByRegion.remove(region);
            region.setRebuilding(false);
        }
    }

    private int process(Job job, World world, List<AtmosphereRegion> allRegions, int budget) {
        while (budget > 0) {
            Component c = job.current;
            if (c == null || c.frontier.isEmpty()) {
                if (c != null) {
                    if (!c.cells.isEmpty())
                        job.components.add(c);
                    job.current = null;
                }
                BlockPos seed = job.seeds.poll();
                if (seed == null)
                    return budget;
                if (job.assigned.contains(seed))
                    continue;
                job.current = new Component();
                if (!visit(job, job.current, world, allRegions, seed)) {
                    job.aborted = true;
                    return budget;
                }
                continue;
            }

            BlockPos pos = c.frontier.poll();
            budget--;

            if (c.cells.size() >= MAX_REGION_VOLUME) {
                // Too large to be a sealed room: treat the remaining frontier as openings to space
                c.overflowVents = c.frontier.size() + 1;
                c.frontier.clear();
                continue;
            }

            c.cells.insert(pos);
            for (BlockPos nb : AtmosphereUtils.neighbors(pos)) {
                if (!visit(job, c, world, allRegions, nb)) {
                    job.aborted = true;
                    return budget;
                }
            }
        }
        return budget;
    }

    /**
     * Examine a position reached by the flood of component {@code c}.
     *
     * @return false if the position is in an unloaded chunk, and the job must be aborted
     */
    private boolean visit(Job job, Component c, World world, List<AtmosphereRegion> allRegions, BlockPos pos) {
        if (pos.getY() < 0 || job.assigned.contains(pos))
            return true;
        if (pos.getY() >= world.getHeight()) {
            c.vents.add(pos);
            return true;
        }
        if (!world.isBlockLoaded(pos))
            return false;
        if (!AtmosphereUtils.isPassable(world, pos)) {
            // Porous blocks don't carry the flood, but leak if vacuum is right behind them
            if (AtmosphereUtils.isPorous(world, pos) && AtmosphereUtils.isVent(world, pos))
                c.vents.add(pos);
            return true;
        }
        if (world.canSeeSky(pos)) {
            c.vents.add(pos);
            return true;
        }

        job.assigned.add(pos);
        c.frontier.add(pos);

        // Connected to another region: absorb it into this rebuild
        for (AtmosphereRegion other : allRegions) {
            if (!job.regions.contains(other) && (other.contains(pos) || other.getDispersers().contains(pos))) {
                addToJob(job, other);
            }
        }
        return true;
    }

    /**
     * Turn a finished job's components into regions, redistributing the old regions' air over them.
     */
    private List<AtmosphereRegion> buildRegions(Job job, World world) {
        List<Component> components = job.components;
        double[] air = new double[components.size()];
        for (AtmosphereRegion old : job.regions) {
            for (BlockPos pos : old.getCells()) {
                for (int i = 0; i < components.size(); i++) {
                    if (components.get(i).cells.contains(pos)) {
                        air[i] += old.getPressure();
                        break;
                    }
                }
            }
        }

        List<AtmosphereRegion> result = new ArrayList<>();
        for (int i = 0; i < components.size(); i++) {
            Component c = components.get(i);
            AtmosphereRegion region = new AtmosphereRegion(c.cells, air[i] / c.cells.size());
            region.getVents().addAll(c.vents);
            region.setOverflowVents(c.overflowVents);
            result.add(region);
        }

        for (AtmosphereRegion old : job.regions) {
            for (BlockPos disperser : old.getDispersers()) {
                AtmosphereRegion home = null;
                for (AtmosphereRegion region : result) {
                    if (region.contains(disperser)) {
                        home = region;
                        break;
                    }
                }
                if (home == null) {
                    // The disperser's position is solid or open to space; track it in an empty region
                    home = new AtmosphereRegion();
                    if (AtmosphereUtils.isVent(world, disperser)) {
                        home.getVents().add(disperser);
                    }
                    result.add(home);
                }
                home.addDisperser(disperser);
            }
        }

        // Air that is no longer attached to a disperser is only simulated while it holds meaningful pressure
        result.removeIf(r -> r.isSourceless() && r.getPressure() <= AtmosphereRegionGraph.MIN_PRESSURE);
        return result;
    }

    @FunctionalInterface
    public interface RebuildCallback {

        void onRebuilt(Collection<AtmosphereRegion> oldRegions, List<AtmosphereRegion> newRegions, boolean dirty);
    }
}
