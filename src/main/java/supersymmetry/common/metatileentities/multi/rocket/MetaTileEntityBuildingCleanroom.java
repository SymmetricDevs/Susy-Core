package supersymmetry.common.metatileentities.multi.rocket;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityCleanroom;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockMetallurgy2;
import supersymmetry.common.blocks.BlockRocketMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;
import supersymmetry.common.metatileentities.multiblockpart.MetaTileEntityComponentScanner;

public class MetaTileEntityBuildingCleanroom extends MetaTileEntityCleanroom {

    public MetaTileEntityComponentScanner scanner;

    public MetaTileEntityBuildingCleanroom(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBuildingCleanroom(metaTileEntityId);
    }

    /** Scans for blocks around the controller to update the dimensions */
    @Override
    public boolean updateStructureDimensions() {
        World world = getWorld();
        if (world.isRemote) {
            return true;
        }
        EnumFacing front = getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing left = front.rotateYCCW();
        EnumFacing right = left.getOpposite();

        BlockPos.MutableBlockPos lPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos fPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos bPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos hPos = new BlockPos.MutableBlockPos(getPos());

        // find the distances from the controller to the plascrete blocks on one
        // horizontal axis and the
        // Y axis
        // repeatable aisles take care of the second horizontal axis
        int lDist = 0;
        int rDist = 0;
        int bDist = 0;
        int fDist = 0;
        int hDist = 0;

        // find the left, right, back, and front distances for the structure pattern
        // maximum size is 15x15x15 including walls, so check 7 block radius around the
        // controller for
        // blocks
        for (int i = 1; i < 16; i++) { // trying for 31x31
            if (lDist == 0 && isBlockEdge(world, lPos, left))
                lDist = i;
            if (rDist == 0 && isBlockEdge(world, rPos, right))
                rDist = i;
            if (bDist == 0 && isBlockEdge(world, bPos, back))
                bDist = i;
            if (fDist == 0 && isBlockEdge(world, fPos, front))
                fDist = i;
            if (lDist != 0 && rDist != 0 && bDist != 0 && fDist != 0)
                break;
        }

        // height is diameter instead of radius, so it needs to be done separately
        for (int i = 1; i < 15; i++) {
            if (isBlockFloor(world, hPos, EnumFacing.DOWN))
                hDist = i;
            if (hDist != 0)
                break;
        }

        if (lDist < MIN_RADIUS || rDist < MIN_RADIUS || bDist < MIN_RADIUS || fDist < MIN_RADIUS || hDist < MIN_DEPTH) {
            invalidateStructure();
            return false;
        }

        this.lDist = lDist;
        this.rDist = rDist;
        this.bDist = bDist;
        this.fDist = fDist;
        this.hDist = hDist;

        writeCustomData(GregtechDataCodes.UPDATE_STRUCTURE_SIZE, buf -> {
            buf.writeInt(this.lDist);
            buf.writeInt(this.rDist);
            buf.writeInt(this.bDist);
            buf.writeInt(this.fDist);
            buf.writeInt(this.hDist);
        });
        return true;
    }

    /**
     * @param world
     *                  the world to check
     * @param pos
     *                  the pos to check and move
     * @param direction
     *                  the direction to move
     * @return if a block is a valid wall block at pos moved in direction
     */
    public boolean isBlockEdge(@NotNull World world, @NotNull BlockPos.MutableBlockPos pos,
                               @NotNull EnumFacing direction) {
        return world.getBlockState(pos.move(direction)) == SuSyBlocks.ROCKET_MULTIBLOCK_CASING
                .getState(BlockRocketMultiblockCasing.CasingType.AEROSPACE_GASKET);
    }

