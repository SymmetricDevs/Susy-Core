package supersymmetry.api.util;

import static supersymmetry.api.blocks.VariantDirectionalRotatableBlock.FACING;
import static supersymmetry.api.util.Welzl.computeMinimalRadius;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.*;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.PatternMatchContext;
import supersymmetry.SuSyValues;
import supersymmetry.api.SusyLog;

public class StructAnalysis {

    public BuildStat status = BuildStat.SUCCESS;

    public enum BuildStat {

        SUCCESS("build_success"),
        SCANNING("scanning"),
        UNSCANNED("unscanned"),
        DISCONNECTED("blocks_disconnected"),
        EMPTY("no_blocks"),
        HULL_WEAK("hull_weak"),
        HULL_FULL("hull_full"),
        NO_NOZZLE("no_nozzle"),
        NOZZLE_MALFORMED("nozzle_invalid"),
        ERROR("unknown_error"),
        WRONG_NUM_C_CHAMBERS("wrong_num_c_chambers"),
        WRONG_NUM_PUMPS("wrong_num_pumps"),
        WEIRD_PUMP("weird_pump"),
        C_CHAMBER_INSIDE("chamber_wrong_place"),
        INVALID_AIRLIKE("invalid_transparent"),
        EXTRANEOUS_BLOCKS("extraneous_blocks"),
        UNCLEAN("cleanroom_not_clean"),
        NOT_LAVAL("nozzle_not_laval"),
        MISSING_TILE("tile_missing"),
        INTERSTAGE_NOT_CYLINDRICAL("interstage_not_cylindrical"),
        WEIRD_FAIRING("fairing_weird"),
        NO_CARD("no_card"),
        UNRECOGNIZED("part_unrecognized"),
        SPACECRAFT_HOLLOW("spacecraft_hollow"),
        WEIRD_PADDING("weird_padding"),
        TOO_SHORT("too_short"),
        CONN_UNALIGNED("conn_unaligned"),
        CONN_WRONG_DIR("conn_wrong_dir"),
        WRONG_TILE("wrong_tile");

        String code;

        BuildStat(String code) {
            this.code = "susy.msg.rocket_component." + code;
        }

        public String getCode() {
            return code;
        }
    }

    public World world;

    public StructAnalysis(World world) {
        this.world = world;
    }

    public static Vec3i[] layerVecs = new Vec3i[] { new Vec3i(1, 0, 0), new Vec3i(0, 0, 1) };
    public static Vec3i[] orthVecs = new Vec3i[] { new Vec3i(1, 0, 0), new Vec3i(0, 1, 0), new Vec3i(0, 0, 1) };
    public static Vec3i[] neighborVecs = new Vec3i[] {
            new Vec3i(1, 0, 0), new Vec3i(-1, 0, 0),
            new Vec3i(0, 1, 0), new Vec3i(0, -1, 0),
            new Vec3i(0, 0, 1), new Vec3i(0, 0, -1),
            new Vec3i(1, 1, 0), new Vec3i(-1, -1, 0),
            new Vec3i(1, 0, 1), new Vec3i(-1, 0, -1),
            new Vec3i(0, 1, 1), new Vec3i(0, -1, -1),
            new Vec3i(1, 1, 1), new Vec3i(-1, -1, -1),
            new Vec3i(1, 0, -1), new Vec3i(-1, 0, 1),
            new Vec3i(1, -1, 0), new Vec3i(-1, 1, 0),
            new Vec3i(0, 1, -1), new Vec3i(0, -1, 1),
            new Vec3i(1, 1, -1), new Vec3i(-1, -1, 1),
            new Vec3i(-1, 1, 1), new Vec3i(1, -1, -1),
            new Vec3i(1, -1, 1), new Vec3i(-1, 1, -1)
    };
    private static final AxisAlignedBB MAX_BB = new AxisAlignedBB(-3.0E7, 0, -3.0E7, 3.0E7, 255, 3.0E7);

