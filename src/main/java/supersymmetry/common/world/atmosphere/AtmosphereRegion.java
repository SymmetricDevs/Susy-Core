package supersymmetry.common.world.atmosphere;

import java.util.*;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A pressurized volume of oxygenated blocks, stored in an Octree.
 *
 * <p>
 * The region is built by flood-filling outward from a source position
 * (the disperser). Filling can be budgeted so only N blocks are processed
 * per call, making it safe for incremental/tick-based expansion.
 *
 * <p>
 * Pressure is tracked as a separate value (0.0–1.0). The octree represents
 * the room shape; pressure determines whether the oxygen is active.
 * Depressurization decreases pressure without removing octree positions,
 * eliminating "lost block" edge cases.
 */
public class AtmosphereRegion {

    private static final int DEFAULT_OCTREE_SIZE = 256; // power of 2

    private final Octree octree;
    private final BlockPos source; // primary position (octree center, fallback flood start)
    private double pressure; // 0.0 .. 1.0
    private boolean fillFailed = false; // true if fill hit canSeeSky (unsealed room)

    // Active disperser positions within this region
    private final Set<BlockPos> dispersers = new HashSet<>();

    // Flood-fill frontier (persisted between budgeted calls)
    private final Deque<BlockPos> frontier = new ArrayDeque<>();
    private final Set<BlockPos> visited = new HashSet<>();
    private boolean fillComplete = false;

    // Tracks the last world tick when a disperser supplied oxygen
    private long lastOxygenSupplyTick = 0;
    private int oxygensSupplied = 0;

    // Active breach positions (wall blocks that were broken, exposing the region to vacuum)
    private final Set<BlockPos> breachPoints = new HashSet<>();

    /**
     * Create a region with a default octree size.
     */
    public AtmosphereRegion(BlockPos source) {
        this(source, DEFAULT_OCTREE_SIZE);
    }

    /**
     * Create a region with a specified octree size.
     */
    public AtmosphereRegion(BlockPos source, int octreeSize) {
        int half = octreeSize / 2;
        int ox = source.getX() - half;
        int oy = source.getY() - half;
        int oz = source.getZ() - half;
        this.octree = new Octree(ox, oy, oz, octreeSize);
        this.source = source;
        this.pressure = 0.0;

        frontier.add(source);
        visited.add(source);
    }

    /**
     * Create a sourceless region from an existing set of positions.
     */
    public AtmosphereRegion(Set<BlockPos> positions, double pressure,
                            int octreeOriginX, int octreeOriginY, int octreeOriginZ, int octreeSize) {
        this(positions.iterator().next(), positions, pressure, Collections.emptySet(),
                octreeOriginX, octreeOriginY, octreeOriginZ, octreeSize);
    }

    /**
     * Reconstruct a region from saved data (old format with position set).
     */
    public AtmosphereRegion(BlockPos source, Set<BlockPos> positions, double pressure, Set<BlockPos> dispersers,
                            int octreeOriginX, int octreeOriginY, int octreeOriginZ, int octreeSize) {
        this.octree = new Octree(octreeOriginX, octreeOriginY, octreeOriginZ, octreeSize);
        this.source = source;
        this.pressure = pressure;
        this.fillComplete = true;
        this.dispersers.addAll(dispersers);

        for (BlockPos pos : positions) {
            octree.insert(pos);
        }
    }

    /**
     * Reconstruct a region from a serialized octree.
     */
    public AtmosphereRegion(BlockPos source, Octree octree, double pressure, Set<BlockPos> dispersers) {
        this.octree = octree;
        this.source = source;
        this.pressure = pressure;
        this.fillComplete = true;
        this.dispersers.addAll(dispersers);
    }

    // ---- Disperser management ----

    public void addDisperser(BlockPos pos) {
        dispersers.add(pos);
    }

    public void removeDisperser(BlockPos pos) {
        dispersers.remove(pos);
    }

    public Set<BlockPos> getDispersers() {
        return dispersers;
    }

    /**
     * Mark that oxygen was supplied by a disperser this tick.
     */
    public void markOxygenSupplied() {
        oxygensSupplied++;
    }

