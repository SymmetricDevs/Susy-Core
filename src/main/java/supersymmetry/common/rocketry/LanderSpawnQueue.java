package supersymmetry.common.rocketry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import supersymmetry.Supersymmetry;

/**
 * WorldSavedData implementation that stores queued lander spawns.
 * This data persists across server restarts.
 */
public class LanderSpawnQueue extends WorldSavedData {

    private static final String DATA_NAME = Supersymmetry.MODID + "_LanderSpawnQueue";
    private final List<LanderSpawnEntry> queuedSpawns;

    public LanderSpawnQueue() {
        this(DATA_NAME);
    }

    public LanderSpawnQueue(String name) {
        super(name);
        this.queuedSpawns = new ArrayList<>();
    }

    /**
     * Gets or creates the LanderSpawnQueue for the given world.
     *
     * @param world The world to get the queue for
     * @return The LanderSpawnQueue instance for this world
     */
    public static LanderSpawnQueue get(World world) {
        MapStorage storage = world.getMapStorage();

        if (storage == null) {
            throw new RuntimeException("Null world storage when accessing LanderSpawnQueue");
        }

        LanderSpawnQueue instance = (LanderSpawnQueue) storage.getOrLoadData(LanderSpawnQueue.class, DATA_NAME);

        if (instance == null) {
            instance = new LanderSpawnQueue();
            storage.setData(DATA_NAME, instance);
        }

        return instance;
    }

    /**
     * Adds a new lander spawn entry to the queue.
     *
     * @param entry The entry to add
     */
    public void addEntry(LanderSpawnEntry entry) {
        this.queuedSpawns.add(entry);
        this.markDirty();
    }

    /**
     * Removes a lander spawn entry from the queue.
     *
     * @param uuid The UUID of the entry to remove
     * @return true if an entry was removed, false otherwise
     */
    public boolean removeEntry(UUID uuid) {
        boolean removed = this.queuedSpawns.removeIf(entry -> entry.getUuid().equals(uuid));
        if (removed) {
            this.markDirty();
        }
        return removed;
    }

    /**
     * Gets all queued spawn entries.
     *
     * @return A list of all queued entries
     */
    public List<LanderSpawnEntry> getEntries() {
        return new ArrayList<>(queuedSpawns);
    }

    /**
     * Gets an iterator for processing entries.
     * Use this when you need to remove entries while iterating.
     *
     * @return An iterator over the queued entries
     */
    public Iterator<LanderSpawnEntry> iterator() {
        return queuedSpawns.iterator();
    }

    /**
     * Gets the number of queued spawns.
     *
     * @return The queue size
     */
    public int size() {
        return queuedSpawns.size();
    }

    /**
     * Checks if the queue is empty.
     *
     * @return true if there are no queued spawns
     */
    public boolean isEmpty() {
        return queuedSpawns.isEmpty();
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound tag) {
        queuedSpawns.clear();

        NBTTagList tagList = tag.getTagList("QueuedSpawns", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound entryTag = tagList.getCompoundTagAt(i);
            LanderSpawnEntry entry = LanderSpawnEntry.deserializeNBT(entryTag);
            queuedSpawns.add(entry);
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound tag) {
        NBTTagList tagList = new NBTTagList();

        for (LanderSpawnEntry entry : queuedSpawns) {
            tagList.appendTag(entry.serializeNBT());
        }

        tag.setTag("QueuedSpawns", tagList);

        return tag;
    }
}
