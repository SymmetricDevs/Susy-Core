package supersymmetry.common.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;


public class PlanetBiome extends Biome {

    private int skyColor;

    private String name;

    public PlanetBiome(BiomeProperties properties, IBlockState filler, IBlockState top, int skyColor) {
        super(properties);
        this.skyColor = skyColor;
        fillerBlock = filler;
        topBlock = top;
    }

    @Override
    public void decorate(World par1World, Random par2Random, BlockPos pos) {
        // TODO Make the world feel alive
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getSkyColorByTemp(float par1) {
        return skyColor;
    }
}
