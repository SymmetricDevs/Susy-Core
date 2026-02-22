package supersymmetry.api.space.dimension.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import supersymmetry.common.world.biome.BiomePlanetaryDecorator;

import java.util.Random;

public class SpaceBiome extends Biome {

    public SpaceBiome(BiomeProperties properties) {
        super(properties);
        // mushrooms still generate with mushroomsPerChunk = 0;
        this.decorator = new BiomePlanetaryDecorator();
        this.decorator.generateFalls = false;
        this.decorator.flowersPerChunk = 0;
        this.decorator.grassPerChunk = 0;
        this.decorator.treesPerChunk = 0;

        // mushrooms still generate with mushroomsPerChunk = 0;
        this.decorator.mushroomsPerChunk = 0;
        this.decorator.cactiPerChunk = 0;
        this.decorator.deadBushPerChunk = 0;
        this.decorator.reedsPerChunk = 0;
        this.decorator.sandPatchesPerChunk = 0;
        this.decorator.gravelPatchesPerChunk = 0;
        this.decorator.clayPerChunk = 0;
        this.decorator.bigMushroomsPerChunk = 0;
    }

    @Override
    public void decorate(World worldIn, Random rand, BlockPos pos) {
        // Empty, prevents all vanilla decoration including structures
    }

    @Override
    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
       // nothing because its void bro like why else
    }
}
