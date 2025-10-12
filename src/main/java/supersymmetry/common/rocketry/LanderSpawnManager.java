package supersymmetry.common.rocketry;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import supersymmetry.api.SusyLog;

/**
 * Public API for queueing lander spawns.
 * This manager provides convenient methods for scheduling EntityLander spawns
 * with arbitrary delays that persist across server restarts.
 */
public class LanderSpawnManager {

    /**
     * Queues a lander to spawn at the specified coordinates after a delay.
     *
     * @param world          The world to queue the spawn in (used to access the queue)
     * @param dimensionId    The dimension ID where the lander should spawn
     * @param x              The X coordinate
     * @param y              The Y coordinate
     * @param z              The Z coordinate
     * @param tickDelay      The number of ticks to wait before spawning
     */
    public static void queueLanderSpawn(World world, int dimensionId, double x, double y, double z, int tickDelay) {
        queueLanderSpawn(world, dimensionId, x, y, z, tickDelay, null);
    }

    /**
     * Queues a lander to spawn at the specified BlockPos after a delay.
     *
     * @param world          The world to queue the spawn in (used to access the queue)
     * @param dimensionId    The dimension ID where the lander should spawn
     * @param pos            The position where the lander should spawn
     * @param tickDelay      The number of ticks to wait before spawning
     */
    public static void queueLanderSpawn(World world, int dimensionId, BlockPos pos, int tickDelay) {
        queueLanderSpawn(world, dimensionId, pos, tickDelay, null);
    }

    /**
     * Queues a lander to spawn at the specified coordinates after a delay with inventory data.
     *
     * @param world          The world to queue the spawn in (used to access the queue)
     * @param dimensionId    The dimension ID where the lander should spawn
     * @param x              The X coordinate
     * @param y              The Y coordinate
     * @param z              The Z coordinate
     * @param tickDelay      The number of ticks to wait before spawning
     * @param inventoryData  Optional NBT data containing inventory contents (can be null)
     */
    public static void queueLanderSpawn(World world, int dimensionId, double x, double y, double z, int tickDelay,
                                        @Nullable NBTTagCompound inventoryData) {
        if (tickDelay < 0) {
            SusyLog.logger.warn("Attempted to queue lander spawn with negative tick delay: {}", tickDelay);
            return;
        }

        LanderSpawnEntry entry = new LanderSpawnEntry(dimensionId, x, y, z, tickDelay, inventoryData);
        LanderSpawnQueue queue = LanderSpawnQueue.get(world);
        queue.addEntry(entry);

        SusyLog.logger.info("Queued lander spawn: {}", entry);
    }

    /**
     * Queues a lander to spawn at the specified BlockPos after a delay with inventory data.
     *
     * @param world          The world to queue the spawn in (used to access the queue)
     * @param dimensionId    The dimension ID where the lander should spawn
     * @param pos            The position where the lander should spawn
     * @param tickDelay      The number of ticks to wait before spawning
     * @param inventoryData  Optional NBT data containing inventory contents (can be null)
     */
    public static void queueLanderSpawn(World world, int dimensionId, BlockPos pos, int tickDelay,
                                        @Nullable NBTTagCompound inventoryData) {
        LanderSpawnEntry entry = new LanderSpawnEntry(dimensionId, pos, tickDelay, inventoryData);
        LanderSpawnQueue queue = LanderSpawnQueue.get(world);
        queue.addEntry(entry);

        SusyLog.logger.info("Queued lander spawn: {}", entry);
    }

    /**
     * Queues a lander to spawn with inventory contents from an IItemHandlerModifiable.
     * This is useful for asteroid harvesting missions where the lander returns with cargo.
     *
     * @param world          The world to queue the spawn in (used to access the queue)
     * @param dimensionId    The dimension ID where the lander should spawn
     * @param pos            The position where the lander should spawn
     * @param tickDelay      The number of ticks to wait before spawning
     * @param inventory      The inventory to copy into the lander
     */
    public static void queueLanderSpawnWithInventory(World world, int dimensionId, BlockPos pos, int tickDelay,
                                                      IItemHandlerModifiable inventory) {
        NBTTagCompound inventoryData = serializeInventory(inventory);
        queueLanderSpawn(world, dimensionId, pos, tickDelay, inventoryData);
    }

    /**
     * Queues a lander to spawn with inventory contents from an IItemHandlerModifiable.
     *
     * @param world          The world to queue the spawn in (used to access the queue)
     * @param dimensionId    The dimension ID where the lander should spawn
     * @param x              The X coordinate
     * @param y              The Y coordinate
     * @param z              The Z coordinate
     * @param tickDelay      The number of ticks to wait before spawning
     * @param inventory      The inventory to copy into the lander
     */
    public static void queueLanderSpawnWithInventory(World world, int dimensionId, double x, double y, double z,
                                                      int tickDelay, IItemHandlerModifiable inventory) {
        NBTTagCompound inventoryData = serializeInventory(inventory);
        queueLanderSpawn(world, dimensionId, x, y, z, tickDelay, inventoryData);
    }

    /**
     * Serializes an IItemHandlerModifiable to NBT.
     *
     * @param inventory The inventory to serialize
     * @return NBT representation of the inventory
     */
    private static NBTTagCompound serializeInventory(IItemHandlerModifiable inventory) {
        if (inventory instanceof ItemStackHandler) {
            return ((ItemStackHandler) inventory).serializeNBT();
        }

        // Fallback: create a new ItemStackHandler and copy items
        ItemStackHandler handler = new ItemStackHandler(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            handler.setStackInSlot(i, inventory.getStackInSlot(i).copy());
        }
        return handler.serializeNBT();
    }

    /**
     * Gets the number of queued lander spawns for a world.
     *
     * @param world The world to check
     * @return The number of queued spawns
     */
    public static int getQueueSize(World world) {
        return LanderSpawnQueue.get(world).size();
    }

    /**
     * Checks if there are any queued lander spawns for a world.
     *
     * @param world The world to check
     * @return true if there are queued spawns
     */
    public static boolean hasQueuedSpawns(World world) {
        return !LanderSpawnQueue.get(world).isEmpty();
    }
}
