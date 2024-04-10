package supersymmetry.api.blocks;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class VariantRotatableBlock<T extends Enum<T> & IStringSerializable> extends VariantBlock<T> {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public VariantRotatableBlock(Material materialIn) {
        super(materialIn);
        this.setDefaultState(this.blockState.getBaseState().withProperty(this.VARIANT, this.VALUES[0]).withProperty(FACING, EnumFacing.NORTH));
    }

    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    public ItemStack getItemVariant(T variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal() * 4);
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        Class<T> enumClass = getActualTypeParameter(this.getClass(), VariantRotatableBlock.class);
        this.VARIANT = PropertyEnum.create("variant", enumClass);
        this.VALUES = enumClass.getEnumConstants();
        return new BlockStateContainer(this, this.VARIANT, this.FACING);
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(this.VARIANT).ordinal() * 4;
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
        return state.getValue(this.VARIANT).ordinal() * 4 + state.getValue(this.FACING).getHorizontalIndex();
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return this.getItemVariant(state.getValue(this.VARIANT), 1);
    }
}
