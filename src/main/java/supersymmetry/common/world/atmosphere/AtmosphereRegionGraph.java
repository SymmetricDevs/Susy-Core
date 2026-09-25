package supersymmetry.common.world.atmosphere;

import java.util.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

/**
 * Top-level manager for the atmosphere/oxygen system.
 *
 * <p>
 * Maintains a collection of {@link AtmosphereRegion}s, each a connected body of air. Block changes that could
 * connect or disconnect air (a wall opening or closing, a vent being sealed, sky exposure changing) schedule a
 * rebuild of the affected regions, which re-derives their connected components from the world and redistributes
 * their air accordingly (see {@link AtmosphereRevalidator}).
 *
 * <p>
 * Pressure is tracked per region as a 0.0–1.0 value:
 * <ul>
 * <li>Decreases while the region has vents (its air is connected to vacuum)</li>
 * <li>Increases while a disperser supplies oxygen</li>
 * </ul>
 * Air cut off from every disperser is simulated only until its pressure is negligible, then dropped.
 */
public class AtmosphereRegionGraph {

    /**
     * Pressure decrease per vent per tick, scaled by 1/volume.
     */
    private static final double DEPRESSURIZE_RATE = 10.0;
    /**
     * Pressure increase per oxygen supply, scaled by 1/volume.
     */
    private static final double PRESSURIZE_RATE = 4.0;
    /**
     * Pressure at or below which air is considered gone.
     */
    static final double MIN_PRESSURE = 0.01;
    /**
     * Blocks processed per tick for region rebuilds.
     */
    private static final int REBUILD_BUDGET_PER_TICK = 2048;
    /**
     * How often (in ticks) to check that every region's vents are still open to vacuum.
     */
    private static final int VENT_CHECK_INTERVAL = 20;
    /**
     * How often (in ticks) to rebuild every region, catching changes that fired no block events.
     */
    private static final int REVALIDATE_INTERVAL = 1200;

    private final List<AtmosphereRegion> regions = new ArrayList<>();
    private final AtmosphereRevalidator revalidator = new AtmosphereRevalidator();

    // Global set of all disperser positions (for idempotent addDisperser)
    private final Set<BlockPos> dispersers = new HashSet<>();

    private int tickCounter = 0;
    // Rebuild everything on the first tick, since the world may have changed while unloaded
    private boolean revalidateAll = true;

    // ---- Disperser management ----

    public void addDisperser(BlockPos pos) {
        if (!dispersers.add(pos))
            return;

        AtmosphereRegion region = getRegionAt(pos);
        if (region != null) {
            region.addDisperser(pos);
            return;
        }

        // New region for this disperser; the rebuild floods it out from the disperser
        region = new AtmosphereRegion();
        region.addDisperser(pos);
        regions.add(region);
        revalidator.schedule(region);
    }

    public void removeDisperser(BlockPos pos) {
        if (!dispersers.remove(pos))
            return;
        for (AtmosphereRegion region : regions) {
            if (region.getDispersers().contains(pos)) {
                region.removeDisperser(pos);
                break;
            }
        }
    }

    // ---- Block changes ----

    /**
     * Called after the block at {@code pos} changed. Compares the world against the region model around that
     * position and schedules a rebuild of every region the change connects, disconnects or exposes.
     *
     * @return true if any region was affected
     */
    public boolean onBlockChanged(World world, BlockPos pos) {
        if (regions.isEmpty())
            return false;

        boolean passable = AtmosphereUtils.isPassable(world, pos);
        Set<AtmosphereRegion> affected = new HashSet<>();

        for (AtmosphereRegion region : regions) {
            if (region.contains(pos)) {
                // A cell was filled in, or is now exposed to the sky
                if (!passable || world.canSeeSky(pos))
                    affected.add(region);
            } else if (region.getVents().contains(pos)) {
                // A vent was sealed
                if (!AtmosphereUtils.isVent(world, pos))
                    affected.add(region);
            } else if (region.isAdjacentTo(pos)) {
                if (passable) {
                    // An opening: the region now connects to whatever lies beyond it
                    if (world.canSeeSky(pos))
                        region.getVents().add(pos); // start venting before the rebuild completes
                    affected.add(region);
                } else if (AtmosphereUtils.isPorous(world, pos) && AtmosphereUtils.isVent(world, pos)) {
                    // Opening plugged with something that isn't air-tight
                    region.getVents().add(pos);
                    affected.add(region);
                }
            } else if (passable && region.getDispersers().contains(pos)) {
                affected.add(region);
            }

            // The change may open or close vacuum behind a porous block next to the region
            for (BlockPos nb : AtmosphereUtils.neighbors(pos)) {
                if (!region.contains(nb) && region.isAdjacentTo(nb) && world.isBlockLoaded(nb) &&
                        region.getVents().contains(nb) != AtmosphereUtils.isVent(world, nb)) {
                    affected.add(region);
                    break;
                }
            }
        }

        // Sky exposure below pos changes only if pos is at the top of its column
        if (pos.getY() >= world.getHeight(pos.getX(), pos.getZ()) - 1) {
            collectSkyChanges(world, pos, affected);
        }

        if (affected.isEmpty())
            return false;
        revalidator.schedule(affected);
        return true;
    }

