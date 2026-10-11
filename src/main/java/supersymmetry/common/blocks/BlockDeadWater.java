package supersymmetry.common.blocks;

import java.util.Random;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlockDeadWater extends VariantBlock<BlockDeadWater.DeadWaterBlockType> {

    public BlockDeadWater() {
        super(Material.ROCK);
        this.setHardness(0.0F);
        this.setResistance(0.0F);
        this.setSoundType(SoundType.SLIME);
        this.setTranslationKey("dead_water");
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (world.getBlockState(pos.offset(side)).getBlock() == this) {
            return false;
        }
        return super.shouldSideBeRendered(state, world, pos, side);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        if (world.isRemote) return;
        MinecraftServer server = world.getMinecraftServer();
        if (server == null) return;
        final BlockPos target = pos.toImmutable();
        server.addScheduledTask(() -> {
            if (world.isBlockLoaded(target) && world.isAirBlock(target)) {
                world.setBlockState(target, Blocks.WATER.getDefaultState(), 3);
            }
        });
    }

    public static enum DeadWaterBlockType implements IStringSerializable, IStateHarvestLevel {

        DEAD_WATER("dead_water", 0);

        private final String name;
        private final int harvestLevel;

        private DeadWaterBlockType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return this.harvestLevel;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
