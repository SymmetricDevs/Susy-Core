package supersymmetry.common.metatileentities.single.electric;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.PatternMatchContext;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityBuildingCleanroom;
import supersymmetry.SuSyValues;

import java.util.*;
import java.util.stream.Collectors;

public class MetaTileEntityComponentScanner extends MetaTileEntity implements ICleanroomReceiver {
    private MetaTileEntityBuildingCleanroom linkedCleanroom;
    public enum BuildError {
        SUCCESS,
        DISCONNECTED,
        EMPTY,
        HULL_WEAK,
        HULL_FULL,
        INVALID_AIRLIKE
    }

    public BuildError status;
    public MetaTileEntityComponentScanner(ResourceLocation mteId) {
        super(mteId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityComponentScanner(this.metaTileEntityId);
    }

    public void scanPart() {
        if (!linkedCleanroom.isClean()) return;
        AxisAlignedBB interior = linkedCleanroom.getInteriorBB();
        int solidBlocks = 0;
        ArrayList<BlockPos> blocks = getBlocks(getWorld(), interior);

        if (blocks.size() == 0) {
            this.status = BuildError.EMPTY;
            return;
        }

        Set<BlockPos> blocksConnected = getBlockConn(getWorld(), interior, blocks.get(0));

        if (blocksConnected.size() != blocks.size()) {
            this.status = BuildError.DISCONNECTED;
        }

        // Block-by-block analysis
        for (BlockPos bp: blocks) {
            IBlockState state = getWorld().getBlockState(bp);
        }

        Set<BlockPos> exterior = checkHull(getWorld(), interior, blocksConnected, false);

        boolean hasAir = status != BuildError.HULL_FULL;

        // Identify component type: Fluid port -> tank, hatch -> pf

        // Component analysis

        /* Plan from here on out:
        1. Gather block statistics
        2. Check for unallowed TileEntities (we can't have as many if it's all being modelized)
        3. Identify component purpose:
            a. Payload fairing
                - Distinguishable by material type
                - Attachments only at bottom
                - Holds port
                - Bottom opening is not filled
                - No through-holes:
                    - All partial holes are counted (if two blocks have a midpoint not in a block, there is a partial hole)
                    - All air blocks in the hole are counted
                    - There is no more than one contiguous set of air blocks not inside the hole that has access to the hole!
            b. Life compartment
                - Contains interior space
                - Contains life support TEs
                - Allows for containers
            c. Fuel tank
                - Contains interior space
                - Contains structural blocks
                - Has a port
                - Contains exterior blocks
            d. Engine
                - Specialized blocks for ignition containment
            e. Hull cover
                - Connection blocks (skirts)
                - Particular surface blocks
                - Support blocks
            f. Control room
                - Port
                - Guidance computer (not a tile entity)
                - Seat
        */
    }

    private ArrayList<BlockPos> getBlocks(World world, AxisAlignedBB faaBB) {
        AxisAlignedBB aaBB = new AxisAlignedBB(Math.round(faaBB.minX),Math.round(faaBB.maxX),
                Math.round(faaBB.minY),Math.round(faaBB.maxY),
                Math.round(faaBB.minZ),Math.round(faaBB.maxZ));
        ArrayList<BlockPos> ret = new ArrayList<>();
        for (int x = (int)aaBB.minX; x < aaBB.maxX; x++) {
            for (int y = (int)aaBB.minY; y < aaBB.maxY; y++) {
                for (int z = (int)aaBB.minZ; z < aaBB.maxZ; z++) {
                    BlockPos bp = new BlockPos(x,y,z);
                    if (!world.isAirBlock(bp)) {
                        if (world.getBlockState(bp).getCollisionBoundingBox(world, bp) == null) {
                            status = BuildError.INVALID_AIRLIKE;
                        }
                        ret.add(bp);
                    }
                }
            }
        }
        return ret;
    }

    private Set<BlockPos> getBlockConn(World world, AxisAlignedBB aaBB, BlockPos beg) {
        if (!aaBB.contains(new Vec3d(beg))) {
            return new HashSet<BlockPos>(); //wtf moment
        }
        Set<BlockPos> blocksCollected = new HashSet<BlockPos>();
        blocksCollected.add(beg);
        Queue<BlockPos> uncheckedBlocks = new ArrayDeque<>(Arrays.asList(beg));


        while (!uncheckedBlocks.isEmpty()) {
            BlockPos bp = uncheckedBlocks.remove();
            blocksCollected.add(bp);
            uncheckedBlocks.addAll(getBlockNeighbors(world, aaBB, bp).stream()
                    .filter(p -> !world.isAirBlock(p) && !blocksCollected.contains(p) && !uncheckedBlocks.contains(p)).collect(Collectors.toSet()));
        }
        return blocksCollected;
    }

    private Set<BlockPos> checkHull(World world, AxisAlignedBB aaBB, Set<BlockPos> actualBlocks, boolean testStrength) {
        AxisAlignedBB floodBB = aaBB.grow(1);// initializes flood fill box
        BlockPos bottom = new BlockPos(floodBB.minX, floodBB.minY, floodBB.minZ); // initializes flood fill start
        Queue<BlockPos> uncheckedBlocks = new ArrayDeque<>();
        Set<BlockPos> airBlocks = new HashSet<>();
        Set<BlockPos> hullBlocks = new HashSet<>();
        PatternMatchContext pmc = new PatternMatchContext();
        uncheckedBlocks.add(bottom);
        for (BlockPos pos; !uncheckedBlocks.isEmpty();) {
            pos = uncheckedBlocks.remove();
            if (actualBlocks.contains(pos)) {
                BlockWorldState bws = new BlockWorldState(); // this is awful but I guess it works?
                bws.update(world, pos,pmc,null,null,SuSyValues.rocketHullBlocks);
                if (testStrength && !SuSyValues.rocketHullBlocks.test(bws)) {
                    status = BuildError.HULL_WEAK;
                    return null;
                }
                hullBlocks.add(pos);
            } else {
                airBlocks.add(pos);
                uncheckedBlocks.addAll(getBlockOrthNeighbors(world, floodBB, pos).stream().filter(
                                p -> actualContains(floodBB, p) && !(airBlocks.contains(p) || uncheckedBlocks.contains(p)))
                        .collect(Collectors.toSet()));
            }
        }
        long volume = Math.round((floodBB.maxX - floodBB.minX + 1)) * Math.round((floodBB.maxY - floodBB.minY + 1)) * Math.round((floodBB.maxZ - floodBB.minZ + 1));
        long remainingAir = volume - airBlocks.size() - actualBlocks.size(); // the .grow() is factored in with airBlocks.size()
        if (remainingAir < 2) { // considering you need a seat and an air block above it
            status = BuildError.HULL_FULL;
            return null;
        }
        return hullBlocks;
    }


    public static ArrayList<BlockPos> getBlockNeighbors(World world, AxisAlignedBB aaBB, BlockPos beg) {
        ArrayList<BlockPos> neighbors = new ArrayList<>();
        for (int dirX = -1; dirX < 2; dirX++) {
            for (int dirY = -1; dirY < 2; dirY++) {
                for (int dirZ = -1; dirZ < 2; dirZ++) {
                    if (!(dirX == 0 && dirY == 0 && dirZ == 0)) {
                        BlockPos newPos = beg.add(new Vec3i(dirX, dirY, dirZ));
                        if (actualContains(aaBB, newPos))
                            neighbors.add(newPos);
                    }
                }
            }
        }
        return neighbors;
    }

    public static Vec3i neighborVecs[] = new Vec3i[] {new Vec3i(1,0,0), new Vec3i(0,1,0), new Vec3i(0, 0, 1)};

    public static ArrayList<BlockPos> getBlockOrthNeighbors(World world, BlockPos beg) {
        ArrayList<BlockPos> neighbors = new ArrayList<>();
        for (int dir = -1; dir < 2; dir += 2) {
            for (int i = 0; i < 3; i++) {
                BlockPos newPos = beg.add(new Vec3i(neighborVecs[i].getX()*dir, neighborVecs[i].getY()*dir, neighborVecs[i].getZ()*dir));
                neighbors.add(newPos);
            }
        }
        return neighbors;
    }

    public static ArrayList<BlockPos> getBlockOrthNeighbors(World world, AxisAlignedBB aaBB, BlockPos beg) {
        ArrayList<BlockPos> neighbors = new ArrayList<>();
        for (int dir = -1; dir < 2; dir += 2) {
            for (int i = 0; i < 3; i++) {
                BlockPos newPos = beg.add(new Vec3i(neighborVecs[i].getX()*dir, neighborVecs[i].getY()*dir, neighborVecs[i].getZ()*dir));
                if (actualContains(aaBB, newPos))
                    neighbors.add(newPos);
            }
        }
        return neighbors;
    }

    public static boolean actualContains(AxisAlignedBB aaBB, BlockPos bp) {
        return aaBB.grow(1).contains(new Vec3d(bp));
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return this.createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }

    private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 166);
        builder.widget(new ClickButtonWidget(10, 140,35,20,"Scan", click -> scanPart()));
        builder.widget(new SlotWidget(importItems, 0, 140,140, true, true));
        return builder;
    }

    @Override
    public @Nullable ICleanroomProvider getCleanroom() {
        return linkedCleanroom;
    }

    @Override
    public void setCleanroom(ICleanroomProvider iCleanroomProvider) {
        if (iCleanroomProvider instanceof MetaTileEntityBuildingCleanroom)
            linkedCleanroom = (MetaTileEntityBuildingCleanroom)iCleanroomProvider;
    }
}
