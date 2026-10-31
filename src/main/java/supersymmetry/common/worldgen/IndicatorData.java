package supersymmetry.common.worldgen;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.WorldSavedData;

public final class IndicatorData extends WorldSavedData {

    public static final String NAME = "susy_pending_indicators_v1";
    public final PendingIndex<String, Task> pending = new PendingIndex<>();

    public static final class Task {

        public final long position;
        public final byte kind;
        public final NBTTagCompound state;

        public Task(long position, byte kind, NBTTagCompound state) {
            this.position = position;
            this.kind = kind;
            this.state = state;
        }

        public String key() {
            java.util.SortedMap<String, String> properties = new java.util.TreeMap<>();
            NBTTagCompound values = state.getCompoundTag("Properties");
            for (String name : values.getKeySet()) properties.put(name, values.getString(name));
            return position + ":" + kind + ":" + state.getString("Name") + ":" + properties;
        }
    }

    public IndicatorData() {
        this(NAME);
    }

    public IndicatorData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        pending.clear();
        NBTTagList tasks = nbt.getTagList("Tasks", 10);
        for (int i = 0; i < tasks.tagCount(); i++) {
            NBTTagCompound tag = tasks.getCompoundTagAt(i);
            Task task = new Task(tag.getLong("Position"), tag.getByte("Kind"), tag.getCompoundTag("State"));
            pending.add(tag.getLong("Chunk"), task.key(), task);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList tasks = new NBTTagList();
        for (long chunk : pending.chunkKeys()) for (java.util.Map.Entry<String, Task> entry : pending.entries(chunk)) {
            Task task = entry.getValue();
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("Position", task.position);
            tag.setByte("Kind", task.kind);
            tag.setLong("Chunk", chunk);
            tag.setTag("State", task.state);
            tasks.appendTag(tag);
        }
        nbt.setInteger("Version", 1);
        nbt.setTag("Tasks", tasks);
        return nbt;
    }
}
