package supersymmetry.common.blocks;

import static gregtech.common.items.tool.rotation.CustomBlockRotations.BLOCK_DIRECTIONAL_BEHAVIOR;
import static net.minecraft.block.BlockDirectional.FACING;

import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import gregtech.common.items.tool.rotation.CustomBlockRotations;
import supersymmetry.api.blocks.IAnimatablePartBlock;
import supersymmetry.api.util.SuSyDamageSources;

public class BlockActiveEccentricRoll extends RedstoneActiveBlock<BlockEccentricRoll.RollType>
                                      implements IAnimatablePartBlock {

    public static final AxisAlignedBB COLLISION_BOX = new AxisAlignedBB(0.05, 0.05, 0.05, 0.95, 0.95, 0.95);

    public BlockActiveEccentricRoll() {
        this(false);
    }

    protected BlockActiveEccentricRoll(boolean inverted) {
        super(net.minecraft.block.material.Material.IRON, inverted);
        setTranslationKey("eccentric_roll_active");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(POWERED, false)
                .withProperty(VARIANT, BlockEccentricRoll.RollType.STEEL));
        CustomBlockRotations.registerCustomRotation(this, BLOCK_DIRECTIONAL_BEHAVIOR);
    }

    @Nullable @Override
    public TileEntity createTileEntity(@NotNull World world, @NotNull IBlockState state) {
        return hasTileEntity(state) ? createNewTileEntity(world, getMetaFromState(state)) : null;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @NotNull @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @NotNull @Override
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @NonNull @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            @NotNull EntityLivingBase placer) {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
                .withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer))
                .withProperty(POWERED, false);
    }

    @NonNull @Override
    protected BlockStateContainer createBlockState() {
        Class<BlockEccentricRoll.RollType> enumClass = BlockEccentricRoll.RollType.class;
        this.VARIANT = PropertyEnum.create("variant", enumClass);
        this.VALUES = enumClass.getEnumConstants();
        return new ExtendedBlockState(this,
                new IProperty[] { VARIANT, FACING, POWERED, ACTIVE_DEPRECATED },
                new IUnlistedProperty[] { gregtech.api.block.VariantActiveBlock.ACTIVE });
    }

    @NonNull @Override
    public IBlockState getStateFromMeta(int meta) {
        int facing = meta & 0x7;
        boolean powered = (meta & 0x8) != 0;
        EnumFacing enumfacing = EnumFacing.byIndex(facing);
        return getDefaultState().withProperty(FACING, enumfacing).withProperty(POWERED, powered);
    }

    @Override
    public int getMetaFromState(@NotNull IBlockState state) {
        int meta = state.getValue(FACING).ordinal();
        if (state.getValue(POWERED)) meta |= 0x8;
        return meta;
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return 0; // Only one variant: STEEL
    }

    @NonNull @Override
    public ItemStack getPickBlock(IBlockState state, @NotNull RayTraceResult target, @NotNull World world,
                                  @NotNull BlockPos pos, @NotNull EntityPlayer player) {
        return this.getItemVariant(state.getValue(VARIANT), 1);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public void onEntityCollision(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                  @NotNull Entity entityIn) {
        super.onEntityCollision(worldIn, pos, state, entityIn);
        if (isEffectActive(state)) {
            entityIn.attackEntityFrom(SuSyDamageSources.getCrusherDamage(), 2.0F);
        }
    }

    @SuppressWarnings("deprecation")
    @Nullable public AxisAlignedBB getCollisionBoundingBox(@NotNull IBlockState blockState, @NotNull IBlockAccess worldIn,
                                                 @NotNull BlockPos pos) {
        return COLLISION_BOX;
    }
}
