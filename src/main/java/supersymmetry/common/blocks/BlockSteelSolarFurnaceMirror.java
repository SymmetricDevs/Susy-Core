package supersymmetry.common.blocks;

import static net.minecraft.block.BlockDirectional.FACING;
import static supersymmetry.common.blocks.BlockEpoxySolarFurnaceMirror.MIRROR_SIDES;

import net.minecraft.block.SoundType;
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
import org.jspecify.annotations.NonNull;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntitySolarFurnace;

public class BlockSteelSolarFurnaceMirror extends VariantBlock<BlockSteelSolarFurnaceMirror.SteelMirrorType> {

    // public static final PropertyEnum<MetaTileEntitySolarFurnace.EnumMirrorSides> MIRROR_SIDES =
    // PropertyEnum.create("mirror_sides",
    // MetaTileEntitySolarFurnace.EnumMirrorSides.class);
    // 0 - top + left, 1 - top + right, 2 - bottom + left, 3 - bottom + right

    public BlockSteelSolarFurnaceMirror() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("steel_solar_furnace_mirror");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(blockState.getBaseState()
                .withProperty(MIRROR_SIDES, MetaTileEntitySolarFurnace.EnumMirrorSides.TOP_LEFT)
                .withProperty(FACING, EnumFacing.NORTH));
    }

    @NonNull @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            @NotNull EntityLivingBase placer) {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
                .withProperty(FACING, placer.getHorizontalFacing().getOpposite())
                .withProperty(MIRROR_SIDES, MetaTileEntitySolarFurnace.EnumMirrorSides.TOP_LEFT);
    }

    @NonNull @Override
    public BlockStateContainer createBlockState() {
        Class<SteelMirrorType> enumClass = SteelMirrorType.class;
        this.VARIANT = PropertyEnum.create("variant", enumClass);
        this.VALUES = enumClass.getEnumConstants();
        return new BlockStateContainer(this, VARIANT, FACING, MIRROR_SIDES);
    }

    @Override
    public int damageDropped(@NotNull IBlockState state) {
        return state.getValue(VARIANT).ordinal();
    }

    @NonNull @Override
    public IBlockState getStateFromMeta(int meta) {
        int facing = meta % 4;
        int mirrorSides = meta / 4;

        EnumFacing enumfacing = EnumFacing.byHorizontalIndex(facing);
        return getDefaultState().withProperty(VARIANT, VALUES[meta / 16]).withProperty(FACING, enumfacing)
                .withProperty(MIRROR_SIDES, MetaTileEntitySolarFurnace.EnumMirrorSides.fromInteger(mirrorSides));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (state.getValue(FACING).getHorizontalIndex() == -1) { // hacky fix for down/up facing showing up
            return state.getValue(MIRROR_SIDES).ordinal() * 4;
        }
        return state.getValue(FACING).getHorizontalIndex() + (state.getValue(MIRROR_SIDES).ordinal() * 4);
    }

    @NonNull @Override
    public ItemStack getPickBlock(IBlockState state, @NotNull RayTraceResult target, @NotNull World world,
                                  @NotNull BlockPos pos, @NotNull EntityPlayer player) {
        return this.getItemVariant(state.getValue(VARIANT), 1);
    }

    public enum SteelMirrorType implements IStringSerializable, IStateHarvestLevel {

        STEEL("steel", 1);

        private final String name;
        private final int harvestLevel;

        SteelMirrorType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @NonNull @Override
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
