package supersymmetry.common.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockStockDetector extends Block implements ITileEntityProvider {

    public static final PropertyBool POWERED = PropertyBool.create("powered");

    public BlockStockDetector() {
        super(Material.IRON);
        setHarvestLevel("pickaxe", 0);
        setHardness(2F);
        setResistance(15F);
        setDefaultState(getDefaultState().withProperty(POWERED, Boolean.valueOf(false)));
        setCreativeTab(CreativeTabs.REDSTONE);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
        // return new stock_detector_tile_entity(worldIn, meta);
    }

    @Override
    public Item getItemDropped(IBlockState p_180660_1_, Random p_180660_2_, int p_180660_3_) {
        return null;
        // return Item.getItemFromBlock(BRIBlocks.stock_detector);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        // BetterRailInterfaces.logger.warn("broke block and is remote: " + worldIn.isRemote);
        // BetterRailInterfaces.logger.warn("broke block that had powered: " + state.getValue(POWERED));

        super.breakBlock(worldIn, pos, state);

        /*
         * TileEntity tileentity = worldIn.getTileEntity(pos);
         * 
         * //worldIn.removeTileEntity(pos);
         * 
         * if (tileentity instanceof stock_detector_tile_entity)
         * {
         * super.breakBlock(worldIn, pos, state);
         * }
         */
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] { POWERED });
    }

    /*
     * @Override
     * public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
     * {
     * }
     */

    // old and unused
    public void updateBlockState(World worldIn, BlockPos pos, IBlockState state, boolean on) {
        if (worldIn.isRemote) {
            state = state.withProperty(POWERED, on);
            worldIn.setBlockState(pos, state, 3);
            worldIn.notifyNeighborsOfStateChange(pos, this, false);
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(POWERED, meta == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(POWERED) ? 1 : 0;
    }

    /*
     * @Override
     * public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
     * {
     * //BetterRailInterfaces.logger.warn("called getActualState, state had value: " + state.getValue(POWERED)); //seems
     * to always match server
     * stock_detector_tile_entity te = (stock_detector_tile_entity)world.getTileEntity(pos);
     * return state.withProperty(POWERED, te.detected);
     * }
     * 
     * @Override
     * public boolean isOpaqueCube(IBlockState state) {
     * return true;
     * }
     * 
     * @Override
     * public boolean isFullCube(IBlockState state) {
     * return true;
     * }
     */

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing facing) {
        // BetterRailInterfaces.logger.warn("weak power from state: " + state.getValue(POWERED) + ", weak power from
        // entity: " + ((stock_detector_tile_entity)world.getTileEntity(pos)).detected
        // + ", is remote: " + world.getTileEntity(pos).getWorld().isRemote
        // );
        return state.getValue(POWERED) ? 15 : 0;
        // return ((stock_detector_tile_entity)world.getTileEntity(pos)).detected ? 15 : 0;
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing facing) {
        return getWeakPower(state, world, pos, facing);
        // return ((stock_detector_tile_entity)world.getTileEntity(pos)).detected ? 15 : 0;
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }
}
