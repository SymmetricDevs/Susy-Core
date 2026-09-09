package supersymmetry.client.renderer.handler;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import supersymmetry.common.metatileentities.multiblockpart.active.MetaTileEntityActiveMuffler;
import supersymmetry.common.metatileentities.single.active.MetaTileEntityFluidActiveCasing;

public class SkinBlockAccess implements IBlockAccess {

    private final IBlockAccess wrapped;
    private final IBlockState centerHolderState;

    public SkinBlockAccess(IBlockAccess wrapped, BlockPos centerPos) {
        this.wrapped = wrapped;
        this.centerHolderState = wrapped.getBlockState(centerPos);
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return wrapped.getTileEntity(pos);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return wrapped.getCombinedLight(pos, lightValue);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        TileEntity te = wrapped.getTileEntity(pos);
        if (te instanceof MetaTileEntityHolder) {
            MetaTileEntity mte = ((MetaTileEntityHolder) te).getMetaTileEntity();
            Block neighborBlock = null;
            int neighborMeta = 0;
            if (mte instanceof MetaTileEntityActiveMuffler) {
                neighborBlock = ((MetaTileEntityActiveMuffler) mte).getStoredBlock();
                neighborMeta = ((MetaTileEntityActiveMuffler) mte).getStoredMeta();
            } else if (mte instanceof MetaTileEntityFluidActiveCasing) {
                neighborBlock = ((MetaTileEntityFluidActiveCasing) mte).getStoredBlock();
                neighborMeta = ((MetaTileEntityFluidActiveCasing) mte).getStoredMeta();
            }
            if (neighborBlock != null) {
                try {
                    return neighborBlock.getStateFromMeta(neighborMeta);
                } catch (Exception ignored) {}
            }
        }
        return centerHolderState;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return wrapped.isAirBlock(pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return wrapped.getBiome(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return wrapped.getStrongPower(pos, direction);
    }

    @Override
    public WorldType getWorldType() {
        return wrapped.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
        return wrapped.isSideSolid(pos, side, _default);
    }
}
