package supersymmetry.api.capability.impl;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import gregtech.api.util.GTTransferUtils;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityQuarry;

/**
 * Logic used for the excavation mode in
 * {@link supersymmetry.common.metatileentities.multi.electric.MetaTileEntityQuarry}
 * 
 * @author h3tR / RMI
 */
public class QuarryLogic {

    private final MetaTileEntityQuarry quarryTileEntity;

    public boolean finished;
    private int column;
    private int row;
    private int layer;
    private BlockPos layerProgression;
    private BlockPos origin;

    // prevents stackOverflows when skipping air
    private int recursionGuard = 0;

    private Random random;

    public QuarryLogic(MetaTileEntityQuarry metaTileEntity) {
        this.quarryTileEntity = metaTileEntity;
        random = new Random();
    }

    public void init() {
        this.origin = quarryTileEntity.getPos()
                .offset(quarryTileEntity.getFrontFacing().getOpposite(), 2) // inside 2
                .offset(quarryTileEntity.getFrontFacing().getOpposite().rotateY(), -getQuarryWidth() / 2)
                .add(0, -2, 0);
        Vec3i front = quarryTileEntity.getFrontFacing().getOpposite().getDirectionVec();
        Vec3i right = quarryTileEntity.getFrontFacing().getOpposite().rotateY().getDirectionVec();
        this.layerProgression = new BlockPos(front.getX() + right.getX(), 0, front.getZ() + right.getZ());
    }

    public void doQuarryOperation() {
        if (quarryTileEntity.getWorld().isRemote)
            return;

        World world = quarryTileEntity.getWorld();
        BlockPos currentPos = getCurrentPos();
        IBlockState state = world.getBlockState(currentPos);

        if (world.isAirBlock(currentPos)) {
            recursionGuard++;
            updateNextPos();
            handleAir();
            recursionGuard = 0;
            return;
        }

        // Don't break TEs or unbreakable (hardness -1) blocks
        if (!state.getBlock().hasTileEntity(state) & state.getBlock().blockHardness != -1F) {
            GTTransferUtils.addItemsToItemHandler(quarryTileEntity.getOutputInventory(), false,
                    state.getBlock().getDrops(world, currentPos, state, 0));
            world.destroyBlock(currentPos, false);
        }

        updateNextPos();
    }

    private boolean isEastWest() {
        return quarryTileEntity.getFrontFacing() == EnumFacing.EAST ||
                quarryTileEntity.getFrontFacing() == EnumFacing.WEST;
    }

    private int getQuarryWidth() {
        return quarryTileEntity.getWidth() - 4;
    }

    private int getQuarryDepth() {
        return quarryTileEntity.getDepth() - 4;
    }

    // traverse up to a layer worth of air in 1 tick
    public void handleAir() {
        if (quarryTileEntity.getWorld().isRemote || finished)
            return;

        World world = quarryTileEntity.getWorld();
        BlockPos currentPos = getCurrentPos();

        final int area = getQuarryWidth() * getQuarryDepth();
        if (world.isAirBlock(currentPos) && recursionGuard < area) {
            recursionGuard++;
            updateNextPos();
            handleAir();
        }
    }

    private void updateNextPos() {
        column++;
        final int width = isEastWest() ? getQuarryDepth() : getQuarryWidth();
        final int depth = isEastWest() ? getQuarryWidth() : getQuarryDepth();
        if (column >= width) {
            column = 0;
            row++;
            if (row >= depth) {
                row = 0;
                layer++;
                if (this.origin.getY() - layer < this.getDimensionLowestY()) {
                    finished = true;
                }
            }
        }
    }

    private BlockPos getCurrentPos() {
        return this.origin.add(new Vec3i(
                column * this.layerProgression.getX(),
                -layer,
                row * this.layerProgression.getZ()));
    }

    private int getDimensionLowestY() {
        return 0; // TODO
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setLong("progress", new BlockPos(column, layer, row).toLong());
        nbt.setLong("layerProgression", layerProgression.toLong());
        nbt.setLong("origin", origin.toLong());
        nbt.setBoolean("finished", finished);
        return nbt;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        BlockPos progress = BlockPos.fromLong(nbt.getLong("progress"));
        column = progress.getX();
        layer = progress.getY();
        row = progress.getZ();
        this.layerProgression = BlockPos.fromLong(nbt.getLong("layerProgression"));
        this.origin = BlockPos.fromLong(nbt.getLong("origin"));
        this.finished = nbt.getBoolean("finished");
    }
}
