package supersymmetry.common.world.atmosphere;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;

/**
 * Top-level manager for the atmosphere/oxygen system.
 *
 * <p>Maintains a collection of {@link AtmosphereRegion}s and handles block-change
 * events and per-tick operations.
 *
 * <p>Each region's octree represents the room shape (derived via flood fill).
 * Pressure is tracked separately as a 0.0–1.0 value:
 * <ul>
 *   <li>Decreases when breaches exist (wall broken to vacuum)</li>
 *   <li>Increases when sealed and a disperser is active</li>
 * </ul>
 * This avoids per-block drain state and the edge cases it caused.
 */
public class AtmosphereRegionGraph {

    /**
     * Pressure decrease per breach per tick, scaled by 1/volume.
     */
    private static final double DEPRESSURIZE_RATE = 10.0;
    /**
     * Pressure increase per tick when sealed with disperser, scaled by 1/volume.
     */
    private static final double PRESSURIZE_RATE = 4.0;
    /**
     * Blocks processed per tick for flood fills.
     */
    private static final int FILL_BUDGET_PER_TICK = 256;
    /**
     * Blocks processed per tick for revalidation.
     */
    private static final int REVALIDATE_BUDGET_PER_TICK = 128;
    /**
     * How often (in ticks) to scan regions for breaches.
     */
    private static final int BREACH_SCAN_INTERVAL = 20;

    private final List<AtmosphereRegion> regions = new ArrayList<>();
    private final AtmosphereRevalidator revalidator;

    // Regions that still need incremental flood-filling
    private final Set<AtmosphereRegion> fillingRegions = new HashSet<>();

    // Global set of all disperser positions (for idempotent addDisperser)
    private final Set<BlockPos> dispersers = new HashSet<>();

    private int tickCounter = 0;

    public AtmosphereRegionGraph() {
        this.revalidator = new AtmosphereRevalidator(REVALIDATE_BUDGET_PER_TICK);
    }

    // ---- Disperser management ----

    public void addDisperser(BlockPos pos) {
        if (dispersers.contains(pos)) return;
        dispersers.add(pos);

        // Check if this position is inside an existing region
        for (AtmosphereRegion region : regions) {
            if (region.contains(pos) || region.getDispersers().contains(pos)) {
                region.addDisperser(pos);
                // Restart fill to expand from the new source too
                if (region.isFillComplete() && !region.isFillFailed()) {
                    region.continueFill();
                    fillingRegions.add(region);
                }
                return;
            }
        }

        // New region for this disperser
        AtmosphereRegion region = new AtmosphereRegion(pos);
        region.addDisperser(pos);
        regions.add(region);
        fillingRegions.add(region);
    }

    public void removeDisperser(BlockPos pos) {
        dispersers.remove(pos);
        for (AtmosphereRegion region : regions) {
            if (region.getDispersers().contains(pos)) {
                region.removeDisperser(pos);
                // If no dispersers left, stop filling
                if (region.isSourceless()) {
                    fillingRegions.remove(region);
                }
                break;
            }
        }
    }

    // ---- Region management ----

    public void removeRegion(AtmosphereRegion region) {
        regions.remove(region);
        fillingRegions.remove(region);
    }

    // ---- Event handlers ----

    /**
     * Called when a block is broken. BreakEvent fires BEFORE block removal,
     * so the position is still solid in the world.
     */
    public void onBlockBreak(World world, BlockPos pos) {
        BlockPos[] neighbors = BlockPosUtil.neighbors(pos);
        AtmosphereRegion expandingRegion = null;

        for (AtmosphereRegion region : new ArrayList<>(regions)) {
            boolean adjacentToRegion = false;
            for (BlockPos nb : neighbors) {
                if (region.contains(nb)) {
                    adjacentToRegion = true;
                    break;
                }
            }
            if (!adjacentToRegion) continue;

            if (isExposedToVacuum(world, pos, pos)) {
                region.addBreach(pos);
                if (!region.isSourceless()) {
                    expandingRegion = region;
                }
            } else if (!region.isSourceless()) {
                // Sealed opening — expand the fill into the new space
                region.continueFill();
                fillingRegions.add(region);
                expandingRegion = region;
            }
        }

        // Absorb adjacent sourceless regions into the non-sourceless region
        if (expandingRegion != null) {
            for (AtmosphereRegion region : new ArrayList<>(regions)) {
                if (region == expandingRegion || !region.isSourceless()) continue;
                boolean adjacent = false;
                for (BlockPos nb : neighbors) {
                    if (region.contains(nb)) {
                        adjacent = true;
                        break;
                    }
                }
                if (adjacent) {
                    for (BlockPos p : region.getAllPositions()) {
                        expandingRegion.getOctree().insert(p);
                    }
                    // Transfer breach points from absorbed region
                    for (BlockPos bp : region.getBreachPoints()) {
                        expandingRegion.addBreach(bp);
                    }
                    regions.remove(region);
                    fillingRegions.remove(region);
                }
            }
        }
    }

