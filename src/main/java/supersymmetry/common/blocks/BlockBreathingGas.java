package supersymmetry.common.blocks;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class BlockBreathingGas extends VariantBlock<BlockBreathingGas.GasType> {
    public BlockBreathingGas() {
        // Unfortunately, Material.AIR causes the update logic to act very strangely.
        super(Material.FIRE);
        setCreativeTab(null);
        setTranslationKey("breathing_gas");
        setTickRandomly(true);
    }

    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        return MapColor.AIR;
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
            Block other = worldIn.getBlockState(offset).getBlock();
            int moved = 0;
            while (other == Blocks.AIR && rand.nextInt(4) > 0) {
                offset = offset.offset(EnumFacing.byIndex(facingOff + i));
                other = worldIn.getBlockState(offset).getBlock();
                moved++;
            }
            if (moved == 0) {
                continue;
            }
            // Step back by one, since we found an obstacle
            offset = offset.offset(EnumFacing.byIndex(facingOff + i).getOpposite());
            if (other == Blocks.AIR) {
                worldIn.setBlockState(offset, state, 2 | 4 | 16);
                worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 2 | 4 | 16);
                worldIn.scheduleUpdate(offset, SuSyBlocks.BREATHING_GAS, 10);
                return;
            } else if (other == SuSyBlocks.BREATHING_GAS) {
                worldIn.scheduleUpdate(offset, SuSyBlocks.BREATHING_GAS, 10);
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

    public enum GasType implements IStringSerializable {
        OXYGEN("oxygen"),
        PESTICIDE("pesticide");

        private final String name;

        GasType(String name) {
            this.name = name;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }
    }
}
