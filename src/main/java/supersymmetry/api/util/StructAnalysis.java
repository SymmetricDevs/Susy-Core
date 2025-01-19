package supersymmetry.api.util;

import com.google.common.collect.Sets;
import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.PatternMatchContext;
import net.minecraft.block.Block;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import supersymmetry.SuSyValues;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.*;

public class StructAnalysis {
    public BuildStat status = BuildStat.SUCCESS;
    public enum BuildStat {
        SUCCESS("susy.msg.rocket_component.build_success"),
        SCANNING("susy.msg.rocket_component.scanning"),
        UNSCANNED("susy.msg.rocket_component.unscanned"),
        DISCONNECTED("susy.msg.rocket_component.blocks_unconnected"),
        EMPTY("susy.msg.rocket_component.no_blocks"),
        HULL_WEAK("susy.msg.rocket_component.hull_weak"),
        HULL_FULL("susy.msg.rocket_component.hull_full"),
        NO_NOZZLE("susy.msg.rocket_component.no_nozzle"),
        NOZZLE_MALFORMED("susy.msg.rocket_component.nozzle_invalid"),
        ERROR("susy.msg.rocket_component.unknown_error"),
        WRONG_NUM_C_CHAMBERS("susy.msg.rocket_component.wrong_num_c_chambers"),
        WRONG_NUM_PUMPS("susy.msg.rocket_component.wrong_num_pumps"),
        WEIRD_PUMP("susy.msg.rocket_component.weird_pump"),
        C_CHAMBER_INSIDE("susy.msg.rocket_component.chamber_wrong_place"),
        INVALID_AIRLIKE("susy.msg.rocket_component.invalid_transparent"),
        EXTRANEOUS_BLOCKS("susy.msg.rocket_component.extraneous_blocks"),
        UNCLEAN("susy.msg.rocket_component.cleanroom_not_clean"), NOT_LAVAL("susy.msg.rocket_component.nozzle_not_laval");

        String code;
        BuildStat(String code) {
            this.code = code;
        }
        public String getCode() {
            return code;
        }
    };
    public World world;
    public StructAnalysis(World world) {
        this.world=world;
    }