    /**
     * Called when a block is placed (becomes solid) at the given position.
     */
    public void onBlockPlace(World world, BlockPos pos) {
        for (AtmosphereRegion region : new ArrayList<>(regions)) {
            // Check if this seals a breach
            if (region.sealBreach(pos)) {
                // Check remaining breaches — they might no longer reach vacuum
                // now that this block was placed
                recheckBreaches(world, region);
            }

            // If placed inside a region, remove from octree and revalidate
            if (region.contains(pos)) {
                region.getOctree().remove(pos);
                revalidator.schedule(region);
            }
        }
    }

    /**
     * Re-check whether each breach point in a region still leads to vacuum.
     * A breach might become sealed by building a wall nearby rather than
     * plugging the exact hole.
     */
    private void recheckBreaches(World world, AtmosphereRegion region) {
        Iterator<BlockPos> it = region.getBreachPoints().iterator();
        while (it.hasNext()) {
            BlockPos bp = it.next();
            if (world.isBlockFullCube(bp)) {
                it.remove();
            } else if (!isExposedToVacuum(world, bp)) {
                it.remove();
            }
        }

        // If all breaches are now sealed, restart fill to recover any lost shape
        if (!region.hasBreaches() && !region.isSourceless()) {
            region.continueFill();
            fillingRegions.add(region);
        }
    }

    // ---- Queries ----

    public double getOxygenation(BlockPos pos) {
        for (AtmosphereRegion region : regions) {
            if (region.contains(pos) && region.getVolume() > 0) {
                return region.getPressure();
            }
        }
        return 0;
    }


    public AtmosphereRegion getRegionAt(BlockPos pos) {
        for (AtmosphereRegion region : regions) {
            if (region.contains(pos)) return region;
        }
        return null;
    }

    // ---- Revalidation ----

    public void triggerRevalidation() {
        for (AtmosphereRegion region : new ArrayList<>(regions)) {
            revalidator.schedule(region);
        }
    }

    // ---- Tick ----

    public void tick(World world) {
        tickCounter++;

        // Periodic breach scan — catches breaches missed by block events
        if (tickCounter % BREACH_SCAN_INTERVAL == 0) {
            scanForBreaches(world);
        }

        updatePressures(world);
        processFloodFills(world);

        revalidator.tick(world, (region, disconnected) -> {
            if (!disconnected.isEmpty()) {
                Octree srcOctree = region.getOctree();
                AtmosphereRegion newRegion = new AtmosphereRegion(
                        disconnected, region.getPressure(),
                        srcOctree.getOriginX(), srcOctree.getOriginY(), srcOctree.getOriginZ(),
                        srcOctree.getTreeSize());

                // Transfer breach points to the new region if adjacent to its positions
                Iterator<BlockPos> breachIt = region.getBreachPoints().iterator();
                while (breachIt.hasNext()) {
                    BlockPos bp = breachIt.next();
                    boolean adjacentToNew = false;
                    for (BlockPos nb : BlockPosUtil.neighbors(bp)) {
                        if (disconnected.contains(nb)) {
                            adjacentToNew = true;
                            break;
                        }
                    }
                    if (adjacentToNew) {
                        newRegion.addBreach(bp);
                    }
                    // Remove from original if no longer adjacent to any of its remaining positions
                    boolean adjacentToOriginal = false;
                    for (BlockPos nb : BlockPosUtil.neighbors(bp)) {
                        if (region.contains(nb)) {
                            adjacentToOriginal = true;
                            break;
                        }
                    }
                    if (!adjacentToOriginal) {
                        breachIt.remove();
                    }
                }

                regions.add(newRegion);
            }
        });

        // Prune dead sourceless regions with no volume
        regions.removeIf(r -> r.isSourceless() && r.getVolume() == 0);

        reRefillRegions(world);
    }

