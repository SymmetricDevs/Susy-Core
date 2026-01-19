package supersymmetry.common.world.gen;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenPit extends WorldGenerator {

    protected static final IBlockState AIR = Blocks.AIR.getDefaultState();

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        IBlockState state = worldIn.getBlockState(position);
        int size = state.getBlock().getMetaFromState(state) + 1;
        worldIn.setBlockState(position, AIR, 2);
        IBlockState biomeBlock = worldIn.getBiome(position).topBlock;

        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                if (x * x + z * z <= size * size * rand.nextFloat(0x.cp0f, 0x1.4p0f)) {
                    int top = worldIn.getHeight(position.getX() + x, position.getZ() + z);
                    if (top < 0x40) top = 0x40;
                    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(position.getX() + x, top,
                            position.getZ() + z);

                    for (int i = 0; i < 5; i++) {
                        worldIn.setBlockState(pos, AIR, 2);
                        pos.move(EnumFacing.DOWN);
                    }

                    for (int i = 0; i < 0x20 && worldIn.getBlockState(pos) != MapGenLunarLavaTube.BASALT; i++) {
                        pos.move(EnumFacing.DOWN);
                    }

                    int height = (int) (size * size * rand.nextFloat(0x.8p0f, 0x1.0p0f) / (x * x + z * z + size)) +
                            rand.nextInt(1);
                    for (int i = 0; i < height; i++) {
                        pos.move(EnumFacing.UP);
                        worldIn.setBlockState(pos, biomeBlock, 2);
                    }
                }
            }
        }
        return true;
    }
}
