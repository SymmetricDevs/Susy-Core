package supersymmetry.api.recipes.properties;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import java.util.ArrayList;
import java.util.Set;

public class BiomeMultiPropertyList {

    public static BiomeMultiPropertyList EMPTY_LIST = new BiomeMultiPropertyList();

    public ArrayList<String> whiteListBiomes = new ArrayList<String>(16); //I will change this hardcoded limit if you somehow whitelist more than 16,
    public ArrayList<String> blackListBiomes = new ArrayList<String>(16);

    public void add(String key, boolean toBlacklist) {
        if (toBlacklist) {
            blackListBiomes.add(key);
            whiteListBiomes.remove(key);
        } else {
            whiteListBiomes.add(key);
            blackListBiomes.remove(key);
        }
    }

    public void merge(BiomeMultiPropertyList list) {
        this.whiteListBiomes.addAll(list.whiteListBiomes);
        this.blackListBiomes.addAll(list.blackListBiomes);
    }

    public boolean checkBiome(Biome biome) {
        boolean valid = true;
        Set<BiomeDictionary.Type> biomeTypes = BiomeDictionary.getTypes(biome);
        if (!this.blackListBiomes.isEmpty()) {
            for (int i = 0; i < blackListBiomes.size(); i++) {
                valid = biomeTypes.contains(BiomeDictionary.Type.blackListBiomes[i]);
            }
        }
        //I'm trying to do the following:
        //  if blacklist has a list of biometype,
        //  valid will grab a String from the array and check if BiomeDictionary.Type.blackListBiomes[i] matches one of biomeTypes
        //  yes -> valid is false, return immediately
        if (!this.whiteListBiomes.isEmpty()) {
            valid = this.whiteListBiomes.contains(biome);
        }
        return valid;
    }
}
