package supersymmetry.common.rocketry;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * Represents a queued lander spawn with all necessary data.
 * This entry will be processed by the LanderSpawnQueue system.
 */
public class LanderSpawnEntry {

    private final UUID uuid;
    private final int dimensionId;
    private final double x;
    private final double y;
    private final double z;
    private int ticksRemaining;
    private final NBTTagCompound inventoryData;

    /**
     * Creates a new lander spawn entry.
     *
     * @param dimensionId    The dimension ID where the lander should spawn
     * @param x              The X coordinate
     * @param y              The Y coordinate
     * @param z              The Z coordinate
     * @param ticksRemaining The number of ticks to wait before spawning
     * @param inventoryData  Optional NBT data containing inventory contents (can be null)
     */
    public LanderSpawnEntry(int dimensionId, double x, double y, double z, int ticksRemaining,
                            @Nullable NBTTagCompound inventoryData) {
        this.uuid = UUID.randomUUID();
        this.dimensionId = dimensionId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.ticksRemaining = ticksRemaining;
        this.inventoryData = inventoryData;
    }

    /**
     * Creates a new lander spawn entry from a BlockPos.
     *
     * @param dimensionId    The dimension ID where the lander should spawn
     * @param pos            The position where the lander should spawn
     * @param ticksRemaining The number of ticks to wait before spawning
     * @param inventoryData  Optional NBT data containing inventory contents (can be null)
     */
    public LanderSpawnEntry(int dimensionId, BlockPos pos, int ticksRemaining,
                            @Nullable NBTTagCompound inventoryData) {
        this(dimensionId, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, ticksRemaining, inventoryData);
    }

    /**
     * Private constructor for deserialization from NBT.
     */
    private LanderSpawnEntry(UUID uuid, int dimensionId, double x, double y, double z, int ticksRemaining,
                             @Nullable NBTTagCompound inventoryData) {
        this.uuid = uuid;
        this.dimensionId = dimensionId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.ticksRemaining = ticksRemaining;
        this.inventoryData = inventoryData;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getDimensionId() {
        return dimensionId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public void decrementTicks() {
        this.ticksRemaining--;
    }

    @Nullable
    public NBTTagCompound getInventoryData() {
        return inventoryData;
    }

    public boolean isReadyToSpawn() {
        return ticksRemaining <= 0;
    }

    /**
     * Serializes this entry to NBT.
     *
     * @return NBT representation of this entry
     */
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("UUID", uuid.toString());
        tag.setInteger("Dimension", dimensionId);
        tag.setDouble("X", x);
        tag.setDouble("Y", y);
        tag.setDouble("Z", z);
        tag.setInteger("TicksRemaining", ticksRemaining);

        if (inventoryData != null) {
            tag.setTag("InventoryData", inventoryData);
        }

        return tag;
    }

    /**
     * Deserializes a LanderSpawnEntry from NBT.
     *
     * @param tag The NBT tag to deserialize from
     * @return A new LanderSpawnEntry instance
     */
    public static LanderSpawnEntry deserializeNBT(NBTTagCompound tag) {
        UUID uuid = UUID.fromString(tag.getString("UUID"));
        int dimensionId = tag.getInteger("Dimension");
        double x = tag.getDouble("X");
        double y = tag.getDouble("Y");
        double z = tag.getDouble("Z");
        int ticksRemaining = tag.getInteger("TicksRemaining");

        NBTTagCompound inventoryData = null;
        if (tag.hasKey("InventoryData")) {
            inventoryData = tag.getCompoundTag("InventoryData");
        }

        return new LanderSpawnEntry(uuid, dimensionId, x, y, z, ticksRemaining, inventoryData);
    }

    @Override
    public String toString() {
        return String.format("LanderSpawnEntry[uuid=%s, dim=%d, pos=(%.1f, %.1f, %.1f), ticks=%d, hasInventory=%b]",
                uuid, dimensionId, x, y, z, ticksRemaining, inventoryData != null);
    }
}
