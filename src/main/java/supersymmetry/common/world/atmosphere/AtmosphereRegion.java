package supersymmetry.common.world.atmosphere;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

/**
 * A single connected body of air, stored as an {@link OctreeVolume}.
 *
 * <p>
 * The volume is every passable, non-sky-exposed block reachable from the region's cells, as determined by the
 * {@link AtmosphereRevalidator}. Where that connected space touches vacuum, the sky-exposed positions it reaches
 * (and porous blocks, like regolith, with vacuum right behind them) are recorded as <em>vents</em>. A region leaks for
 * as long as it has vents; there is no separately tracked breach state that could go stale.
 *
 * <p>
 * Pressure is tracked as a separate value (0.0–1.0) describing how much air is in the volume. When regions are
 * connected or disconnected, the air they held is redistributed over the resulting volumes.
 */
public class AtmosphereRegion {

    private final OctreeVolume volume;
    private double pressure; // 0.0 .. 1.0

    // Active disperser positions within this region
    private final Set<BlockPos> dispersers = new HashSet<>();

    // Positions adjacent to this region through which air escapes to vacuum (see AtmosphereUtils.isVent)
    private final Set<BlockPos> vents = new HashSet<>();
    // Openings into space too large to track (the flood fill hit the volume cap)
    private int overflowVents = 0;

    private int oxygensSupplied = 0;
    private boolean rebuilding = false;

    public AtmosphereRegion() {
        this(new OctreeVolume(), 0.0);
    }

    public AtmosphereRegion(OctreeVolume volume, double pressure) {
        this.volume = volume;
        setPressure(pressure);
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

    public boolean isSourceless() {
        return dispersers.isEmpty();
    }

    /**
     * Mark that oxygen was supplied by a disperser.
     */
    public void markOxygenSupplied() {
        oxygensSupplied++;
    }

    /**
     * Returns the number of oxygen supplies since the last call, and resets the counter.
     */
    public int clearOxygens() {
        int temp = oxygensSupplied;
        oxygensSupplied = 0;
        return temp;
    }

    // ---- Leaks ----

    public Set<BlockPos> getVents() {
        return vents;
    }

    public void setOverflowVents(int overflowVents) {
        this.overflowVents = overflowVents;
    }

    public int getLeakCount() {
        return vents.size() + overflowVents;
    }

    public boolean isLeaking() {
        return getLeakCount() > 0;
    }

    // ---- Queries ----

    public boolean contains(BlockPos pos) {
        return volume.contains(pos);
    }

    public boolean isAdjacentTo(BlockPos pos) {
        for (BlockPos nb : AtmosphereUtils.neighbors(pos)) {
            if (volume.contains(nb))
                return true;
        }
        return false;
    }

    public OctreeVolume getCells() {
        return volume;
    }

    public int getVolume() {
        return volume.size();
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = Math.max(0.0, Math.min(1.0, pressure));
    }

    /**
     * Whether the region's shape is up to date, i.e. it is not waiting on a rebuild.
     */
    public boolean isFillComplete() {
        return !rebuilding;
    }

    public boolean isRebuilding() {
        return rebuilding;
    }

    void setRebuilding(boolean rebuilding) {
        this.rebuilding = rebuilding;
    }

    // ---- NBT serialization ----

    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setDouble("pressure", pressure);
        tag.setIntArray("dispersers", AtmosphereUtils.toIntArray(dispersers));
        tag.setIntArray("vents", AtmosphereUtils.toIntArray(vents));
        tag.setInteger("overflowVents", overflowVents);
        tag.setTag("volume", volume.writeToNBT());
        return tag;
    }

    public static AtmosphereRegion readFromNBT(NBTTagCompound tag) {
        OctreeVolume volume;
        if (tag.hasKey("octreeData")) {
            // Legacy format: a single octree with an explicit origin and size
            Octree octree = Octree.deserialize(tag.getInteger("octreeOriginX"), tag.getInteger("octreeOriginY"),
                    tag.getInteger("octreeOriginZ"), tag.getInteger("octreeSize"), tag.getByteArray("octreeData"));
            volume = new OctreeVolume();
            for (BlockPos pos : octree) {
                volume.insert(pos);
            }
        } else {
            volume = OctreeVolume.readFromNBT(tag.getTagList("volume", Constants.NBT.TAG_COMPOUND));
        }

        AtmosphereRegion region = new AtmosphereRegion(volume, tag.getDouble("pressure"));
        AtmosphereUtils.fromIntArray(tag.getIntArray("dispersers"), region.dispersers);
        AtmosphereUtils.fromIntArray(tag.getIntArray("vents"), region.vents);
        region.overflowVents = tag.getInteger("overflowVents");

        // Legacy format: the source position was the single disperser
        if (!tag.hasKey("dispersers") && tag.hasKey("srcX") && !tag.getBoolean("sourceless")) {
            region.dispersers.add(new BlockPos(tag.getInteger("srcX"), tag.getInteger("srcY"),
                    tag.getInteger("srcZ")));
        }
        return region;
    }
}
