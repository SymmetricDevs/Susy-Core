package supersymmetry.api.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.RayTracer;
import gregtech.api.cover.CoverRayTracer;
import gregtech.api.metatileentity.MetaTileEntity;
import supersymmetry.client.renderer.handler.VariantCoverableBlockRenderer;
import supersymmetry.common.tileentities.TileEntityCoverable;

public class VariantDirectionalCoverableBlock<T extends Enum<T> & IStringSerializable> extends
                                             VariantDirectionalRotatableBlock<T> implements ITileEntityProvider {

    public VariantDirectionalCoverableBlock(Material materialIn) {
        super(materialIn);
        // this.setDefaultState(blockState.getBaseState().withProperty(VARIANT, VALUES[0]).withProperty(FACING,
        // EnumFacing.SOUTH));
        // CustomBlockRotations.registerCustomRotation(this, BLOCK_DIRECTIONAL_BEHAVIOR);
    }

    public Predicate<ItemStack> validCover;

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        Class<T> enumClass = getActualTypeParameter(getClass(), VariantDirectionalCoverableBlock.class);
        this.VARIANT = PropertyEnum.create("variant", enumClass);
        this.VALUES = enumClass.getEnumConstants();
        return new BlockStateContainer(this, VARIANT, FACING);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        // worldIn.getTileEntity(pos).invalidate();
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
                         int fortune) {
        if (world.getTileEntity(pos) instanceof TileEntityCoverable te) {
            ItemStack newStack = te.getCoverItem();
            newStack.setCount(te.getCoverCount());
            drops.add(newStack);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                    @NotNull EntityPlayer playerIn,
                                    @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY,
                                    float hitZ) {
        if ((validCover.test(playerIn.getHeldItem(hand)) || playerIn.getHeldItem(hand).isEmpty()) &&
                worldIn.getTileEntity(pos) instanceof TileEntityCoverable te) {
            CuboidRayTraceResult rayTraceResult = (CuboidRayTraceResult) RayTracer.retraceBlock(worldIn, playerIn, pos);
            ItemStack itemStack = playerIn.getHeldItem(hand);
            if (rayTraceResult == null) {
                return false;
            }
            EnumFacing gridSideHit = CoverRayTracer.determineGridSideHit(rayTraceResult);
            ItemStack out = te.placeCover(gridSideHit, playerIn.getHeldItem(hand), playerIn);
            playerIn.setHeldItem(hand, out);
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        return TileEntityCoverable.RENDER_SWITCH || !((TileEntityCoverable) world.getTileEntity(pos)).isCovered(face);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        EnumBlockRenderType ret = TileEntityCoverable.RENDER_SWITCH ? VariantCoverableBlockRenderer.BLOCK_RENDER_TYPE :
                EnumBlockRenderType.MODEL;
        return ret;
    }

    @Nullable
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityCoverable();
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public RayTraceResult collisionRayTrace(@NotNull IBlockState blockState, @NotNull World worldIn,
                                            @NotNull BlockPos pos, @NotNull Vec3d start, @NotNull Vec3d end) {
        List<IndexedCuboid6> collisionList = new ArrayList<>();
        collisionList.add(MetaTileEntity.FULL_CUBE_COLLISION);
        return RayTracer.rayTraceCuboidsClosest(start, end, pos, collisionList);
    }
}
