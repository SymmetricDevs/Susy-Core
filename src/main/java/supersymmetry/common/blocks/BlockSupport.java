package supersymmetry.common.blocks;

import javax.annotation.Nonnull;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import gregtech.api.block.VariantBlock;

public class BlockSupport extends VariantBlock<BlockSupport.SupportType> {

    public static final AxisAlignedBB FULL_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)
            .grow(-0.0625D);

    public BlockSupport() {
        super(Material.IRON);
        setTranslationKey("support");
        setBlockUnbreakable();
    }

    public void onEntityCollision(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state,
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


    @Override
    public boolean canCreatureSpawn(
            @NotNull IBlockState state,
            @NotNull IBlockAccess world,
            @NotNull BlockPos pos,
            @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(@NotNull IBlockState state, @NotNull BlockRenderLayer layer) {
        if (state.getValue(VARIANT) == SupportType.SUPPORT) {
            return layer == BlockRenderLayer.CUTOUT;
        }
        return super.canRenderInLayer(state, layer);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        if (state.getValue(VARIANT) == SupportType.SUPPORT) {
            return false;
        }
        return super.isOpaqueCube(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightOpacity(@NotNull IBlockState state) {
        if (state.getValue(VARIANT) == SupportType.SUPPORT) {
            return 3; // Some random number IDK
        }
        return super.getLightOpacity(state);
    }

    public enum SupportType implements IStringSerializable {

        SUPPORT("support");

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