    /**
     * Periodic scan: for each filled, pressurized region, check if the source
     * can reach vacuum. This catches breaches regardless of how they were
     * created (block events, pistons, other mods, etc.).
     */
    private void scanForBreaches(World world) {
        for (AtmosphereRegion region : regions) {
            if (region.getVolume() == 0) continue;
            if (region.getPressure() <= 0.0) continue;
            if (!region.isFillComplete() || region.isFillFailed()) continue;

            boolean anyExposed = false;
            BlockPos exposedSource = null;
            for (BlockPos src : region.getFloodSources()) {
                if (isExposedToVacuum(world, src)) {
                    anyExposed = true;
                    exposedSource = src;
                    break;
                }
            }

            if (anyExposed && !region.hasBreaches()) {
                region.addBreach(exposedSource);
            } else if (!anyExposed && region.hasBreaches()) {
                // Room is now sealed — clear all breaches
                region.getBreachPoints().clear();
                if (!region.isSourceless()) {
                    region.continueFill();
                    fillingRegions.add(region);
                }
            }
        }
    }

    /**
     * Update pressure for all regions based on breach state.
     */
    private void updatePressures(World world) {
        for (AtmosphereRegion region : new ArrayList<>(regions)) {
            // Prune breaches that are no longer passable (block placed at breach pos)
            region.validateBreaches(world);

            int volume = region.getVolume();
            if (volume == 0) continue;

            if (region.hasBreaches()) {
                // Depressurize: rate scales with number of breaches, inversely with volume
                double delta = region.getBreachCount() * DEPRESSURIZE_RATE / volume;
                region.setPressure(region.getPressure() - delta);

                // Fully depressurized — clear the room shape
                if (region.getPressure() <= 0.0) {
                    if (region.isSourceless()) {
                        // Sourceless regions can't refill — just clear the octree
                        for (BlockPos p : region.getAllPositions()) {
                            region.getOctree().remove(p);
                        }
                    } else {
                        region.resetFill();
                    }
                    region.setPressure(0.0);
                }
            } else if (region.isFillComplete() && !region.isFillFailed()) {
                // Pressurize: sealed room with oxygen being supplied
                double delta = PRESSURIZE_RATE / volume * region.clearOxygens();
                region.setPressure(region.getPressure() + delta);
            }
        }
    }

    private void processFloodFills(World world) {
        List<AtmosphereRegion> completed = new ArrayList<>();

        for (AtmosphereRegion region : fillingRegions) {
            // Don't fill while breached — the room is actively losing pressure
            if (region.hasBreaches()) continue;

            boolean done = region.floodFill(world, FILL_BUDGET_PER_TICK);
            if (done) {
                completed.add(region);
            }
        }

        fillingRegions.removeAll(completed);

        // Safety net: absorb sourceless regions whose primary position is now inside a completed non-sourceless fill
        for (AtmosphereRegion filled : completed) {
            if (filled.isSourceless()) continue;
            regions.removeIf(r -> r != filled && r.isSourceless() && filled.contains(r.getSource()));
        }
    }

    /**
     * Re-start fill for regions whose source is in a sealed environment.
     * Handles recovery after full depressurization + breach repair.
     */
    private void reRefillRegions(World world) {
        for (AtmosphereRegion region : regions) {
            if (region.isSourceless()) continue;
            if (region.hasBreaches()) continue;
            if (region.isFillComplete() && !region.isFillFailed() && region.getVolume() > 0) continue;
            if (fillingRegions.contains(region)) continue;

            // Check if any disperser is in a viable (passable, sealed) position
            boolean anyViable = false;
            for (BlockPos src : region.getDispersers()) {
                if (!world.isBlockFullCube(src) && !isExposedToVacuum(world, src)) {
                    anyViable = true;
                    break;
                }
            }
            if (!anyViable) continue;

            if (region.getVolume() == 0 || region.isFillFailed()) {
                region.resetFill();
                region.setPressure(0.0);
                fillingRegions.add(region);
            }
        }
    }

