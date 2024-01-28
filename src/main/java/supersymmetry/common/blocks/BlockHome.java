package supersymmetry.common.blocks;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockHome extends VariantBlock<BlockHome.HomeType> {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockHome() {
        super(Material.IRON);
        this.setTranslationKey("home_block");
        this.setHardness(0.5f);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("pickaxe", 1);
        this.setDefaultState(getState(HomeType.HOME_PRIMITIVE));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean isBed(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable Entity player) {
        return true;
    }

    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
    }

    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
    }

    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
    }

    public IBlockState getStateFromMeta(int meta) {
        int i = meta / 4;
        int j = meta % 4 + 2;

        EnumFacing enumfacing = EnumFacing.byIndex(j);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(this.VARIANT, this.VALUES[i % this.VALUES.length]);
    }

    public int getMetaFromState(IBlockState state) {
        int i = ((Enum)state.getValue(this.VARIANT)).ordinal();
        int j = ((EnumFacing)state.getValue(FACING)).getIndex();
        return j - 2 + i * 4;
    }

    @Nonnull
    protected BlockStateContainer createBlockState() {
        super.createBlockState();

        return new BlockStateContainer(this, new IProperty[]{this.VARIANT, this.FACING});
    }

    public ItemStack getItemVariant(BlockHome.HomeType variant, int amount) {
        return new ItemStack(this, amount, variant.ordinal() * 4) ;
    }

    public int damageDropped(@Nonnull IBlockState state) {
        return this.getMetaFromState(state) - ((EnumFacing)state.getValue(FACING)).getIndex() + 2;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if ((worldIn.provider.canRespawnHere() && worldIn.getBiome(pos) != net.minecraft.init.Biomes.HELL) && !worldIn.isRemote) {
            playerIn.sendStatusMessage(new TextComponentTranslation("tile.home.allowed"), true);
            net.minecraftforge.event.ForgeEventFactory.onPlayerSpawnSet(playerIn, pos, true);
            playerIn.bedLocation = pos;
            playerIn.setSpawnPoint(playerIn.bedLocation, false);
        } else {
            playerIn.sendStatusMessage(new TextComponentTranslation("tile.home.denied"), true);
        }
        return false;
    }

    public enum HomeType implements IStringSerializable {
        HOME_PRIMITIVE("home_primitive"),
        HOME_GT_BRUTALIST("home_gt_brutalist"),
        HOME_RENEWAL_BRUTALIST("home_renewal_brutalist"),
        HOME_SCIFI("home_scifi");

        public final String name;

        HomeType(String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.getName();
        }
    }
}
