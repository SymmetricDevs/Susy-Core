package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockCleanroomCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityCleanroom;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;
import supersymmetry.common.metatileentities.single.rocket.MetaTileEntityComponentScanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.List;

public class MetaTileEntityBuildingCleanroom extends MetaTileEntityCleanroom {
    // Unfortunately, this class has to be cursed.
    protected Field lDist_f;
    protected Field rDist_f;
    protected Field bDist_f;
    protected Field fDist_f;
    protected Field hDist_f;
    protected Field energy_cont_f;
    protected Field receivers_f;

    public MetaTileEntityComponentScanner scanner;
    public MetaTileEntityBuildingCleanroom(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        try {
            hDist_f = MetaTileEntityCleanroom.class.getDeclaredField("hDist");
            hDist_f.setAccessible(true);
            rDist_f = MetaTileEntityCleanroom.class.getDeclaredField("rDist");
            rDist_f.setAccessible(true);
            bDist_f = MetaTileEntityCleanroom.class.getDeclaredField("bDist");
            bDist_f.setAccessible(true);
            fDist_f = MetaTileEntityCleanroom.class.getDeclaredField("fDist");
            fDist_f.setAccessible(true);
            lDist_f = MetaTileEntityCleanroom.class.getDeclaredField("lDist");
            lDist_f.setAccessible(true);
            receivers_f = MetaTileEntityCleanroom.class.getDeclaredField("cleanroomReceivers");
            receivers_f.setAccessible(true);
            energy_cont_f = MetaTileEntityCleanroom.class.getDeclaredField("energyContainer");
            energy_cont_f.setAccessible(true);
        } catch (Exception e) { // this shouldn't happen UNLESS there is a bug
            return;
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBuildingCleanroom(metaTileEntityId);
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
            String[] center = Arrays.copyOf(slice, slice.length);
            if (this.frontFacing != EnumFacing.NORTH && this.frontFacing != EnumFacing.SOUTH) {
                center[0] = centerBuilder.toString();
                center[center.length - 1] = controllerBuilder.toString();
            } else {
                center[0] = centerBuilder.reverse().toString();
                center[center.length - 1] = controllerBuilder.reverse().toString();
            }

            TraceabilityPredicate wallPredicate = states(this.getCasingState(), this.getGlassState());
            TraceabilityPredicate basePredicate = this.autoAbilities()
                    .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3));
            return FactoryBlockPattern.start()
                    .aisle(wall)
                    .aisle(slice).setRepeatable(bDist_f.getInt(this) - 1)
                    .aisle(center)
                    .aisle(slice).setRepeatable(fDist_f.getInt(this)-1)
                    .aisle(wall)
                    .where('S', this.selfPredicate())
                    .where('B', states(this.getCasingState()).or(basePredicate))
                    .where('X', wallPredicate.or(basePredicate)
                            .or(doorPredicate().setMaxGlobalLimited(8))
                            .or(this.scannerPredicate().setMaxGlobalLimited(1).setMinGlobalLimited(1))
                            .or(abilities(MultiblockAbility.PASSTHROUGH_HATCH).setMaxGlobalLimited(30)))
                    .where('K', wallPredicate)
                    .where('F', this.filterPredicate())
                    .where(' ', this.innerPredicate()).build();
        } catch (Exception e) {
            return null;
        }
    }
    @Override
    public int getEnergyTier() {
        try {
            return this.energy_cont_f.get(this) == null ? 1 : Math.max(4, GTUtility.getFloorTierByVoltage(((IEnergyContainer)energy_cont_f.get(this)).getInputVoltage()));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected TraceabilityPredicate scannerPredicate() {
        return (new TraceabilityPredicate((blockWorldState -> {
            IBlockState bs = blockWorldState.getBlockState();
            TileEntity tile = blockWorldState.getTileEntity();
            if (tile instanceof MetaTileEntityHolder) {
                MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tile).getMetaTileEntity();
                if (metaTileEntity instanceof MetaTileEntityComponentScanner) {
                    ICleanroomReceiver cleanroomReceiver = (MetaTileEntityComponentScanner)metaTileEntity;
                    if (cleanroomReceiver.getCleanroom() != this) {
                        cleanroomReceiver.setCleanroom(this);
                        try {
                            ((Collection<ICleanroomReceiver>)receivers_f.get(this)).add(cleanroomReceiver);
                        } catch (Exception e) {
                            return false;
                        }
                        scanner = (MetaTileEntityComponentScanner) metaTileEntity;
                    }
                    return true;
                }
            }
            return false;
        })));
    }
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle(new String[]{"XXXXX", "XIHLX", "XXDXX", "XXXXX", "XXXXX"})
                .aisle(new String[]{"XXXXX", "X   X", "G   G", "X   X", "XFFFX"})
                .aisle(new String[]{"XXXXX", "X   X", "G   G", "X   X", "XFSFX"})
                .aisle(new String[]{"XXXXX", "X   X", "G   G", "X   X", "XFFFX"})
                .aisle(new String[]{"XMXEX", "XXOCX", "XXRXX", "XXXXX", "XXXXX"})

                .where('X', MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE))
                .where('G', MetaBlocks.TRANSPARENT_CASING.getState(gregtech.common.blocks.BlockGlassCasing.CasingType.CLEANROOM_GLASS))
                .where('S', SuSyMetaTileEntities.BUILDING_CLEANROOM, EnumFacing.SOUTH).where(' ', Blocks.AIR.getDefaultState())
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[1], EnumFacing.SOUTH)
                .where('I', MetaTileEntities.PASSTHROUGH_HATCH_ITEM, EnumFacing.NORTH)
                .where('L', MetaTileEntities.PASSTHROUGH_HATCH_FLUID, EnumFacing.NORTH)
                .where('C', SuSyMetaTileEntities.COMPONENT_SCANNER,EnumFacing.NORTH)
                .where('H', MetaTileEntities.HULL[3], EnumFacing.NORTH)
                .where('D', MetaTileEntities.DIODES[3], EnumFacing.NORTH).where('M', () -> {
                    return ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH : MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE);
                }, EnumFacing.SOUTH)
                .where('O', Blocks.IRON_DOOR.getDefaultState().withProperty(BlockDoor.FACING, EnumFacing.NORTH).withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER))
                .where('R', Blocks.IRON_DOOR.getDefaultState().withProperty(BlockDoor.FACING, EnumFacing.NORTH).withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER));
        Arrays.stream(BlockCleanroomCasing.CasingType.values()).filter((casingType) -> {
            return !casingType.equals(BlockCleanroomCasing.CasingType.PLASCRETE);
        }).forEach((casingType) -> {
            shapeInfo.add(builder.where('F', MetaBlocks.CLEANROOM_CASING.getState(casingType)).build());
        });
        return shapeInfo;
    }
    public AxisAlignedBB getInteriorBB() {
        EnumFacing front = this.getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing left = front.rotateYCCW();
        EnumFacing right = left.getOpposite();
        Vec3i down = new Vec3i(0,-1,0);
        try {
            BlockPos frontleftdown = getPos().add(multiply(front.getDirectionVec(), fDist_f.getInt(this) - 1))
                    .add(multiply(left.getDirectionVec(), lDist_f.getInt(this) - 1))
                    .add(multiply(down, hDist_f.getInt(this) - 1));
            BlockPos backrightup = getPos().add(multiply(back.getDirectionVec(), bDist_f.getInt(this) - 1))
                    .add(multiply(right.getDirectionVec(), rDist_f.getInt(this) - 1));
            AxisAlignedBB nearRet = new AxisAlignedBB(frontleftdown, backrightup);
            return new AxisAlignedBB(nearRet.minX,nearRet.minY,nearRet.minZ,
                    nearRet.maxX+1,nearRet.maxY,nearRet.maxZ+1); // here to be consistent with block analysis
        } catch (Exception e) {
             return null;
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        if (scanner != null) {
            scanner.invalidate();
        }
    }

    private Vec3i multiply(Vec3i bp, int val) {
        return new Vec3i(bp.getX()*val, bp.getY()*val, bp.getZ()*val);
    }

    public IEnergyContainer getEnergyContainer() {
        try {
            return (IEnergyContainer) energy_cont_f.get(this);
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}
