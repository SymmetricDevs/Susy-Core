package supersymmetry.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockBreathingGas extends Block {
    public BlockBreathingGas() {
        super(Material.AIR, MapColor.AIR);
        setTranslationKey("breathing_gas");
        setTickRandomly(true);
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public int tickRate(World worldIn) {
        return 10;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        // Occasionally just disappear
        if (rand.nextInt(50) < 1) {
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 2 | 4 | 16);
            return;
        }
        // Otherwise, swap around randomly
        int facingOff = rand.nextInt(EnumFacing.VALUES.length);
        for (int i = 0; i < EnumFacing.VALUES.length; i++) {
            // Use the wrapping of .byIndex
            BlockPos offset = pos.offset(EnumFacing.byIndex(facingOff + i));
            IBlockState other = worldIn.getBlockState(offset);
            if (other.getBlock() != this) {
                worldIn.setBlockState(offset, getDefaultState(), 2 | 4 | 16);
                worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 2 | 4 | 16);
                return;
            }
        }
    }

    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return false;
    }


    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
    }

    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    public boolean isFullCube(IBlockState state) {
        return false;
    }

    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
}