    public static Vec3i layerVecs[] = new Vec3i[] {new Vec3i(1,0,0), new Vec3i(0, 0, 1)};
    public static Vec3i orthVecs[] = new Vec3i[] {new Vec3i(1,0,0), new Vec3i(0,1,0), new Vec3i(0, 0, 1)};
    public static Vec3i neighborVecs[] = new Vec3i[] {
            new Vec3i(1,0,0),new Vec3i(-1,0,0),
            new Vec3i(0,1,0),new Vec3i(0,-1,0),
            new Vec3i(0,0,1),new Vec3i(0,0,-1),
            new Vec3i(1,1,0),new Vec3i(-1,-1,0),
            new Vec3i(1,0,1),new Vec3i(-1,0,-1),
            new Vec3i(0,1,1),new Vec3i(0,-1,-1),
            new Vec3i(1,1,1),new Vec3i(-1,-1,-1),
            new Vec3i(1,0,-1),new Vec3i(-1,0,1),
            new Vec3i(1,-1,0),new Vec3i(-1,1,0),
            new Vec3i(0,1,-1),new Vec3i(0,-1,1),
            new Vec3i(1,1,-1),new Vec3i(-1,-1,1),
            new Vec3i(-1,1,1),new Vec3i(1,-1,-1),
            new Vec3i(1,-1,1),new Vec3i(-1,1,-1)
    };
    private static AxisAlignedBB MAX_BB = new AxisAlignedBB(-3.0E7,-3.0E7,0,3.0E7,3.0E7,255);
    public ArrayList<BlockPos> getBlocks(World world, AxisAlignedBB faaBB, boolean checkAir) {
        AxisAlignedBB aaBB = new AxisAlignedBB(Math.round(faaBB.minX),Math.round(faaBB.minY),
                Math.round(faaBB.minZ),Math.round(faaBB.maxX),
                Math.round(faaBB.maxY),Math.round(faaBB.maxZ));
        ArrayList<BlockPos> ret = new ArrayList<>();
        for (int x = (int)aaBB.minX; x < aaBB.maxX; x++) {
            for (int y = (int)aaBB.minY; y < aaBB.maxY; y++) {
                for (int z = (int)aaBB.minZ; z < aaBB.maxZ; z++) {
                    BlockPos bp = new BlockPos(x,y,z);
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
        if (!blockCont(aaBB,beg)) {
            return new HashSet<BlockPos>(); //wtf moment
        }
        Set<BlockPos> blocksCollected = new HashSet<BlockPos>();
        blocksCollected.add(beg);
        Queue<BlockPos> uncheckedBlocks = new ArrayDeque<>(Arrays.asList(beg));


        while (!uncheckedBlocks.isEmpty()) {
            BlockPos bp = uncheckedBlocks.remove();
            blocksCollected.add(bp);
            uncheckedBlocks.addAll(getBlockNeighbors(bp, aaBB).stream()
                    .filter(p -> !world.isAirBlock(p) && !blocksCollected.contains(p) && !uncheckedBlocks.contains(p)).collect(Collectors.toSet()));
        }
        return blocksCollected;
    }

    public Tuple<Set<BlockPos>,Set<BlockPos>> checkHull(AxisAlignedBB aaBB, Set<BlockPos> actualBlocks, boolean testStrength) {
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
                bws.update(world, pos,pmc,null,null, SuSyValues.rocketHullBlocks);
                if (testStrength && !SuSyValues.rocketHullBlocks.test(bws)) {
                    status = BuildStat.HULL_WEAK;
                    return null;
                }
                hullBlocks.add(pos);
            } else {
                airBlocks.add(pos);
                uncheckedBlocks.addAll(getBlockNeighbors(pos, floodBB, orthVecs).stream().filter(
                                p -> floodBB.grow(1).contains(new Vec3d(p)) && !(airBlocks.contains(p) || uncheckedBlocks.contains(p)))
                        .collect(Collectors.toSet()));
            }
        }
        long volume = Math.round((floodBB.maxX - floodBB.minX + 1)) * Math.round((floodBB.maxY - floodBB.minY + 1)) * Math.round((floodBB.maxZ - floodBB.minZ + 1));
        int remainingAir = (int) (volume - airBlocks.size() - actualBlocks.size()); // the .grow() is factored in with airBlocks.size()
        HashSet<BlockPos> air = getBlocks(aaBB);
        air.removeAll(hullBlocks);
        air.removeAll(airBlocks);
        if (remainingAir < 2) { // considering you need a seat and an air block above it
            status = BuildStat.HULL_FULL;
            return null;
        }
        return new Tuple<>(hullBlocks, air);
    }


    public ArrayList<BlockPos> getBlockNeighbors(BlockPos beg, AxisAlignedBB aaBB) {
        return getBlockNeighbors(beg, aaBB, neighborVecs);
    }


    public ArrayList<BlockPos> getBlockNeighbors(BlockPos beg) {
        return getBlockNeighbors(beg,MAX_BB, neighborVecs);
    }

    public ArrayList<BlockPos> getBlockNeighbors(BlockPos beg, Vec3i[] neighborVecs) {
        return getBlockNeighbors(beg,MAX_BB, neighborVecs);
    }

    public ArrayList<BlockPos> getBlockNeighbors(BlockPos beg, AxisAlignedBB aaBB, Vec3i[] neighborVecs) {
        ArrayList<BlockPos> neighbors = new ArrayList<>();
        for (Vec3i vec: neighborVecs) {
            for (int i = -1; i < 2; i+=2) {
                BlockPos newPos = beg.add(multiply(i,vec));
                if (blockCont(aaBB, newPos))
                    neighbors.add(newPos);
            }
        }
        return neighbors;
    }

    public static boolean blockCont(AxisAlignedBB bb, BlockPos bp) {
        return bb.minX <= bp.getX() && bb.minY <= bp.getY() && bb.minZ <= bp.getZ() &&
                bb.maxX > bp.getX() && bb.maxY > bp.getY() && bb.maxZ > bp.getZ();
    }

    public HashSet<BlockPos> getBlocks(AxisAlignedBB bb) {
        HashSet<BlockPos> ret = new HashSet<>();
        for (int x = (int)bb.minX; x < bb.maxX; x++) {
            for (int y = (int)bb.minY; y < bb.maxY; y++) {
                for (int z = (int)bb.minZ; z < bb.maxZ; z++) {
                   ret.add(new BlockPos(x,y,z));
                }
            }
        }
        return ret;
    }

    public Set<BlockPos> getLayerAir(World world, AxisAlignedBB section, int y) {
        AxisAlignedBB sect = new AxisAlignedBB(section.minX-1,y,section.minZ-1,section.maxX+1,y+1,section.maxZ+1);
        Predicate<BlockPos> isNotObstacle = ((Predicate<BlockPos>)world::isAirBlock).or(bp -> !blockCont(section,bp));
        Set<BlockPos> blocks = getBlocks(sect).stream().filter(isNotObstacle).collect(Collectors.toSet()); // the one-argument getBlocks doesn't care about air blocks
        List<HashSet<BlockPos>> partitions = new ArrayList<>();
        Set<BlockPos> consumed = new HashSet<>();
        for (BlockPos block: blocks) {
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
                Stream<BlockPos> stream = getBlockNeighbors(bp, sect, layerVecs).stream().filter(((Predicate<BlockPos>)bPart::contains).negate().and(isNotObstacle));
                remaining.addAll(getBlockNeighbors(bp, sect, layerVecs).stream().filter(((Predicate<BlockPos>)bPart::contains).negate().and(isNotObstacle)).
                        collect(Collectors.toList()));
                stream.forEach(consumed::add);
            }
            partitions.add(bPart);
        }
        // This looks cursed, but the idea is to ensure that
        if (partitions.size() != 2) {
            status = BuildStat.NOZZLE_MALFORMED;
            return null;
        } else {
            List<Boolean> res = partitions.stream().map(set -> getPerimeter(set, layerVecs).stream().allMatch(p -> blockCont(sect,p))).collect(Collectors.toList());
            for (int i = 0; i < 2; i++) {
                if (res.get(i)) {
                    return partitions.get(i);
                }
            }
        }
        status = BuildStat.ERROR;
        return null;
    }

