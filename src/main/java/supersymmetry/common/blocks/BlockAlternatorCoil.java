package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.blocks.ISuSyHorizontalOrientable;

import javax.annotation.Nonnull;

public class BlockAlternatorCoil extends VariantBlock<BlockAlternatorCoil.AlternatorCoilType> implements ISuSyHorizontalOrientable {
        
    public BlockAlternatorCoil() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("alternator_coil");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(AlternatorCoilType.COPPER).withProperty(FACING, EnumFacing.NORTH));
    }

    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int i = meta / 4;
        int j = meta % 4;

        EnumFacing enumfacing = EnumFacing.byHorizontalIndex(j);
        return this.getDefaultState()
                .withProperty(FACING, enumfacing)
                .withProperty(this.VARIANT, this.VALUES[i % this.VALUES.length]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(this.VARIANT).ordinal() * 4 + state.getValue(this.FACING).getHorizontalIndex() - 1;
    }

    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
    {
        for (AlternatorCoilType alternatorCoilType:
                AlternatorCoilType.values()) {
            items.add(this.getItemVariant(alternatorCoilType));
        }
    }
    
    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        super.createBlockState();

        return new BlockStateContainer(this, new IProperty[]{this.VARIANT, this.FACING});
    }

    @Override
    public ItemStack getItemVariant(AlternatorCoilType variant) {
        return this.getItemVariant(variant, 1);
    }

    @Override
    public ItemStack getItemVariant(@NotNull AlternatorCoilType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal() * 4);
    }

    @Nullable
    @Override
    public EnumFacing[] getValidRotations(World world, BlockPos pos) {
        return super.getValidRotations(world, pos);
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(this.VARIANT).ordinal() * 4;
    }

    public enum AlternatorCoilType implements IStringSerializable, IStateHarvestLevel {
        COPPER("copper", 1);

        private final String name;
        private final int harvestLevel;

        AlternatorCoilType(String name, int harvestLevel) {
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
