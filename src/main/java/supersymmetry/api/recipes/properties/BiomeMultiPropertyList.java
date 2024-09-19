package supersymmetry.api.recipes.properties;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

// It would've been better to have one list and swap between blacklist and whitelist, but that would've been
// a bit awkward to apply to the property in practice.
public class BiomeMultiPropertyList {

    public static BiomeMultiPropertyList EMPTY_LIST = new BiomeMultiPropertyList();

    public IntList whiteListBiomes = new IntArrayList();
    public IntList blackListBiomes = new IntArrayList();

    public void add(Biome key, boolean toBlacklist) {
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
