package supersymmetry.common.world.layer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Biomes;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraftforge.common.BiomeManager.BiomeEntry;

public class PlanetGenLayerBiomes extends GenLayer {

    protected List<BiomeEntry> allowedBiomes;

    public PlanetGenLayerBiomes(long seed, GenLayer genlayer) {
        super(seed);
        parent = genlayer;
        this.allowedBiomes = new ArrayList<>();
    }

    public PlanetGenLayerBiomes setBiomeList(List<BiomeEntry> biomes) {
        this.allowedBiomes = biomes;
        return this;
    }

    @Override
    public int[] getInts(int x, int z, int width, int depth) {
        if (allowedBiomes == null || allowedBiomes.isEmpty()) {
            throw new RuntimeException(
                    "PlanetGenLayerBiomes has no biomes! I guess something called this before it was initialized?");
        }

        int[] dest = IntCache.getIntCache(width * depth);

        for (int dz = 0; dz < depth; dz++)
            for (int dx = 0; dx < width; dx++) {
                initChunkSeed(dx + x, dz + z);
                dest[dx + dz * width] = Biome.getIdForBiome(getWeightedBiomeEntry().biome);
            }
        return dest;
    }

    protected BiomeEntry getWeightedBiomeEntry() {
        if (allowedBiomes == null || allowedBiomes.isEmpty())
            return new BiomeEntry(Biomes.OCEAN, 100);

        List<BiomeEntry> biomeList = allowedBiomes;
        int totalWeight = WeightedRandom.getTotalWeight(biomeList);
        // TODO: amortize the above
        int weight = nextInt(totalWeight);
        return WeightedRandom.getRandomItem(biomeList, weight);
    }
}