    //
    public Set<BlockPos> getPerimeter(Collection<BlockPos> blocks, Vec3i vecs[]) {
        Set<BlockPos> ret = new HashSet<>();
        for (BlockPos block: blocks) {
            ret.addAll(getBlockNeighbors(block, MAX_BB, vecs).stream().filter(((Predicate<BlockPos>)blocks::contains).negate()).collect(Collectors.toSet()));
        }
        return ret;
    }

    public AxisAlignedBB getBB(Collection<BlockPos> blocks) {
        int minX=(int)3.0E7,minY=(int)3.0E7,minZ=(int)3.0E7,maxX = (int)-3.0E7,maxY=(int)-3.0E7,maxZ = (int)-3.0E7;
        for (BlockPos block: blocks) {
            maxX = Math.max(block.getX()+1, maxX);
            maxY = Math.max(block.getY()+1,maxY);
            maxZ = Math.max(block.getZ()+1,maxZ);
            minX = Math.min(block.getX(),minX);
            minY = Math.min(block.getY(),minY);
            minZ = Math.min(block.getZ(),minZ);
        }
        return new AxisAlignedBB(minX,minY,minZ,maxX,maxY,maxZ);
    }
    public Stream<BlockPos> getOfBlockType(Collection<BlockPos> bp, Block block) {
        return bp.stream()
                .filter(p -> world.getBlockState(p).getBlock().equals(block));
    }

    public Vec3i multiply(int mult, Vec3i inp) {
        return new Vec3i(mult*inp.getX(), mult*inp.getY(), mult*inp.getZ());
    }

    public Vec3i diff(Vec3i one, Vec3i two) {
        return new Vec3i(one.getX()-two.getX(),one.getY()-two.getY(),one.getZ()-two.getZ());
    }
}

