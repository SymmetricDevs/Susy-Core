package supersymmetry.common.world;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.Collections;
import java.util.List;

public class ChunkGeneratorVoid implements IChunkGenerator {

    private final World world;

    public ChunkGeneratorVoid(World world) {
        this.world = world;
    }

    @Override
    public Chunk generateChunk(int x, int z) {
        ChunkPrimer primer = new ChunkPrimer();
        // ChunkPrimer is all-air by default — nothing to place.

        Chunk chunk = new Chunk(world, primer, x, z);

        // Populate biome array so the chunk is valid.
        Biome[] biomes = world.getBiomeProvider().getBiomes(null, x * 16, z * 16, 16, 16);
        byte[] biomeIds = new byte[biomes.length];
        for (int i = 0; i < biomes.length; i++) {
            biomeIds[i] = (byte) Biome.getIdForBiome(biomes[i]);
        }
        chunk.setBiomeArray(biomeIds);

        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(int x, int z) {
        // No ores, trees, structures, or other decoration.
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
        // No structures to recreate.
    }
    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored)
    {
        return new BlockPos(0,0,0); // no structures haha oliwier get to work and make some
    }
    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos position) {
        return false; // no structures haha oliwier get to work and make some
    }
}