    /**
     * Returns true if a disperser has supplied oxygen recently (within 1 second).
     */
    public int clearOxygens() {
        int temp = oxygensSupplied;
        oxygensSupplied = 0;
        return temp;
    }

    /**
     * Returns the positions to start flood fills / revalidation from.
     * Uses all disperser positions if any exist, otherwise falls back to the primary source.
     */
    public Collection<BlockPos> getFloodSources() {
        return dispersers.isEmpty() ? Collections.singleton(source) : dispersers;
    }

    // ---- Flood fill ----

    /**
     * Expand the region by flood-filling up to {@code budget} blocks.
     *
     * @return true if the flood fill is now complete (frontier exhausted)
     */
    public boolean floodFill(World world, int budget) {
        if (fillComplete) return true;
        if (fillFailed) return true;

        int processed = 0;
        while (!frontier.isEmpty() && processed < budget) {
            BlockPos pos = frontier.poll();
            processed++;

            if (world.isBlockFullCube(pos)) continue;

            // If this position can see the sky, the room is unsealed
            if (world.canSeeSky(pos)) {
                for (BlockPos p : octree.getAllPositions()) {
                    octree.remove(p);
                }
                frontier.clear();
                visited.clear();
                fillComplete = true;
                fillFailed = true;
                pressure = 0.0;
                return true;
            }

            octree.insert(pos);

            for (BlockPos nb : BlockPosUtil.neighbors(pos)) {
                if (!visited.contains(nb) && !world.isBlockFullCube(nb)) {
                    visited.add(nb);
                    frontier.add(nb);
                }
            }
        }

        if (frontier.isEmpty()) {
            fillComplete = true;
            visited.clear();
        }

        return fillComplete;
    }

    /**
     * Reset the flood-fill state so the region can be re-flooded from all sources.
     * Clears the octree.
     */
    public void resetFill() {
        for (BlockPos pos : octree.getAllPositions()) {
            octree.remove(pos);
        }
        frontier.clear();
        visited.clear();
        for (BlockPos src : getFloodSources()) {
            frontier.add(src);
            visited.add(src);
        }
        fillComplete = false;
        fillFailed = false;
    }

    /**
     * Restart the flood-fill from all sources WITHOUT clearing the octree.
     * Existing positions are kept (no oxygen loss). The fill will
     * re-derive connectivity and expand into any newly accessible space.
     */
    public void continueFill() {
        frontier.clear();
        visited.clear();
        for (BlockPos src : getFloodSources()) {
            frontier.add(src);
            visited.add(src);
        }
        fillComplete = false;
        fillFailed = false;
    }

    // ---- Breach management ----

    public void addBreach(BlockPos pos) {
        breachPoints.add(pos);
    }

    /**
     * Remove a breach if the position is now solid.
     * 
     * @return true if the breach was present and removed
     */
    public boolean sealBreach(BlockPos pos) {
        return breachPoints.remove(pos);
    }

    public boolean hasBreaches() {
        return !breachPoints.isEmpty();
    }

    public int getBreachCount() {
        return breachPoints.size();
    }

    public Set<BlockPos> getBreachPoints() {
        return breachPoints;
    }

    /**
     * Remove any breach points that are no longer passable (block placed there).
     */
    public void validateBreaches(World world) {
        breachPoints.removeIf(world::isBlockFullCube);
    }

    // ---- Queries ----

    public boolean contains(BlockPos pos) {
        return octree.contains(pos);
    }

    public int size() {
        return octree.size();
    }

    public List<BlockPos> getAllPositions() {
        return octree.getAllPositions();
    }

    // ---- Getters / setters ----

    public BlockPos getSource() {
        return source;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = Math.max(0.0, Math.min(1.0, pressure));
    }

    public int getVolume() {
        return octree.size();
    }

    public boolean isFillComplete() {
        return fillComplete;
    }

    public Octree getOctree() {
        return octree;
    }

    public boolean isSourceless() {
        return dispersers.isEmpty();
    }

    public boolean isFillFailed() {
        return fillFailed;
    }
}
