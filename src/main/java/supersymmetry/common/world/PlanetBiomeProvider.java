package supersymmetry.common.world;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;
import net.minecraft.world.gen.layer.IntCache;
import supersymmetry.common.world.layer.PlanetGenLayer;
import supersymmetry.common.world.layer.PlanetGenLayerBiomes;

import java.util.List;

public class PlanetBiomeProvider extends BiomeProvider {
    private GenLayer biomeToUse;
    private GenLayer biomeIndex;
    private List<Biome> biomesToSpawnIn;
    private BiomeCache cache;

    public PlanetBiomeProvider(World world, List<Biome> biomes) {
        super(world.getWorldInfo());

        allowedBiomes = biomes;
        biomesToSpawnIn = biomes;
        cache = new BiomeCache(this);
    }

    @Override
    public GenLayer[] getModdedBiomeGenerators(WorldType worldType, long seed, GenLayer[] original) {
        GenLayer biomes = new PlanetGenLayer(allowedBiomes.toArray(new Biome[0]), seed);

        biomes = new PlanetGenLayerBiomes(allowedBiomes.toArray(new Biome[0]), seed, biomes);
        biomes = new GenLayerZoom(1000, biomes);
        biomes = new GenLayerZoom(1001, biomes);
        biomes = new GenLayerZoom(1002, biomes);
        biomes = new GenLayerZoom(1003, biomes);
        biomes = new GenLayerZoom(1004, biomes);

        GenLayer biomeIndexLayer = new GenLayerVoronoiZoom(10L, biomes);
        biomeIndexLayer.initWorldGenSeed(seed);

        biomeToUse = biomes;
        biomeIndex = biomeIndexLayer;

        return new GenLayer[] {
                biomes,
                biomeIndexLayer
        };
    }

    @Override
    public List<Biome> getBiomesToSpawnIn() {
        return biomesToSpawnIn;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return this.getBiome(pos, null);
    }

    @Override
    public Biome getBiome(BlockPos pos, Biome biomegen) {
        Biome biome = cache.getBiome(pos.getX(), pos.getZ(), biomegen);

        if (biome != null) {
            return biome;
        }
        return Biomes.TAIGA;
    }

    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int par2, int par3, int par4, int par5) {
        if (biomes == null || biomes.length < par4 * par5)
            biomes = new Biome[par4 * par5];

        int[] aint = biomeToUse.getInts(par2, par3, par4, par5);

        for (int i = 0; i < par4 * par5; ++i)
            if (aint[i] >= 0 && aint[i] <= Biome.REGISTRY.getKeys().size())
                biomes[i] = Biome.getBiome(aint[i]);
            else
                biomes[i] = Biomes.TAIGA;

        return biomes;
    }

    @Override
    public Biome[] getBiomes(Biome[] biomes, int x, int y, int width, int length, boolean cacheFlag) {
        IntCache.resetIntCache();

        if (biomes == null || biomes.length < width * length)
            biomes = new Biome[width * length];

        if (cacheFlag && width == 16 && length == 16 && (x & 15) == 0 && (y & 15) == 0) {
            Biome[] aBiome1 = cache.getCachedBiomes(x, y);
            System.arraycopy(aBiome1, 0, biomes, 0, width * length);
            return biomes;
        } else {
            int[] aint = biomeIndex.getInts(x, y, width, length);

            for (int i = 0; i < width * length; ++i)
                if (aint[i] >= 0 && aint[i] <= Biome.REGISTRY.getKeys().size())
                    biomes[i] = Biome.getBiome(aint[i]);
                else
                    biomes[i] = Biomes.TAIGA;

            return biomes;
        }
    }

    @Override
    public void cleanupCache() {
        cache.cleanupCache();
    }

}
