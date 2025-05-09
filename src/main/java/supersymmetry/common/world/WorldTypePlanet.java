package supersymmetry.common.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.List;

public class WorldTypePlanet extends WorldType {
    private List<Biome> biomes;
    private IBlockState stone;
    private IBlockState bedrock;

    public WorldTypePlanet(String name, List<Biome> biomes, IBlockState stone, IBlockState bedrock) {
        super(name);
        this.biomes = biomes;
        this.stone = stone;
        this.bedrock = bedrock;
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
        return new PlanetBiomeProvider(world, biomes);
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        return new PlanetChunkGenerator(world, world.getSeed(), stone, bedrock);
    }

}
