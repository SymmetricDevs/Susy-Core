package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityCleanroom;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import scala.tools.nsc.interpreter.EchoReader;
import supersymmetry.common.metatileentities.single.rocket.MetaTileEntityComponentScanner;

import java.util.Arrays;
import java.lang.reflect.*;
import java.util.Collection;

public class MetaTileEntityBuildingFakeCleanroom extends MetaTileEntityCleanroom {
    // Unfortunately, this class has to be cursed.
    protected Field lDist_f;
    protected Field rDist_f;
    protected Field bDist_f;
    protected Field fDist_f;
    protected Field hDist_f;
    protected Field receivers_f;
    public MetaTileEntityBuildingFakeCleanroom(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        try {
            hDist_f = getClass().getDeclaredField("hDist");
            rDist_f = getClass().getDeclaredField("rDist");
            bDist_f = getClass().getDeclaredField("bDist");
            fDist_f = getClass().getDeclaredField("fDist");
            lDist_f = getClass().getDeclaredField("lDist");
            receivers_f = getClass().getDeclaredField("cleanroomRecievers");
        } catch (Exception e) { // this shouldn't happen UNLESS there is a bug
            return;
        }
    }


    /**
     * Scans for blocks around the controller to update the dimensions
     */
    @Override
    public boolean updateStructureDimensions() {
        World world = getWorld();
        EnumFacing front = getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing left = front.rotateYCCW();
        EnumFacing right = left.getOpposite();

        BlockPos.MutableBlockPos lPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos fPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos bPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos hPos = new BlockPos.MutableBlockPos(getPos());

        // find the distances from the controller to the plascrete blocks on one horizontal axis and the Y axis
        // repeatable aisles take care of the second horizontal axis
        int lDist = 0;
        int rDist = 0;
        int bDist = 0;
        int fDist = 0;
        int hDist = 0;

        // find the left, right, back, and front distances for the structure pattern
        // maximum size is 15x15x15 including walls, so check 7 block radius around the controller for blocks
        for (int i = 1; i < 8; i++) {
            if (lDist == 0 && isBlockEdge(world, lPos, left)) lDist = i;
            if (rDist == 0 && isBlockEdge(world, rPos, right)) rDist = i;
            if (bDist == 0 && isBlockEdge(world, bPos, back)) bDist = i;
            if (fDist == 0 && isBlockEdge(world, fPos, front)) fDist = i;
            if (lDist != 0 && rDist != 0 && bDist != 0 && fDist != 0) break;
        }

        // height is diameter instead of radius, so it needs to be done separately
        for (int i = 1; i < 15; i++) {
            if (isBlockFloor(world, hPos, EnumFacing.DOWN)) hDist = i;
            if (hDist != 0) break;
        }

        if (lDist < MIN_RADIUS || rDist < MIN_RADIUS || bDist < MIN_RADIUS || fDist < MIN_RADIUS || hDist < MIN_DEPTH) {
            invalidateStructure();
            return false;
        }
        try {
            lDist_f.setInt(this, lDist);
            rDist_f.setInt(this, rDist);
            bDist_f.setInt(this,bDist);
            fDist_f.setInt(this,fDist);
            hDist_f.setInt(this,hDist);
        } catch (Exception e) {
            invalidateStructure();
            return false;
        }
        writeCustomData(GregtechDataCodes.UPDATE_STRUCTURE_SIZE, buf -> {
            try {
                buf.writeInt(lDist_f.getInt(this));
                buf.writeInt(rDist_f.getInt(this));
                buf.writeInt(bDist_f.getInt(this));
                buf.writeInt(fDist_f.getInt(this));
                buf.writeInt(hDist_f.getInt(this));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        return true;
    }


    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        if (this.getWorld() != null) {
            this.updateStructureDimensions();
        }
        try {
            if (lDist_f.getInt(this) < MIN_RADIUS) lDist_f.setInt(this,MIN_RADIUS);
            if (rDist_f.getInt(this) < MIN_RADIUS) rDist_f.setInt(this,MIN_RADIUS);
            if (bDist_f.getInt(this) < MIN_RADIUS) bDist_f.setInt(this,MIN_RADIUS);
            if (fDist_f.getInt(this) < MIN_RADIUS) fDist_f.setInt(this,MIN_RADIUS);
            if (hDist_f.getInt(this) < MIN_RADIUS) hDist_f.setInt(this,MIN_RADIUS);

            if (this.frontFacing == EnumFacing.EAST || this.frontFacing == EnumFacing.WEST) {
                int tmp = lDist_f.getInt(this);
                lDist_f.setInt(this, rDist_f.getInt(this));
                rDist_f.setInt(this,tmp);
            }

            StringBuilder borderBuilder = new StringBuilder();
            StringBuilder wallBuilder = new StringBuilder();
            StringBuilder insideBuilder = new StringBuilder();
            StringBuilder roofBuilder = new StringBuilder();
            StringBuilder controllerBuilder = new StringBuilder();
            StringBuilder centerBuilder = new StringBuilder();

            int i;
            for (i = 0; i < lDist_f.getInt(this); ++i) {
                borderBuilder.append("B");
                if (i == 0) {
                    wallBuilder.append("B");
                    insideBuilder.append("X");
                    roofBuilder.append("B");
                    controllerBuilder.append("B");
                    centerBuilder.append("B");
                } else {
                    insideBuilder.append(" ");
                    wallBuilder.append("X");
                    roofBuilder.append("F");
                    controllerBuilder.append("F");
                    centerBuilder.append("X");
                }
            }

            borderBuilder.append("B");
            wallBuilder.append("X");
            insideBuilder.append(" ");
            roofBuilder.append("F");
            controllerBuilder.append("S");
            centerBuilder.append("K");

            for (i = 0; i < rDist_f.getInt(this); ++i) {
                borderBuilder.append("B");
                if (i == rDist_f.getInt(this) - 1) {
                    wallBuilder.append("B");
                    insideBuilder.append("X");
                    roofBuilder.append("B");
                    controllerBuilder.append("B");
                    centerBuilder.append("B");
                } else {
                    insideBuilder.append(" ");
                    wallBuilder.append("X");
                    roofBuilder.append("F");
                    controllerBuilder.append("F");
                    centerBuilder.append("X");
                }
            }

            String[] wall = new String[hDist_f.getInt(this) + 1];
            Arrays.fill(wall, wallBuilder.toString());
            wall[0] = borderBuilder.toString();
            wall[wall.length - 1] = borderBuilder.toString();
            String[] slice = new String[hDist_f.getInt(this) + 1];
            Arrays.fill(slice, insideBuilder.toString());
            slice[0] = wallBuilder.toString();
            slice[slice.length - 1] = roofBuilder.toString();
            String[] center = (String[]) Arrays.copyOf(slice, slice.length);
            if (this.frontFacing != EnumFacing.NORTH && this.frontFacing != EnumFacing.SOUTH) {
                center[0] = centerBuilder.toString();
                center[center.length - 1] = controllerBuilder.toString();
            } else {
                center[0] = centerBuilder.reverse().toString();
                center[center.length - 1] = controllerBuilder.reverse().toString();
            }

            TraceabilityPredicate wallPredicate = states(new IBlockState[]{this.getCasingState(), this.getGlassState()});
            TraceabilityPredicate basePredicate = this.autoAbilities().or(abilities(new MultiblockAbility[]{MultiblockAbility.INPUT_ENERGY}).setMinGlobalLimited(1).setMaxGlobalLimited(3));
            return FactoryBlockPattern.start().aisle(wall).aisle(slice).setRepeatable(bDist_f.getInt(this) - 1).aisle(center).aisle(slice).setRepeatable(fDist_f.getInt(this)-1).aisle(wall).
                    where('S', this.selfPredicate()).where('B', states(new IBlockState[]{this.getCasingState()}).or(basePredicate)).where('X', wallPredicate.or(basePredicate).or(doorPredicate().setMaxGlobalLimited(8)).or(abilities(new MultiblockAbility[]{MultiblockAbility.PASSTHROUGH_HATCH}).setMaxGlobalLimited(30))
                            .or(scannerPredicate().setExactLimit(1))).
                    where('K', wallPredicate).where('F', this.filterPredicate()).where('C', this.scannerPredicate()).where(' ', this.innerPredicate()).build();
        } catch (Exception e) {
            return null;
        }
    }

    protected TraceabilityPredicate scannerPredicate() {
        return (new TraceabilityPredicate((blockWorldState -> {
            IBlockState bs = blockWorldState.getBlockState();
            TileEntity tile = blockWorldState.getTileEntity();
            if (tile instanceof MetaTileEntityHolder) {
                MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tile).getMetaTileEntity();
                if (metaTileEntity instanceof MetaTileEntityComponentScanner) {
                    ICleanroomReceiver cleanroomReceiver = (ICleanroomReceiver)metaTileEntity;
                    if (cleanroomReceiver.getCleanroom() != this) {
                        cleanroomReceiver.setCleanroom(this);
                        try {
                            ((Collection<ICleanroomReceiver>)receivers_f.get(this)).add(cleanroomReceiver);
                        } catch (Exception e) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        })));
    }
}
