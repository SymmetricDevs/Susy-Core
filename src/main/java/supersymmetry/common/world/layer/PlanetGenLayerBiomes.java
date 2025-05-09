package supersymmetry.common.world.layer;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class PlanetGenLayerBiomes extends GenLayer {

    protected Biome[] allowedBiomes;

    public PlanetGenLayerBiomes(Biome[] biomes, long seed, GenLayer genlayer) {
        super(seed);
        parent = genlayer;
        this.allowedBiomes = biomes;
    }

    public PlanetGenLayerBiomes(Biome[] biomes, long seed) {
        super(seed);
        this.allowedBiomes = biomes;
    }

    @Override
    public int[] getInts(int x, int z, int width, int depth) {
        int[] dest = IntCache.getIntCache(width * depth);

        for (int dz = 0; dz < depth; dz++)
            for (int dx = 0; dx < width; dx++) {
                initChunkSeed(dx + x, dz + z);
                dest[dx + dz * width] = Biome.getIdForBiome(allowedBiomes[nextInt(allowedBiomes.length)]);
            }
        return dest;
    }
}