    /**
     * Check if a position can reach sky (vacuum) via passable blocks.
     */
    private boolean isExposedToVacuum(World world, BlockPos start) {
        return isExposedToVacuum(world, start, null);
    }

    /**
     * Check if a position can reach sky (vacuum) via passable blocks.
     * {@code treatedAsPassable} is an extra position considered passable even if
     * it is currently solid (used for BreakEvent, which fires before removal).
     */
    private boolean isExposedToVacuum(World world, BlockPos start, BlockPos treatedAsPassable) {
        if (!start.equals(treatedAsPassable) && world.isBlockFullCube(start)) return false;
        if (world.canSeeSky(start)) return true;

        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);
        int limit = 50000;

        while (!queue.isEmpty()) {
            if (visited.size() > limit) {
                return true;
            }

            BlockPos pos = queue.poll();

            for (BlockPos nb : BlockPosUtil.neighbors(pos)) {
                if (visited.contains(nb)) continue;
                if (!nb.equals(treatedAsPassable) && world.isBlockFullCube(nb)) continue;
                if (world.canSeeSky(nb)) return true;
                visited.add(nb);
                queue.add(nb);
            }
        }

        return false;
    }

    // ---- NBT serialization ----

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList regionList = new NBTTagList();
        for (AtmosphereRegion region : regions) {
            NBTTagCompound regionTag = new NBTTagCompound();
            BlockPos src = region.getSource();
            regionTag.setInteger("srcX", src.getX());
            regionTag.setInteger("srcY", src.getY());
            regionTag.setInteger("srcZ", src.getZ());
            regionTag.setDouble("pressure", region.getPressure());
            regionTag.setInteger("octreeOriginX", region.getOctree().getOriginX());
            regionTag.setInteger("octreeOriginY", region.getOctree().getOriginY());
            regionTag.setInteger("octreeOriginZ", region.getOctree().getOriginZ());
            regionTag.setInteger("octreeSize", region.getOctree().getTreeSize());

            // Save per-region disperser positions
            Set<BlockPos> regionDispersers = region.getDispersers();
            int[] dispCoords = new int[regionDispersers.size() * 3];
            int di = 0;
            for (BlockPos dp : regionDispersers) {
                dispCoords[di++] = dp.getX();
                dispCoords[di++] = dp.getY();
                dispCoords[di++] = dp.getZ();
            }
            regionTag.setIntArray("dispersers", dispCoords);

            // Serialize octree structure (much smaller than listing all positions)
            regionTag.setByteArray("octreeData", region.getOctree().serialize());
            regionList.appendTag(regionTag);
        }
        nbt.setTag("regions", regionList);
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList regionList = nbt.getTagList("regions", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < regionList.tagCount(); i++) {
            NBTTagCompound regionTag = regionList.getCompoundTagAt(i);
            BlockPos src = new BlockPos(
                    regionTag.getInteger("srcX"),
                    regionTag.getInteger("srcY"),
                    regionTag.getInteger("srcZ"));
            double pressure = regionTag.getDouble("pressure");
            int octreeOriginX = regionTag.getInteger("octreeOriginX");
            int octreeOriginY = regionTag.getInteger("octreeOriginY");
            int octreeOriginZ = regionTag.getInteger("octreeOriginZ");
            int octreeSize = regionTag.getInteger("octreeSize");

            // Load per-region dispersers
            Set<BlockPos> regionDispersers = new HashSet<>();
            int[] dispCoords = regionTag.getIntArray("dispersers");
            for (int j = 0; j + 2 < dispCoords.length; j += 3) {
                regionDispersers.add(new BlockPos(dispCoords[j], dispCoords[j + 1], dispCoords[j + 2]));
            }

            // Backward compat: if no per-region dispersers, check old "sourceless" flag
            if (regionDispersers.isEmpty() && !regionTag.getBoolean("sourceless")) {
                // Old format: source was the single disperser
                regionDispersers.add(src);
            }

            byte[] octreeData = regionTag.getByteArray("octreeData");
            Octree octree = Octree.deserialize(octreeOriginX, octreeOriginY, octreeOriginZ, octreeSize, octreeData);
            if (octree.isEmpty()) continue;
            AtmosphereRegion region = new AtmosphereRegion(src, octree, pressure, regionDispersers);

            regions.add(region);

            // Rebuild global disperser set
            dispersers.addAll(regionDispersers);
        }
    }
}
