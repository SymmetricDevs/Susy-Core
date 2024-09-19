package supersymmetry.api.recipes.properties;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.biome.Biome;

public class BiomeMultiPropertyList {

    public static BiomeMultiPropertyList EMPTY_LIST = new BiomeMultiPropertyList();

    public IntList whiteListBiomes = new IntArrayList();
    public IntList blackListBiomes = new IntArrayList();

    public void add(String key, boolean toBlacklist) {
        if (toBlacklist) {
            blackListBiomes.add(key);
            whiteListBiomes.rem(key);
        } else {
            whiteListBiomes.add(key);
            blackListBiomes.rem(key);
        }
    }

    public void merge(BiomeMultiPropertyList list) {
        this.whiteListBiomes.addAll(list.whiteListBiomes);
        this.blackListBiomes.addAll(list.blackListBiomes);
    }

    public boolean checkBiome(Biome biome) {
        boolean valid = true;
        if (!this.blackListBiomes.isEmpty()) valid = !this.blackListBiomes.contains(biome);
        if (!this.whiteListBiomes.isEmpty()) valid = this.whiteListBiomes.contains(biome);
        return valid;
    }
}
