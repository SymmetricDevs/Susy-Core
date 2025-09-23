package supersymmetry.api.blocks;

import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
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

import gregtech.api.block.VariantBlock;
import gregtech.api.cover.CoverRayTracer;
import gregtech.common.items.tool.rotation.CustomBlockRotations;
import gregtech.common.items.tool.rotation.ICustomRotationBehavior;

public class VariantAxialRotatableBlock<T extends Enum<T> & IStringSerializable> extends VariantBlock<T> {

    public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class);

    public static final ICustomRotationBehavior BLOCK_AXIAL_BEHAVIOR = (state, world, pos, hitResult) -> {
        EnumFacing gridSide = CoverRayTracer.determineGridSideHit(hitResult);
        if (gridSide == null) return false;
        EnumFacing.Axis axis = gridSide.getAxis();
        if (axis != state.getValue(VariantAxialRotatableBlock.AXIS)) {
            state = state.withProperty(VariantAxialRotatableBlock.AXIS, axis);
            world.setBlockState(pos, state);
            return true;
        }
        return false;
    };

    public VariantAxialRotatableBlock(Material materialIn) {
        super(materialIn);
        this.setDefaultState(
                blockState.getBaseState().withProperty(VARIANT, VALUES[0]).withProperty(AXIS, EnumFacing.Axis.X));
        CustomBlockRotations.registerCustomRotation(this, BLOCK_AXIAL_BEHAVIOR);
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            @NotNull EntityLivingBase placer) {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(AXIS,
                EnumFacing.getDirectionFromEntityLiving(pos, placer).getAxis());
    }

    @Override
    public ItemStack getItemVariant(T variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal() * 3);
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        Class<T> enumClass = getActualTypeParameter(getClass(), VariantAxialRotatableBlock.class);
        this.VARIANT = PropertyEnum.create("variant", enumClass);
        this.VALUES = enumClass.getEnumConstants();
        return new BlockStateContainer(this, VARIANT, AXIS);
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal() * 3;
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        int i = meta / 3;
        int j = meta % 3;

        EnumFacing.Axis axis = EnumFacing.Axis.values()[j];
        return getDefaultState()
                .withProperty(AXIS, axis)
                .withProperty(VARIANT, VALUES[i % VALUES.length]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).ordinal() * 3 + state.getValue(AXIS).ordinal();
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(IBlockState state, @NotNull RayTraceResult target, @NotNull World world,
                                  @NotNull BlockPos pos, @NotNull EntityPlayer player) {
        return this.getItemVariant(state.getValue(VARIANT), 1);
    }

    @Override
    public boolean rotateBlock(World world, @NotNull BlockPos pos, EnumFacing axis) {
        IBlockState state = world.getBlockState(pos);
        EnumFacing.Axis currentAxis = state.getValue(AXIS);
        if (currentAxis == axis.getAxis()) {
            return false;
        }
        world.setBlockState(pos, state.withProperty(AXIS, axis.getAxis()));
        return true;
    }
}
