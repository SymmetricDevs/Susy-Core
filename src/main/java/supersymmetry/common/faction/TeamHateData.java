package supersymmetry.common.faction;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.NBTDataStorage;

public class TeamHateData implements NBTDataStorage.Data {

    public static final String ID = "susy_hate";
    private static final String KEY_HATE = "hate";
    private static final String KEY_BASELINE = "baseline";

    private final ForgeTeam team;
    private final Map<String, Integer> hate = new HashMap<>();
    private int baseline = 0;

    public TeamHateData(ForgeTeam team) {
        this.team = team;
    }

    public static TeamHateData get(ForgeTeam team) {
        return (TeamHateData) team.getData().get(ID);
    }

    @Override
    public String getId() {
        return ID;
    }

    public int getHate(String faction) {
        return hate.getOrDefault(faction, 0);
    }

    public void setHate(String faction, int amount) {
        if (hate.getOrDefault(faction, 0) != amount) {
            hate.put(faction, amount);
            team.markDirty();
        }
    }

    public Map<String, Integer> getAll() {
        return hate;
    }

    public int getBaseline() {
        return baseline;
    }

    public void raiseBaseline(int value) {
        if (value > baseline) {
            baseline = value;
            team.markDirty();
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger(KEY_BASELINE, baseline);
        NBTTagCompound factions = new NBTTagCompound();
        hate.forEach(factions::setInteger);
        nbt.setTag(KEY_HATE, factions);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        hate.clear();
        baseline = nbt.getInteger(KEY_BASELINE);
        if (nbt.hasKey(KEY_HATE, 10)) {
            NBTTagCompound factions = nbt.getCompoundTag(KEY_HATE);
            for (String key : factions.getKeySet()) {
                hate.put(key, factions.getInteger(key));
            }
        } else {
            // legacy flat format
            for (String key : nbt.getKeySet()) {
                if (!key.equals(KEY_BASELINE)) hate.put(key, nbt.getInteger(key));
            }
        }
    }
}
