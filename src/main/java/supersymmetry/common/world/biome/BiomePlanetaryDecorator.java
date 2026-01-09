package supersymmetry.common.world.biome;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.gen.feature.WorldGenerator;

public class BiomePlanetaryDecorator extends BiomeDecorator {

    @Override
    public void decorate(World worldIn, Random random, Biome biome, BlockPos pos) {
        this.mushroomBrownGen = new NoGenerator();
        this.mushroomRedGen = new NoGenerator();
        super.decorate(worldIn, random, biome, pos);
    }

    private static class NoGenerator extends WorldGenerator {

        @Override
        public boolean generate(World worldIn, Random rand, BlockPos position) {
            return false;
        }
    }
}
