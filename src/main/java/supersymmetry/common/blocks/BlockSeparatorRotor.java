package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import supersymmetry.api.blocks.ISuSyHorizontalOrientable;

import javax.annotation.Nonnull;

public class BlockSeparatorRotor extends VariantBlock<BlockSeparatorRotor.BlockSeparatorRotorType> implements ISuSyHorizontalOrientable {
    public BlockSeparatorRotor() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("separator_rotor");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(BlockSeparatorRotorType.STEEL).withProperty(FACING, EnumFacing.NORTH));
    }
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int i = meta / 4;
        int j = meta % 4 + 2;

        EnumFacing enumfacing = EnumFacing.byHorizontalIndex(j);
        return this.getDefaultState()
                .withProperty(FACING, enumfacing)
                .withProperty(this.VARIANT, this.VALUES[i % this.VALUES.length]);
    }

    // Ignore facing
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(this.VARIANT).ordinal() * 4;
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        super.createBlockState();

        return new BlockStateContainer(this, this.VARIANT, this.FACING);
    }

    public ItemStack getItemVariant(BlockSeparatorRotor.BlockSeparatorRotorType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal() * 4);
    }

    public int damageDropped(@Nonnull IBlockState state) {
        return this.getMetaFromState(state) -  state.getValue(FACING).getIndex() + 2;
    }

    public enum BlockSeparatorRotorType implements IStringSerializable, IStateHarvestLevel {
        STEEL("steel", 2);

        private final String name;
        private final int harvestLevel;

        BlockSeparatorRotorType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        public String getName() {
            return this.name;
        }

        public int getHarvestLevel(IBlockState state) {
            return this.harvestLevel;
        }

        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }
    }
}
