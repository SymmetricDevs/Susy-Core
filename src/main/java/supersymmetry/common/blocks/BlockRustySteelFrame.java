package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class BlockRustySteelFrame extends VariantBlock<BlockRustySteelFrame.RustySteelFrameVariant> {

    public static final AxisAlignedBB COLLISION_BOX = new AxisAlignedBB(0.05, 0.0, 0.05, 0.95, 1.0, 0.95);

    public BlockRustySteelFrame() {
        super(Material.IRON);
        setTranslationKey("rusty_steel_frame");
        setHardness(3.0F);
        setResistance(5.0F);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(RustySteelFrameVariant.RUSTY_STEEL_VARIANT_1));
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public @NotNull BlockFaceShape getBlockFaceShape(@NotNull IBlockAccess worldIn, @NotNull IBlockState state, @NotNull BlockPos pos, @NotNull EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    // Taken from GT frame box code
    @Override
    public void onEntityCollision(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, Entity entityIn) {
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

    public @NotNull EnumPushReaction getPushReaction(@NotNull IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    public AxisAlignedBB getCollisionBoundingBox(@NotNull IBlockState blockState, @NotNull IBlockAccess worldIn, @NotNull BlockPos pos) {
        return COLLISION_BOX;
    }

    public enum RustySteelFrameVariant implements IStringSerializable, IStateHarvestLevel {
        RUSTY_STEEL_VARIANT_1("variant_1", 1),
        RUSTY_STEEL_VARIANT_2("variant_2", 1),
        RUSTY_STEEL_VARIANT_3("variant_3", 1),
        RUSTY_STEEL_VARIANT_4("variant_4", 1),
        RUSTY_STEEL_VARIANT_5("variant_5", 1),
        RUSTY_STEEL_VARIANT_6("variant_6", 1),
        RUSTY_STEEL_VARIANT_7("variant_7", 1),
        RUSTY_STEEL_VARIANT_8("variant_8", 1);

        private final String name;
        private final int harvestLevel;

        RustySteelFrameVariant(String name, int harvestLevel) {
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