    /**
     * @param world
     *                  the world to check
     * @param pos
     *                  the pos to check and move
     * @param direction
     *                  the direction to move
     * @return if a block is a valid floor block at pos moved in direction
     */
    public boolean isBlockFloor(@NotNull World world, @NotNull BlockPos.MutableBlockPos pos,
                                @NotNull EnumFacing direction) {
        return isBlockEdge(world, pos, direction) || world.getBlockState(pos) == SuSyBlocks.ROCKET_MULTIBLOCK_CASING
                .getState(BlockRocketMultiblockCasing.CasingType.VINYL_COMPOSITE_FLOORING);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        if (this.getWorld() != null) {
            this.updateStructureDimensions();
        }
        this.lDist = Math.max(lDist, MIN_RADIUS);
        this.rDist = Math.max(rDist, MIN_RADIUS);
        this.bDist = Math.max(bDist, MIN_RADIUS);
        this.fDist = Math.max(fDist, MIN_RADIUS);
        this.hDist = Math.max(hDist, MIN_RADIUS);
        if (this.frontFacing == EnumFacing.EAST || this.frontFacing == EnumFacing.WEST) {
            int tmp = lDist;
            this.lDist = rDist;
            this.rDist = tmp;
        }

        StringBuilder borderBuilder = new StringBuilder();
        StringBuilder floorBuilder = new StringBuilder();
        StringBuilder wallBuilder = new StringBuilder();
        StringBuilder insideBuilder = new StringBuilder();
        StringBuilder roofBuilder = new StringBuilder();
        StringBuilder controllerBuilder = new StringBuilder();

        int i;
        for (i = 0; i < lDist; ++i) {
            borderBuilder.append("B"); // Aerospace gasket edges
            if (i == 0) {
                floorBuilder.append("B"); // Aerospace gasket at floor edges
                wallBuilder.append("B"); // Aerospace gasket edge
                insideBuilder.append("W"); // Left wall (EAST/WEST facing)
                roofBuilder.append("B"); // Aerospace gasket edge
                controllerBuilder.append("B"); // Aerospace gasket edge
            } else {
                floorBuilder.append("L"); // Vinyl composite flooring
                insideBuilder.append(" "); // Air
                wallBuilder.append("Y"); // Back/front wall (NORTH/SOUTH facing)
                roofBuilder.append("F"); // Ceiling tiles/filters
                controllerBuilder.append("F"); // Ceiling tiles/filters
            }
        }

        borderBuilder.append("B"); // Aerospace gasket edge
        floorBuilder.append("L"); // Aerospace gasket at floor edge
        wallBuilder.append("Y"); // Back/front wall center (NORTH/SOUTH facing)
        insideBuilder.append(" "); // Air
        roofBuilder.append("F"); // Ceiling
        controllerBuilder.append("S"); // Controller

        for (i = 0; i < rDist; ++i) {
            borderBuilder.append("B"); // Aerospace gasket edge
            if (i == rDist - 1) {
                floorBuilder.append("B"); // Aerospace gasket at floor edge
                wallBuilder.append("B"); // Aerospace gasket edge
                insideBuilder.append("W"); // Right wall (EAST/WEST facing)
                roofBuilder.append("B"); // Aerospace gasket edge
                controllerBuilder.append("B"); // Aerospace gasket edge
            } else {
                floorBuilder.append("L"); // Vinyl composite flooring
                insideBuilder.append(" "); // Air
                wallBuilder.append("Y"); // Back/front wall (NORTH/SOUTH facing)
                roofBuilder.append("F"); // Ceiling tiles/filters
                controllerBuilder.append("F"); // Ceiling tiles/filters
            }
        }

        String[] wall = new String[hDist + 1];
        Arrays.fill(wall, wallBuilder.toString());
        wall[0] = borderBuilder.toString(); // The bottom row of the wall is an edge
        wall[wall.length - 1] = borderBuilder.toString(); // Top edges
        String[] slice = new String[hDist + 1];
        Arrays.fill(slice, insideBuilder.toString());
        slice[0] = floorBuilder.toString(); // Floor with edges
        slice[slice.length - 1] = roofBuilder.toString(); // Ceiling
        String[] center = Arrays.copyOf(slice, slice.length);
        if (this.frontFacing != EnumFacing.NORTH && this.frontFacing != EnumFacing.SOUTH) {
            center[0] = floorBuilder.toString(); // Floor with edges
            center[center.length - 1] = controllerBuilder.toString(); // Ceiling with controller
        } else {
            center[0] = floorBuilder.reverse().toString(); // Floor with edges (reversed)
            center[center.length - 1] = controllerBuilder.reverse().toString(); // Ceiling with controller
            // (reversed)
        }

        // Count total ceiling positions for 5% calculation
        int totalCeilingPositions = (bDist + fDist - 1) *
                (lDist + rDist - 1) - 1;
        int minFilters = Math.max(1, (int) Math.ceil(totalCeilingPositions * 0.05));

        TraceabilityPredicate basePredicate = this.autoAbilities()
                .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3));

        // Glass wall predicate (no facing constraint)
        TraceabilityPredicate glassPredicate = states(this.getGlassState());

        // Polystyrene wall with NORTH/SOUTH facing (for left/right walls perpendicular
        // to X-axis)
        TraceabilityPredicate polystyreneNSPredicate = new TraceabilityPredicate((blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            if (state.getBlock() != SuSyBlocks.METALLURGY_2)
                return false;
            EnumFacing facing = state.getValue(VariantHorizontalRotatableBlock.FACING);

            return SuSyBlocks.METALLURGY_2.getState(state) ==
                    BlockMetallurgy2.BlockMetallurgy2Type.POLYSTYRENE_WALL &&
                    (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH);
        }));

        // Polystyrene wall with EAST/WEST facing (for front/back walls perpendicular to
        // Z-axis)
        TraceabilityPredicate polystyreneEWPredicate = new TraceabilityPredicate((blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            if (state.getBlock() != SuSyBlocks.METALLURGY_2)
                return false;
            EnumFacing facing = state.getValue(VariantHorizontalRotatableBlock.FACING);

            return SuSyBlocks.METALLURGY_2.getState(state) ==
                    BlockMetallurgy2.BlockMetallurgy2Type.POLYSTYRENE_WALL &&
                    (facing == EnumFacing.EAST || facing == EnumFacing.WEST);
        }));

        // Scanner predicate to catch both sides
        TraceabilityPredicate scannerPredicate = this.scannerPredicate().setMaxGlobalLimited(1)
                .setMinGlobalLimited(1);

        return FactoryBlockPattern.start().aisle(wall).aisle(slice).setRepeatable(bDist - 1)
                .aisle(center).aisle(slice).setRepeatable(fDist - 1).aisle(wall)
                .where('S', this.selfPredicate())
                .where('B',
                        states(SuSyBlocks.ROCKET_MULTIBLOCK_CASING
                                .getState(BlockRocketMultiblockCasing.CasingType.AEROSPACE_GASKET))
                                .or(basePredicate))
                .where('L',
                        states(SuSyBlocks.ROCKET_MULTIBLOCK_CASING
                                .getState(BlockRocketMultiblockCasing.CasingType.VINYL_COMPOSITE_FLOORING)))
                .where('W',
                        glassPredicate.or(polystyreneEWPredicate).or(basePredicate)
                                .or(doorPredicate().setMaxGlobalLimited(8)).or(scannerPredicate)
                                .or(abilities(MultiblockAbility.PASSTHROUGH_HATCH).setMaxGlobalLimited(30)))
                .where('Y',
                        glassPredicate.or(polystyreneNSPredicate).or(basePredicate)
                                .or(doorPredicate().setMaxGlobalLimited(8)).or(scannerPredicate)
                                .or(abilities(MultiblockAbility.PASSTHROUGH_HATCH).setMaxGlobalLimited(30)))
                .where('F',
                        states(SuSyBlocks.ROCKET_MULTIBLOCK_CASING
                                .getState(BlockRocketMultiblockCasing.CasingType.VINYL_CEILING_TILE))
                                .or(states(SuSyBlocks.ROCKET_MULTIBLOCK_CASING
                                        .getState(
                                                BlockRocketMultiblockCasing.CasingType.CEILING_GRID_FILTER_UNIT))
                                        .setMinGlobalLimited(minFilters)))
                .where(' ', this.innerPredicate()).build();
    }

    @Override
    public int getEnergyTier() {
        return this.energyContainer == null ? 1 :
                Math.max(4, GTUtility.getFloorTierByVoltage(this.energyContainer.getInputVoltage()));
    }

    protected TraceabilityPredicate scannerPredicate() {
        return (new TraceabilityPredicate((blockWorldState -> {
            IBlockState bs = blockWorldState.getBlockState();
            TileEntity tile = blockWorldState.getTileEntity();
            if (tile instanceof MetaTileEntityHolder) {
                MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tile).getMetaTileEntity();
                if (metaTileEntity instanceof MetaTileEntityComponentScanner thisScanner) {
                    Set<IMultiblockPart> partsFound = blockWorldState.getMatchContext().getOrCreate("MultiblockParts",
                            HashSet::new);
                    partsFound.add((IMultiblockPart) metaTileEntity);
                    if (thisScanner.getCleanroom() != this) {
                        thisScanner.setCleanroom(this);
                        this.cleanroomReceivers.add(thisScanner);
                    }
                    return true;
                }
            }
            return false;
        })));
    }

    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("BBBBB", "BILOB", "BEHDB", "BYCYB", "BBBBB")
                .aisle("BJJJB", "W   X", "W   X", "W   X", "BFGFB")
                .aisle("BJJJB", "W   X", "W   X", "W   X", "BGSGB")
                .aisle("BJJJB", "W   X", "W   X", "W   X", "BFGFB")
                .aisle(new String[] { "BBMBB", "BZPZB", "BZRZB", "BZZZB", "BBBBB" })
                // Edges/borders
                .where('B',
                        SuSyBlocks.ROCKET_MULTIBLOCK_CASING
                                .getState(BlockRocketMultiblockCasing.CasingType.AEROSPACE_GASKET))
                // Floor
                .where('J',
                        SuSyBlocks.ROCKET_MULTIBLOCK_CASING
                                .getState(BlockRocketMultiblockCasing.CasingType.VINYL_COMPOSITE_FLOORING))
                // Ceiling tiles
                .where('F',
                        SuSyBlocks.ROCKET_MULTIBLOCK_CASING
                                .getState(BlockRocketMultiblockCasing.CasingType.VINYL_CEILING_TILE))
                // Ceiling grid filter (satisfies 5% requirement)
                .where('G',
                        SuSyBlocks.ROCKET_MULTIBLOCK_CASING
                                .getState(BlockRocketMultiblockCasing.CasingType.CEILING_GRID_FILTER_UNIT))
                // Polystyrene walls - left/right (EAST/WEST facing)
                .where('W',
                        SuSyBlocks.METALLURGY_2.getState(BlockMetallurgy2.BlockMetallurgy2Type.POLYSTYRENE_WALL)
                                .withProperty(VariantHorizontalRotatableBlock.FACING, EnumFacing.EAST))
                .where('X',
                        SuSyBlocks.METALLURGY_2.getState(BlockMetallurgy2.BlockMetallurgy2Type.POLYSTYRENE_WALL)
                                .withProperty(VariantHorizontalRotatableBlock.FACING, EnumFacing.WEST))
                // Polystyrene walls - back/front (NORTH/SOUTH facing)
                .where('Y',
                        SuSyBlocks.METALLURGY_2.getState(BlockMetallurgy2.BlockMetallurgy2Type.POLYSTYRENE_WALL)
                                .withProperty(VariantHorizontalRotatableBlock.FACING, EnumFacing.NORTH))
                .where('Z',
                        SuSyBlocks.METALLURGY_2.getState(BlockMetallurgy2.BlockMetallurgy2Type.POLYSTYRENE_WALL)
                                .withProperty(VariantHorizontalRotatableBlock.FACING, EnumFacing.SOUTH))
                // Controller
                .where('S', SuSyMetaTileEntities.BUILDING_CLEANROOM, EnumFacing.SOUTH)
                // Air
                .where(' ', Blocks.AIR.getDefaultState())
                // Energy hatch
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[1], EnumFacing.SOUTH)
                // Passthrough hatches
                .where('I', MetaTileEntities.PASSTHROUGH_HATCH_ITEM, EnumFacing.NORTH)
                .where('L', MetaTileEntities.PASSTHROUGH_HATCH_FLUID, EnumFacing.NORTH)
                .where('O', MetaTileEntities.PASSTHROUGH_HATCH_ITEM, EnumFacing.NORTH)
                // Component scanner
                .where('C', SuSyMetaTileEntities.COMPONENT_SCANNER, EnumFacing.NORTH)
                // Hull
                .where('H', MetaTileEntities.HULL[3], EnumFacing.NORTH)
                // Diode
                .where('D', MetaTileEntities.DIODES[3], EnumFacing.NORTH)
                // Maintenance hatch
                .where('M', () -> {
                    return ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH :
                            SuSyBlocks.ROCKET_MULTIBLOCK_CASING
                                    .getState(BlockRocketMultiblockCasing.CasingType.AEROSPACE_GASKET);
                }, EnumFacing.SOUTH)
                // Door (lower half)
                .where('P',
                        Blocks.IRON_DOOR.getDefaultState().withProperty(BlockDoor.FACING, EnumFacing.NORTH)
                                .withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER))
                .where('R', Blocks.IRON_DOOR.getDefaultState().withProperty(BlockDoor.FACING, EnumFacing.NORTH)
                        .withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER));

        // Build the shape
        shapeInfo.add(builder.build());
        return shapeInfo;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return SusyTextures.AEROSPACE_GASKET;
    }

    public AxisAlignedBB getInteriorBB() {
        updateStructureDimensions();
        EnumFacing front = this.getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing left = front.rotateYCCW();
        EnumFacing right = left.getOpposite();
        Vec3i down = new Vec3i(0, -1, 0);
        BlockPos frontleftdown = getPos().add(multiply(front.getDirectionVec(), fDist - 1))
                .add(multiply(left.getDirectionVec(), lDist - 1))
                .add(multiply(down, hDist - 1));
        BlockPos backrightup = getPos().add(multiply(back.getDirectionVec(), bDist - 1))
                .add(multiply(right.getDirectionVec(), rDist - 1));
        AxisAlignedBB nearRet = new AxisAlignedBB(frontleftdown, backrightup);
        return new AxisAlignedBB(nearRet.minX, nearRet.minY, nearRet.minZ, nearRet.maxX + 1, nearRet.maxY,
                nearRet.maxZ + 1); // here to be consistent with block analysis
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        if (scanner != null) {
            scanner.invalidate();
        }
    }

    private Vec3i multiply(Vec3i bp, int val) {
        return new Vec3i(bp.getX() * val, bp.getY() * val, bp.getZ() * val);
    }

    public IEnergyContainer getEnergyContainer() {
        return this.energyContainer;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("susy.machine.building_cleanroom.tooltip.1"));
        tooltip.add(I18n.format("susy.machine.building_cleanroom.tooltip.2"));
    }
}
