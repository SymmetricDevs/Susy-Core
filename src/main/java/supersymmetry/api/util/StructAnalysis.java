package supersymmetry.api.util;

import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.PatternMatchContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import supersymmetry.SuSyValues;
import supersymmetry.common.blocks.SuSyBlocks;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.*;

public class StructAnalysis {
    public BuildError status = BuildError.SUCCESS;
    public enum BuildError {
        SUCCESS,
        DISCONNECTED,
        EMPTY,
        HULL_WEAK,
        HULL_FULL,
        NO_NOZZLE,
        NOZZLE_MALFORMED, ERROR, WRONG_NUM_C_CHAMBERS, WRONG_NUM_PUMPS, WEIRD_PUMP, C_CHAMBER_INSIDE, INVALID_AIRLIKE, EXTRANEOUS_BLOCKS;
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
        AxisAlignedBB aaBB = new AxisAlignedBB(Math.round(faaBB.minX),Math.round(faaBB.maxX),
                Math.round(faaBB.minY),Math.round(faaBB.maxY),
                Math.round(faaBB.minZ),Math.round(faaBB.maxZ));
        ArrayList<BlockPos> ret = new ArrayList<>();
        for (int x = (int)aaBB.minX; x < aaBB.maxX; x++) {
            for (int y = (int)aaBB.minY; y < aaBB.maxY; y++) {
                for (int z = (int)aaBB.minZ; z < aaBB.maxZ; z++) {
                    BlockPos bp = new BlockPos(x,y,z);
                    if (!world.isAirBlock(bp)) {
                        if (checkAir && world.getBlockState(bp).getCollisionBoundingBox(world, bp) == null) {
                            status = BuildError.INVALID_AIRLIKE;
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
        if (!aaBB.contains(new Vec3d(beg))) {
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

    public Set<BlockPos> checkHull(AxisAlignedBB aaBB, Set<BlockPos> actualBlocks, boolean testStrength) {
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
                    status = BuildError.HULL_WEAK;
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
        long remainingAir = volume - airBlocks.size() - actualBlocks.size(); // the .grow() is factored in with airBlocks.size()
        if (remainingAir < 2) { // considering you need a seat and an air block above it
            status = BuildError.HULL_FULL;
            return null;
        }
        return hullBlocks;
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
            BlockPos newPos = beg.add(vec);
            if (aaBB.grow(1).contains(new Vec3d(newPos)))
                neighbors.add(newPos);
        }
        return neighbors;
    }

    public boolean blockCont(AxisAlignedBB bb, BlockPos bp) {
        return bb.minX <= bp.getX() && bb.minY <= bp.getY() && bb.minZ <= bp.getZ() &&
                bb.maxX > bp.getX() && bb.maxY > bp.getY() && bb.maxZ > bp.getZ();
    }

    public ArrayList<BlockPos> getBlocks(AxisAlignedBB bb) {
        ArrayList<BlockPos> ret = new ArrayList<>();
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
        Set<BlockPos> blocks = getBlocks(sect).stream().filter(world::isAirBlock).collect(Collectors.toSet());
        List<HashSet<BlockPos>> partitions = new ArrayList<>();
        for (BlockPos block: blocks) {
            ArrayDeque<BlockPos> remaining = new ArrayDeque<>();
            HashSet<BlockPos> bPart = new HashSet<>();
            remaining.add(block);
            while (!remaining.isEmpty()) {
                BlockPos bp = remaining.pop();
                bPart.add(bp);
                Stream<BlockPos> stream = getBlockNeighbors(bp, sect, layerVecs).stream().filter(pos->!bPart.contains(pos)&&world.isAirBlock(pos));
                remaining.addAll(getBlockNeighbors(bp, sect, layerVecs).stream().filter(pos->!bPart.contains(pos)&&world.isAirBlock(pos)).collect(Collectors.toList()));
                stream.forEach(blocks::remove);
            }
            partitions.add(bPart);
        }
        if (partitions.size() > 2) {
            status = BuildError.NOZZLE_MALFORMED;
            return null;
        } else {
            List<Boolean> res = partitions.stream().map(set -> getPerimeter(set, layerVecs).stream().allMatch(p -> blockCont(sect,p))).collect(Collectors.toList());
            for (int i = 0; i < 2; i++) {
                if (res.get(i)) {
                    return partitions.get(i);
                }
            }
        }
        status = BuildError.ERROR;
        return null;
    }

    public Set<BlockPos> getPerimeter(Collection<BlockPos> blocks, Vec3i vecs[]) {
        Set<BlockPos> ret = new HashSet<>();
        for (BlockPos block: blocks) {
            ret.addAll(getBlockNeighbors(block, MAX_BB, vecs));
        }
        return ret;
    }

    public AxisAlignedBB getBB(Collection<BlockPos> blocks) {
        int minX=0,minY=0,minZ=0,maxX = 0,maxY=0,maxZ = 0;
        for (BlockPos block: blocks) {
            maxX = Math.max(block.getX(), maxX);
            maxY = Math.max(block.getY(),maxY);
            maxZ = Math.max(block.getZ(),maxZ);
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
}