    /**
     * Scan down the column below {@code pos} (as far as its sky exposure can have changed) for cells that became
     * exposed or vents that were covered.
     */
    private void collectSkyChanges(World world, BlockPos pos, Set<AtmosphereRegion> affected) {
        for (BlockPos p = pos.down(); p.getY() >= 0; p = p.down()) {
            for (AtmosphereRegion region : regions) {
                if (region.contains(p) ? world.canSeeSky(p) :
                        region.getVents().contains(p) && !AtmosphereUtils.isVent(world, p)) {
                    affected.add(region);
                }
            }
            if (world.getBlockState(p).getLightOpacity(world, p) > 0)
                break;
        }
    }

    // ---- Queries ----

    public double getOxygenation(BlockPos pos) {
        AtmosphereRegion region = getRegionAt(pos);
        return region == null ? 0 : region.getPressure();
    }

    public AtmosphereRegion getRegionAt(BlockPos pos) {
        for (AtmosphereRegion region : regions) {
            if (region.contains(pos))
                return region;
        }
        return null;
    }

    // ---- Tick ----

    /**
     * @return true if there is any atmosphere state that may have changed (and should be saved)
     */
    public boolean tick(World world) {
        tickCounter++;

        if (revalidateAll || tickCounter % REVALIDATE_INTERVAL == 0) {
            revalidateAll = false;
            for (AtmosphereRegion region : regions) {
                if (!region.isRebuilding())
                    revalidator.schedule(region);
            }
        } else if (tickCounter % VENT_CHECK_INTERVAL == 0) {
            checkVents(world);
        }

        updatePressures();
        revalidator.tick(world, regions, REBUILD_BUDGET_PER_TICK, this::onRebuilt);

        // Stop simulating air that has no disperser and has (nearly) all leaked away
        regions.removeIf(r -> !r.isRebuilding() && r.isSourceless() &&
                (r.getVolume() == 0 || r.getPressure() <= MIN_PRESSURE));

        return !regions.isEmpty();
    }

    private void onRebuilt(Collection<AtmosphereRegion> oldRegions, List<AtmosphereRegion> newRegions,
                           boolean dirty) {
        regions.removeAll(oldRegions);
        regions.addAll(newRegions);
        if (dirty && !newRegions.isEmpty()) {
            revalidator.schedule(newRegions);
        }
    }

    /**
     * Schedule a rebuild for any region with a vent that no longer leads to vacuum. Catches block changes that
     * fired no neighbor notification.
     */
    private void checkVents(World world) {
        for (AtmosphereRegion region : regions) {
            if (region.isRebuilding())
                continue;
            for (BlockPos vent : region.getVents()) {
                if (world.isBlockLoaded(vent) && !AtmosphereUtils.isVent(world, vent)) {
                    revalidator.schedule(region);
                    break;
                }
            }
        }
    }

    private void updatePressures() {
        for (AtmosphereRegion region : regions) {
            int supplied = region.clearOxygens();
            int volume = region.getVolume();
            if (volume == 0) {
                region.setPressure(0.0);
                continue;
            }

            double delta = (supplied * PRESSURIZE_RATE - region.getLeakCount() * DEPRESSURIZE_RATE) / volume;
            region.setPressure(region.getPressure() + delta);
            if (region.isLeaking() && region.getPressure() <= MIN_PRESSURE) {
                region.setPressure(0.0);
            }
        }
    }

    // ---- NBT serialization ----

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList regionList = new NBTTagList();
        for (AtmosphereRegion region : regions) {
            regionList.appendTag(region.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag("regions", regionList);
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList regionList = nbt.getTagList("regions", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < regionList.tagCount(); i++) {
            AtmosphereRegion region = AtmosphereRegion.readFromNBT(regionList.getCompoundTagAt(i));
            if (region.getVolume() == 0 && region.isSourceless())
                continue;
            regions.add(region);
            dispersers.addAll(region.getDispersers());
        }
    }
}
