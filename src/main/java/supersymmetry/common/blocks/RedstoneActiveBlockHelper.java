package supersymmetry.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import gregtech.api.block.VariantActiveBlock;

public final class RedstoneActiveBlockHelper {

    private RedstoneActiveBlockHelper() {}

    public static void onBlockAdded(World world, BlockPos pos, boolean inverted) {
        if (!world.isRemote) {
            boolean powered = world.isBlockPowered(pos);
            VariantActiveBlock.setBlockActive(world.provider.getDimension(), pos, inverted != powered);
        }
    }

    public static void neighborChanged(IBlockState state, World world, BlockPos pos,
                                       Block block, BlockPos fromPos, boolean inverted) {
        if (!world.isRemote) {
            boolean powered = world.isBlockPowered(pos);
            VariantActiveBlock.setBlockActive(world.provider.getDimension(), pos, inverted != powered);
        }
    }

    public static void onBlockRemoved(World world, BlockPos pos) {
        VariantActiveBlock.setBlockActive(world.provider.getDimension(), pos, false);
    }
}
