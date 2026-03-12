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

    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
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
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        if (world instanceof World) {
            updateTick((World) world, pos, world.getBlockState(pos), ((World) world).rand);
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        // Occasionally just disappear
        if (rand.nextInt(50) < 1) {
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 2 | 4 | 16);
            return;
        }
        BlockPos offset;
        Block other;
        // Swap around randomly by default
        int facingOff = rand.nextInt(EnumFacing.VALUES.length);
        for (int i = 0; i < EnumFacing.VALUES.length; i++) {
            // Use the wrapping of .byIndex
            boolean isGood = false;
            BlockPos goodPos = pos;
            offset = pos.offset(EnumFacing.byIndex(facingOff + i));
            other = worldIn.getBlockState(offset).getBlock();

            while (other == Blocks.AIR || other == SuSyBlocks.BREATHING_GAS) {
                if (other == Blocks.AIR) {
                    isGood = true;
                    goodPos = offset;
                    if (rand.nextInt(6) == 0) {
                        break;
                    }
                }
                offset = offset.offset(EnumFacing.byIndex(facingOff + i));
                other = worldIn.getBlockState(offset).getBlock();
            }
            if (!isGood) {
                continue;
            }
            worldIn.setBlockState(goodPos, state, 2 | 4 | 16);
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 2 | 4 | 16);
            worldIn.scheduleUpdate(offset, SuSyBlocks.BREATHING_GAS, 200);
            return;
        }

        // Tell other blocks to get out of the way
        for (int i = 0; i < EnumFacing.VALUES.length; i++) {
            // OK so we need to tell blocks in this direction to get out of the way
            offset = pos.offset(EnumFacing.byIndex(facingOff + i));
            other = worldIn.getBlockState(offset).getBlock();

            while (other == SuSyBlocks.BREATHING_GAS) {
                worldIn.scheduleUpdate(offset, SuSyBlocks.BREATHING_GAS, 150 + i);
                offset = offset.offset(EnumFacing.byIndex(facingOff + i));
                other = worldIn.getBlockState(offset).getBlock();
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
