package supersymmetry.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class BlockRadicalAir extends VariantBlock<BlockRadicalAir.BlockRadicalAirType> {

    public BlockRadicalAir() {
        super(Material.AIR);
        setTranslationKey("radical_air");
        setHardness(0.0F);
        setResistance(0.0F);
        setLightOpacity(0);
        setTickRandomly(true);
        registerIgnitables();
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isTranslucent(IBlockState state) {
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.TRANSLUCENT;
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        IBlockState neighborState = blockAccess.getBlockState(pos.offset(side));
        if (neighborState.getBlock() == this) {
            return false;
        }
        return true;
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isPassable(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return false;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return net.minecraft.init.Items.AIR;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return true;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 300;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 1000;
    }

    @Override
    public boolean isFireSource(World world, BlockPos pos, EnumFacing side) {
        return true;
    }

    public enum BlockRadicalAirType implements IStringSerializable, IStateHarvestLevel {
        RADICAL_AIR("radicalair");

        private final String name;
        BlockRadicalAirType(String name) { this.name = name; }

        @Override public String getName() { return name; }
        @Override public int getHarvestLevel(IBlockState state) { return 0; }
    }

    //decay
    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) return;

        if (rand.nextInt(20) == 0) {
            world.setBlockToAir(pos);
        }
    }

    //fire logic
    //moved over so it can still ignite torches if they are right next to it even after the machine is gone and the gas has not yet decayed

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        igniteNeighbors(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        igniteNeighbors(world, pos);
    }

    private void igniteNeighbors(World world, BlockPos pos) {
        if (world.isRemote) return;
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos neighborPos = pos.offset(facing);
            if (!world.isBlockLoaded(neighborPos)) continue;
            IBlockState neighborState = world.getBlockState(neighborPos);
            if (!isIgnitable(neighborState)) continue;
            world.setBlockState(neighborPos, Blocks.FIRE.getDefaultState(), 2);
        }
    }

    private static final Set<ResourceLocation> IGNITABLES      = new HashSet<>();
    private static final Set<String>           IGNITABLES_META  = new HashSet<>();

    public static void addIgnitable(ResourceLocation block) {
        IGNITABLES.add(block);
    }

    public static boolean isIgnitable(IBlockState state) {
        Block block = state.getBlock();
        ResourceLocation name = ForgeRegistries.BLOCKS.getKey(block);
        if (name == null) return false;
        if (IGNITABLES.contains(name)) return true;
        return IGNITABLES_META.contains(name.toString() + ":" + block.getMetaFromState(state));
    }

    //add shit here
    public static void registerIgnitables() {
        addIgnitable(new ResourceLocation("minecraft", "torch"));
        System.out.println("registering flammables");
    }
}
