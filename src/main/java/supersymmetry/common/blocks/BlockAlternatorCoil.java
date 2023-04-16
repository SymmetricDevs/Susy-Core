package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import gregtech.api.util.GTUtility;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import supersymmetry.api.SusyLog;

import javax.annotation.Nonnull;

public class BlockAlternatorCoil extends VariantBlock<BlockAlternatorCoil.AlternatorCoilType> {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockAlternatorCoil(){
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("alternator_coil");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        SusyLog.logger.info(this.FACING);
        setDefaultState(getState(AlternatorCoilType.COPPER).withProperty(FACING, EnumFacing.NORTH));
    }

    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
    }

    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
    }

    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
    }

    public IBlockState getStateFromMeta(int meta)
    {
        int i = meta / 4;
        int j = meta % 4 + 2;

        EnumFacing enumfacing = EnumFacing.byIndex(j);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(this.VARIANT, this.VALUES[i % this.VALUES.length]);
    }

    public int getMetaFromState(IBlockState state)
    {
        int i = ((Enum)state.getValue(this.VARIANT)).ordinal();
        int j = ((EnumFacing)state.getValue(FACING)).getIndex();
        return j - 2 + i * 4;
    }

    @Nonnull
    protected BlockStateContainer createBlockState() {
        super.createBlockState();

        return new BlockStateContainer(this, new IProperty[]{this.VARIANT, this.FACING});
    }

    public ItemStack getItemVariant(BlockTurbineRotor.BlockTurbineRotorType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal() * 4) ;
    }

    public int damageDropped(@Nonnull IBlockState state) {
        return this.getMetaFromState(state) - ((EnumFacing)state.getValue(FACING)).getIndex() + 2;
    }

    public static enum AlternatorCoilType implements IStringSerializable, IStateHarvestLevel {
        COPPER("copper", 1);

        private final String name;
        private final int harvestLevel;

        private AlternatorCoilType(String name, int harvestLevel) {
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
