package supersymmetry.common.blocks;

import static net.minecraft.block.material.Material.IRON;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import gregtech.api.cover.CoverRayTracer;
import gregtech.common.items.tool.rotation.CustomBlockRotations;
import gregtech.common.items.tool.rotation.ICustomRotationBehavior;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;

public class BlockConveyor extends VariantHorizontalRotatableBlock<BlockConveyor.ConveyorType> {

    static final AxisAlignedBB AABB_BOTTOM_HALF = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D);

    public static final ICustomRotationBehavior BLOCK_FLAT_HORIZONTAL_BEHAVIOR = new ICustomRotationBehavior() {

        @Override
        public boolean customRotate(IBlockState state, World world, BlockPos pos, RayTraceResult hitResult) {
            // Prohibit rotate other than up/down faces, as base GT does not support non-square faces
            if (hitResult.sideHit != EnumFacing.UP && hitResult.sideHit != EnumFacing.DOWN)
                return false;
            // The rest is the same with BLOCK_HORIZONTAL_BEHAVIOR
            EnumFacing gridSide = CoverRayTracer.determineGridSideHit(hitResult);
            if (gridSide == null) return false;
            if (gridSide.getAxis() == EnumFacing.Axis.Y) return false;

            if (gridSide != state.getValue(BlockHorizontal.FACING)) {
                state = state.withProperty(BlockHorizontal.FACING, gridSide);
                world.setBlockState(pos, state);
                return true;
            }
            return false;
        }

        @Override
        public boolean showGrid() {
            return false;
        }
    };

    public BlockConveyor() {
        super(IRON);
        CustomBlockRotations.registerCustomRotation(this, BLOCK_FLAT_HORIZONTAL_BEHAVIOR);
        setTranslationKey("conveyor_belt");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(ConveyorType.LV_CONVEYOR));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABB_BOTTOM_HALF;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    public enum ConveyorType implements IStringSerializable {

        LV_CONVEYOR("lv");

        public final String name;

        ConveyorType(String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }

        @NotNull
        @Override
        public String toString() {
            return getName();
        }
    }
}
