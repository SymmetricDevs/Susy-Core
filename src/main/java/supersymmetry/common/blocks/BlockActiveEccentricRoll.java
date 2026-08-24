package supersymmetry.common.blocks;

import static gregtech.common.items.tool.rotation.CustomBlockRotations.BLOCK_DIRECTIONAL_BEHAVIOR;
import static net.minecraft.block.BlockDirectional.FACING;

import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import gregtech.api.block.VariantBlock;
import gregtech.common.items.tool.rotation.CustomBlockRotations;
import supersymmetry.api.blocks.IAnimatablePartBlock;

public class BlockActiveEccentricRoll extends VariantBlock<BlockEccentricRoll.RollType>
                                      implements IAnimatablePartBlock {

    public static final AxisAlignedBB COLLISION_BOX = new AxisAlignedBB(0.05, 0.05, 0.05, 0.95, 0.95, 0.95);

    public BlockActiveEccentricRoll() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("eccentric_roll_active");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(blockState.getBaseState().withProperty(ACTIVE, true));
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
                .withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)).withProperty(ACTIVE, true);
    }

    @NonNull @Override
    public BlockStateContainer createBlockState() {
        Class<BlockEccentricRoll.RollType> enumClass = BlockEccentricRoll.RollType.class;
        this.VARIANT = PropertyEnum.create("variant", enumClass);
        this.VALUES = enumClass.getEnumConstants();
        return new BlockStateContainer(this, VARIANT, FACING, ACTIVE);
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    @NonNull @Override
    public IBlockState getStateFromMeta(int meta) {
        // (InActive) North... -> 0...=5
        // (Active) North... -> 6...=11
        int facing = meta % 6;
        boolean active = meta > 5;

        EnumFacing enumfacing = EnumFacing.byIndex(facing);
        return getDefaultState().withProperty(VARIANT, VALUES[meta / 12]).withProperty(FACING, enumfacing)
                .withProperty(ACTIVE, active);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).ordinal() + (state.getValue(ACTIVE) ? 6 : 0);
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

    @SuppressWarnings("deprecation")
    @Nullable public AxisAlignedBB getCollisionBoundingBox(@NotNull IBlockState blockState, @NotNull IBlockAccess worldIn,
                                                 @NotNull BlockPos pos) {
        return COLLISION_BOX;
    }
}
