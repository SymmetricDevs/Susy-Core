package supersymmetry.common.blocks;

import static net.minecraft.block.BlockDirectional.FACING;

import javax.annotation.Nonnull;

import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlockPaddleShaft extends VariantBlock<BlockPaddleShaft.ShaftType> {

    public BlockPaddleShaft() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("paddle_shaft");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(ShaftType.IRON).withProperty(FACING, EnumFacing.NORTH));
    }

    @Nonnull
    @Override
    public IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos,
                                            @NotNull EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int meta, @NotNull EntityLivingBase placer) {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
                .withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        Class<ShaftType> enumClass = ShaftType.class;
        this.VARIANT = PropertyEnum.create("variant", enumClass);
        this.VALUES = enumClass.getEnumConstants();
        return new BlockStateContainer(this, VARIANT, FACING);
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.byIndex(meta);
        return getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).ordinal();
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@NotNull IBlockState state, @NotNull RayTraceResult target,
                                  @NotNull World world, @NotNull BlockPos pos,
                                  @NotNull EntityPlayer player) {
        return this.getItemVariant(state.getValue(VARIANT), 1);
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world,
                                    @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    public enum ShaftType implements IStringSerializable, IStateHarvestLevel {

        IRON("iron", 2);

        private final String name;
        private final int harvestLevel;

        ShaftType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getHarvestLevel(IBlockState state) {
            return this.harvestLevel;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }
    }
}
