package supersymmetry.common.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import supersymmetry.Supersymmetry;

public class MobHordeWorldData extends WorldSavedData
                               implements Function<UUID, MobHordePlayerData> {

    private static final String DATA_NAME = Supersymmetry.MODID + "_InvasionData";
    private final Map<UUID, MobHordePlayerData> playerDataMap;

    public MobHordeWorldData() {
        this(DATA_NAME);
    }

    public MobHordeWorldData(String name) {
        super(name);
        this.playerDataMap = new HashMap<>();
    }

    public static MobHordeWorldData get(World world) {
        MapStorage storage = world.getMapStorage();

        if (storage == null) {
            throw new RuntimeException("Null world storage");
        }

        MobHordeWorldData instance = (MobHordeWorldData) storage.getOrLoadData(MobHordeWorldData.class, DATA_NAME);

        if (instance == null) {
            instance = new MobHordeWorldData();
            storage.setData(DATA_NAME, instance);
        }
        return instance;
    }

    @Override
    public MobHordePlayerData apply(UUID uuid) {
        return this.getPlayerData(uuid);
    }

    public MobHordePlayerData getPlayerData(UUID uuid) {
        MobHordePlayerData invasionPlayerData = this.playerDataMap.get(uuid);

        if (invasionPlayerData == null) {
            invasionPlayerData = new MobHordePlayerData(1000);
            this.playerDataMap.put(uuid, invasionPlayerData);
            this.markDirty();
        }

        return invasionPlayerData;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound tag) {
        NBTTagList tagList = tag.getTagList("PlayerData", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tagEntry = (NBTTagCompound) tagList.get(i);
            UUID uuid = UUID.fromString(tagEntry.getString("UUID"));
            MobHordePlayerData data = new MobHordePlayerData(1000);
            data.deserializeNBT(tagEntry.getCompoundTag("Data"));
            this.playerDataMap.put(uuid, data);
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound tag) {
        NBTTagList tagList = new NBTTagList();

        for (Map.Entry<UUID, MobHordePlayerData> entry : this.playerDataMap.entrySet()) {
            NBTTagCompound tagEntry = new NBTTagCompound();
            tagEntry.setString("UUID", entry.getKey().toString());
            tagEntry.setTag("Data", entry.getValue().serializeNBT());
            tagList.appendTag(tagEntry);
        }

        tag.setTag("PlayerData", tagList);

        return tag;
    }
}