    public ArrayList<BlockPos> getBlocks(World world, AxisAlignedBB faaBB, boolean checkAir) {
        AxisAlignedBB aaBB = new AxisAlignedBB(Math.round(faaBB.minX), Math.round(faaBB.minY),
                Math.round(faaBB.minZ), Math.round(faaBB.maxX),
                Math.round(faaBB.maxY), Math.round(faaBB.maxZ));
        ArrayList<BlockPos> ret = new ArrayList<>();
        for (int x = (int) aaBB.minX; x < aaBB.maxX; x++) {
            for (int y = (int) aaBB.minY; y < aaBB.maxY; y++) {
                for (int z = (int) aaBB.minZ; z < aaBB.maxZ; z++) {
                    BlockPos bp = new BlockPos(x, y, z);
                    if (!world.isAirBlock(bp)) {
                        if (checkAir && world.getBlockState(bp).getCollisionBoundingBox(world, bp) == null) {
                            status = BuildStat.INVALID_AIRLIKE;
                            return null;
                        }
                        ret.add(bp);
                    }
                }
            }
        }
        return ret;
    }

    public Set<BlockPos> getBlockConn(AxisAlignedBB aaBB, BlockPos beg) {
        if (!blockCont(aaBB, beg)) {
            return new HashSet<>(); // wtf moment
        }
        Set<BlockPos> blocksCollected = new HashSet<BlockPos>();
        blocksCollected.add(beg);
        Queue<BlockPos> uncheckedBlocks = new ArrayDeque<>(List.of(beg));

        while (!uncheckedBlocks.isEmpty()) {
            BlockPos bp = uncheckedBlocks.remove();
            blocksCollected.add(bp);
            uncheckedBlocks.addAll(getBlockNeighbors(bp, aaBB).stream()
                    .filter(p -> !world.isAirBlock(p) && !blocksCollected.contains(p) && !uncheckedBlocks.contains(p))
                    .collect(Collectors.toSet()));
        }
        return blocksCollected;
    }

    public record HullData(Set<BlockPos> exterior, Set<BlockPos> interior) {}

