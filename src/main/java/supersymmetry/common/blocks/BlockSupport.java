package supersymmetry.common.blocks;

import org.jspecify.annotations.NonNull;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.VariantBlock;

public class BlockSupport extends VariantBlock<BlockSupport.SupportType> {

    public BlockSupport() {
        super(Material.IRON);
        setTranslationKey("support");
        setBlockUnbreakable();
    }

    public void onEntityCollision(@NonNull World worldIn, @NonNull BlockPos pos, @NonNull IBlockState state,
                                  Entity entityIn) {
        entityIn.motionX = MathHelper.clamp(entityIn.motionX, -0.15, 0.15);
        entityIn.motionZ = MathHelper.clamp(entityIn.motionZ, -0.15, 0.15);
        entityIn.fallDistance = 0.0F;
        if (entityIn.motionY < -0.15) {
            entityIn.motionY = -0.15;
        }

        if (entityIn.isSneaking() && entityIn.motionY < 0.0) {
            entityIn.motionY = 0.0;
        }

        if (entityIn.collidedHorizontally) {
            entityIn.motionY = 0.3;
        }
    }

    @NonNull
    public EnumPushReaction getPushReaction(@NonNull IBlockState state) {
        return EnumPushReaction.NORMAL;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
                         int fortune) {
        // Nothing
    }
    /*
     * @Override
     * 
     * @NotNull
     * 
     * @SuppressWarnings("deprecation")
     * public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
     * {
     * return ;
     * }
     */

    @Override
    @SuppressWarnings("deprecation")
    public boolean isFullCube(@NotNull IBlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    public enum SupportType implements IStringSerializable {

        LAUNCH_PAD_TYPE("lv");

        public final String name;

        SupportType(String name) {
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