    public HullData checkHull(AxisAlignedBB aaBB, Set<BlockPos> actualBlocks,
                              boolean testStrength) {
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
                bws.update(world, pos, pmc, null, null, SuSyValues.rocketHullBlocks);
                if (testStrength && !SuSyValues.rocketHullBlocks.test(bws)) {
                    status = BuildStat.HULL_WEAK;
                    return null;
                }
                hullBlocks.add(pos);
            } else {
                airBlocks.add(pos);
                uncheckedBlocks.addAll(getBlockNeighbors(pos, floodBB, orthVecs).stream().filter(
                        p -> floodBB.grow(1).contains(new Vec3d(p)) &&
                                !(airBlocks.contains(p) || uncheckedBlocks.contains(p)))
                        .collect(Collectors.toSet()));
            }
        }
        long volume = Math.round((floodBB.maxX - floodBB.minX + 1)) * Math.round((floodBB.maxY - floodBB.minY + 1)) *
                Math.round((floodBB.maxZ - floodBB.minZ + 1));
        int remainingAir = (int) (volume - airBlocks.size() - actualBlocks.size()); // the .grow() is factored in with
                                                                                    // airBlocks.size()
        HashSet<BlockPos> air = getBlocks(aaBB);
        air.removeAll(hullBlocks);
        air.removeAll(airBlocks);
        if (remainingAir < 2) { // considering you need a seat and an air block above it
            status = BuildStat.HULL_FULL;
            return null;
        }
        return new HullData(hullBlocks, air);
    }

    public ArrayList<BlockPos> getBlockNeighbors(BlockPos beg, AxisAlignedBB aaBB) {
        return getBlockNeighbors(beg, aaBB, neighborVecs);
    }

    public ArrayList<BlockPos> getBlockNeighbors(BlockPos beg) {
        return getBlockNeighbors(beg, MAX_BB, neighborVecs);
    }

    public ArrayList<BlockPos> getBlockNeighbors(BlockPos beg, Vec3i[] neighborVecs) {
        return getBlockNeighbors(beg, MAX_BB, neighborVecs);
    }

    public ArrayList<BlockPos> getBlockNeighbors(BlockPos beg, AxisAlignedBB aaBB, Vec3i[] neighborVecs) {
        ArrayList<BlockPos> neighbors = new ArrayList<>();
        for (Vec3i vec : neighborVecs) {
            for (int i = -1; i < 2; i += 2) {
                BlockPos newPos = beg.add(multiply(i, vec));
                if (blockCont(aaBB, newPos))
                    neighbors.add(newPos);
            }
        }
        return neighbors;
    }

    /**
     * Checks if a block is within an axis aligned bounding box
     * 
     * @param bb the bounding box
     * @param bp the block position
     * @return If the block is inside or not
     */
    public static boolean blockCont(AxisAlignedBB bb, BlockPos bp) {
        return bb.minX <= bp.getX() && bb.minY <= bp.getY() && bb.minZ <= bp.getZ() &&
                bb.maxX > bp.getX() && bb.maxY > bp.getY() && bb.maxZ > bp.getZ();
    }

    /**
     * Creates a hash set of all block positions within an AABB
     * 
     * @param bb the AABB to find all blocks fitting inside
     * @return A hash set
     */
    public HashSet<BlockPos> getBlocks(AxisAlignedBB bb) {
        HashSet<BlockPos> ret = new HashSet<>();
        for (int x = (int) bb.minX; x < bb.maxX; x++) {
            for (int y = (int) bb.minY; y < bb.maxY; y++) {
                for (int z = (int) bb.minZ; z < bb.maxZ; z++) {
                    ret.add(new BlockPos(x, y, z));
                }
            }
        }
        return ret;
    }

    /**
     *
     * @param sect
     * @return
     */
    public List<HashSet<BlockPos>> getPartitions(AxisAlignedBB sect) {
        Predicate<BlockPos> isNotObstacle = ((Predicate<BlockPos>) world::isAirBlock).or(bp -> !blockCont(sect, bp));
        Set<BlockPos> blocks = getBlocks(sect).stream().filter(isNotObstacle).collect(Collectors.toSet());
        // the one-argument getBlocks doesn't care about air blocks

        List<HashSet<BlockPos>> partitions = new ArrayList<>();
        Set<BlockPos> consumed = new HashSet<>();
        for (BlockPos block : blocks) {
            if (consumed.contains(block)) {
                continue;
            }
            consumed.add(block);
            ArrayDeque<BlockPos> remaining = new ArrayDeque<>();
            HashSet<BlockPos> bPart = new HashSet<>();
            remaining.add(block);
            while (!remaining.isEmpty()) {
                BlockPos bp = remaining.pop();
                bPart.add(bp);
                Stream<BlockPos> stream = getBlockNeighbors(bp, sect, orthVecs).stream()
                        .filter(((Predicate<BlockPos>) bPart::contains).negate().and(isNotObstacle));
                remaining.addAll(getBlockNeighbors(bp, sect, orthVecs).stream()
                        .filter(((Predicate<BlockPos>) bPart::contains).negate().and(isNotObstacle))
                        .collect(Collectors.toList()));
                stream.forEach(consumed::add);
            }
            partitions.add(bPart);
        }
        return partitions;
    }

    public Set<BlockPos> getLayerAir(AxisAlignedBB section, int y) {
        AxisAlignedBB sect = new AxisAlignedBB(section.minX - 1, y, section.minZ - 1, section.maxX + 1, y + 1,
                section.maxZ + 1);
        Predicate<BlockPos> isNotObstacle = ((Predicate<BlockPos>) world::isAirBlock).or(bp -> !blockCont(section, bp));
        Set<BlockPos> blocks = getBlocks(sect).stream().filter(isNotObstacle).collect(Collectors.toSet());
        // the one-argument getBlocks doesn't care about air blocks (again)

        List<HashSet<BlockPos>> partitions = getPartitions(sect);
        // This looks cursed, but the idea is to ensure that
        if (partitions.size() != 2) {
            status = BuildStat.NOZZLE_MALFORMED;
            return null;
        } else {
            List<Boolean> res = partitions.stream()
                    .map(set -> getPerimeter(set, layerVecs).stream().allMatch(p -> blockCont(sect, p)))
                    .collect(Collectors.toList());
            for (int i = 0; i < 2; i++) {
                if (res.get(i)) {
                    return partitions.get(i);
                }
            }
        }
        status = BuildStat.ERROR;
        return null;
    }

    /**
     * Gets all blocks bordering a collection of BlockPoses given a certain neighborhood
     * 
     * @param blocks The blocks to find those around.
     * @param vecs   The offsets of blocks considered in a neighborhood
     * @return A set containing the border, not any of blocks.
     */
    public Set<BlockPos> getPerimeter(Collection<BlockPos> blocks, Vec3i[] vecs) {
        Set<BlockPos> ret = new HashSet<>();
        for (BlockPos block : blocks) {
            ret.addAll(getBlockNeighbors(block, MAX_BB, vecs).stream()
                    .filter(((Predicate<BlockPos>) blocks::contains).negate()).collect(Collectors.toSet()));
        }
        return ret;
    }

    // Obtains the bounding box of all blocks in the collection blocks
    public AxisAlignedBB getBB(Collection<BlockPos> blocks) {
        int minX = (int) 3.0E7, minY = (int) 3.0E7, minZ = (int) 3.0E7, maxX = (int) -3.0E7, maxY = (int) -3.0E7,
                maxZ = (int) -3.0E7;
        for (BlockPos block : blocks) {
            maxX = Math.max(block.getX() + 1, maxX);
            maxY = Math.max(block.getY() + 1, maxY);
            maxZ = Math.max(block.getZ() + 1, maxZ);
            minX = Math.min(block.getX(), minX);
            minY = Math.min(block.getY(), minY);
            minZ = Math.min(block.getZ(), minZ);
        }
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public double getRadius(Collection<BlockPos> blocks) {
        if (blocks.isEmpty()) {
            SusyLog.logger.warn("No blocks were found in {}!", blocks);
            return 0;
        }

        List<Welzl.Point2D> points = blocks.stream()
                .map(blockPos -> new Welzl.Point2D(blockPos.getX(), blockPos.getZ()))
                .collect(Collectors.toList());

        return computeMinimalRadius(points);
    }

    public int getHeight(Collection<BlockPos> blocks) {
        // Get max and min at same time
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (BlockPos block : blocks) {
            int y = block.getY();
            if (y > max) {
                max = y;
            }
            if (y < min) {
                min = y;
            }
        }

        return max - min;
    }

    /**
     * Gets the lowest layer of a set of blocks
     * 
     * @param set A set of blocks
     * @return All blocks in set with the minimum Y value
     */
    public Set<BlockPos> getLowestLayer(Set<BlockPos> set) {
        if (set.isEmpty()) {
            return new HashSet<>();
        }
        int minY = set.stream().map(Vec3i::getY).min(Integer::compare).get();
        return set.stream().filter(bp -> bp.getY() == minY)
                .collect(Collectors.toSet());
    }

    // Used to analyze fairing connectors
    public boolean isFacingOutwards(BlockPos bp) {
        if (!world.getBlockState(bp).getPropertyKeys().contains(FACING)) {
            return false;
        }
        EnumFacing facing = world.getBlockState(bp).getValue(FACING);
        return world.isAirBlock(bp.add(facing.getDirectionVec()));
    }

    public Set<BlockPos> getClosest(BlockPos bp, Set<BlockPos> candidates) {
        double closestDist = candidates.iterator().next().getDistance(bp.getX(), bp.getY(), bp.getZ());
        Set<BlockPos> closestNeighbors = new HashSet<BlockPos>();
        for (BlockPos neighbor : candidates) {
            double neighborDistance = neighbor.getDistance(bp.getX(), bp.getY(), bp.getZ());
            if (neighborDistance < closestDist) {
                closestNeighbors.clear();
                closestNeighbors.add(neighbor);
                closestDist = neighborDistance;
            } else if (neighborDistance == closestDist) {
                closestNeighbors.add(neighbor);
            }
        }
        return closestNeighbors;
    }

    public int getCoordRelevantToDirection(BlockPos bp) {
        if (world.getBlockState(bp).getPropertyKeys().contains(FACING)) {
            EnumFacing dir = this.world.getBlockState(bp).getValue(FACING);
            if (dir.equals(EnumFacing.UP) || dir.equals(EnumFacing.DOWN)) {

            }
        } else {
            return 0;
        }
        return 0;
    }

    public Stream<BlockPos> getOfBlockType(Collection<BlockPos> bp, Block block) {
        return bp.stream()
                .filter(p -> world.getBlockState(p).getBlock().equals(block));
    }

    public int getCoordOfAxis(BlockPos bp) {
        if (!world.getBlockState(bp).getPropertyKeys().contains(FACING)) {
            return 0;
        }
        EnumFacing facing = world.getBlockState(bp).getValue(FACING);
        EnumFacing.Axis axis = facing.getAxis();
        return switch (axis) {
            case X -> bp.getX();
            case Y -> bp.getY();
            case Z -> bp.getZ();
        };
    }

    public Vec3i multiply(int mult, Vec3i inp) {
        return new Vec3i(mult * inp.getX(), mult * inp.getY(), mult * inp.getZ());
    }

    public Vec3i diff(Vec3i one, Vec3i two) {
        return new Vec3i(one.getX() - two.getX(), one.getY() - two.getY(), one.getZ() - two.getZ());
    }

    public Optional<NBTTagCompound> errorPos(BlockPos error) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("errorPos", error.toLong());
        return Optional.of(tag);
    }
}
