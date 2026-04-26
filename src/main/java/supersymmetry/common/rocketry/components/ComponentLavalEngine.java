package supersymmetry.common.rocketry.components;

import static supersymmetry.api.blocks.VariantDirectionalRotatableBlock.FACING;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;

import gregtech.api.block.VariantBlock;
import gregtech.api.unification.material.Materials;
import supersymmetry.api.SusyLog;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.components.MaterialCost;
import supersymmetry.api.rocketry.components.RocketEngine;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.rocketry.BlockCombustionChamber;

public class ComponentLavalEngine extends AbstractComponent<ComponentLavalEngine> implements RocketEngine {

    public double areaRatio;
    public double fuelThroughput;

    public ComponentLavalEngine() {
        super(
                "laval_engine",
                "engine",
                candidate -> candidate.getSecond().stream()
                        .anyMatch(
                                pos -> {
                                    boolean a = candidate
                                            .getFirst().world
                                                    .getBlockState(pos)
                                                    .getBlock()
                                                    .equals(SuSyBlocks.COMBUSTION_CHAMBER);
                                    boolean b = candidate
                                            .getFirst().world
                                                    .getBlockState(pos)
                                                    .equals(SuSyBlocks.COMBUSTION_CHAMBER.getState(
                                                            BlockCombustionChamber.CombustionType.MONOPROPELLANT));
                                    return a && !b;
                                }));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setDouble("radius", this.radius);
        tag.setDouble("area_ratio", this.areaRatio);
        tag.setDouble("throughput", this.fuelThroughput);
    }

    @Override
    public Optional<ComponentLavalEngine> readFromNBT(NBTTagCompound compound) {
        if (compound.getString("type").isEmpty() || compound.getString("name").isEmpty()) {
            return Optional.empty();
        }
        ComponentLavalEngine engine = new ComponentLavalEngine();
        if (!compound.hasKey("mass", Constants.NBT.TAG_DOUBLE)) return Optional.empty();
        if (!compound.hasKey("radius", Constants.NBT.TAG_DOUBLE)) return Optional.empty();
        if (!compound.hasKey("area_ratio", Constants.NBT.TAG_DOUBLE)) return Optional.empty();
        if (!compound.hasKey("materials", Constants.NBT.TAG_LIST)) return Optional.empty();
        if (!compound.hasKey("throughput", Constants.NBT.TAG_DOUBLE)) return Optional.empty();
        compound
                .getTagList("materials", Constants.NBT.TAG_COMPOUND)
                .forEach(x -> engine.materials.add(MaterialCost.fromNBT((NBTTagCompound) x)));

        engine.areaRatio = compound.getDouble("area_ratio");
        engine.radius = compound.getDouble("radius");
        engine.mass = compound.getDouble("mass");
        engine.fuelThroughput = compound.getDouble("throughput");

        if (engine.materials.isEmpty()) {
            SusyLog.logger.warn("No materials were found in {}!", compound);
        }
        return Optional.of(engine);
    }

    @Override
    public Optional<NBTTagCompound> analyzePattern(StructAnalysis analysis, AxisAlignedBB aabb) {
        Set<BlockPos> blocks = analysis.getBlockConn(aabb, analysis.getBlocks(analysis.world, aabb, true).get(0));
        Set<BlockPos> nozzle = analysis.getOfBlockType(blocks, SuSyBlocks.ROCKET_NOZZLE).collect(Collectors.toSet());
        if (nozzle.isEmpty()) {
            analysis.status = BuildStat.NO_NOZZLE;
            return Optional.empty();
        }
        ArrayList<Integer> areas = new ArrayList<>();
        AxisAlignedBB nozzleBB = analysis.getBB(nozzle);
        List<Block> allowedBlocks = Arrays.asList(Blocks.AIR, Blocks.PLANKS);

        for (int i = (int) nozzleBB.maxY - 1; i >= (int) nozzleBB.minY; i--) {
            Set<BlockPos> airLayer = analysis.getLayerOccupied(nozzleBB, i, allowedBlocks);
            if (airLayer == null) { // there should be an error here
                analysis.status = BuildStat.NOZZLE_MALFORMED;
                return Optional.empty();
            }
            Set<BlockPos> airPerimeter = analysis.getPerimeter(airLayer, StructAnalysis.layerVecs);
            if ((double) airPerimeter.size() < 3 * Math.sqrt((double) airLayer.size())) { // Establishes a roughly
                // circular pattern
                analysis.status = BuildStat.NOZZLE_MALFORMED;
                return Optional.empty();
            }
            areas.add(airLayer.size() + airPerimeter.size() / 2);
        }

        // For all rocket nozzles, the air layer list should be increasing. 3 blocks should be a minimum
        // length under that assumption.
        if (areas.size() < 3 || areas.get(0) > 5) {
            if (areas.size() < 3) {
                analysis.status = BuildStat.NOZZLE_TOO_SHORT;
            } else {
                analysis.status = BuildStat.NOZZLE_MALFORMED;
            }
            return Optional.empty();
        }

        int initial = areas.get(0);
        int fin = initial;

        for (int a : areas) {
            if (fin <= a) {
                fin = a;
            } else {
                analysis.status = BuildStat.NOT_LAVAL;
                return Optional.empty();
            }
        }
        float computedAreaRatio = ((float) fin) / initial;
        if (computedAreaRatio < 1.5) {
            analysis.status = BuildStat.NOT_LAVAL;
            return Optional.empty();
        }

        // One combustion chamber is, I think, reasonable
        List<BlockPos> cChambers = analysis.getOfBlockType(blocks, SuSyBlocks.COMBUSTION_CHAMBER)
                .collect(Collectors.toList());
        if (cChambers.size() != 1) {
            analysis.status = BuildStat.WRONG_NUM_C_CHAMBERS;
            return Optional.empty();
        }
        // Below the chamber: Open space
        BlockPos cChamber = cChambers.get(0);
        Set<BlockPos> pumps = analysis
                .getOfBlockType(
                        analysis.getBlockNeighbors(cChamber, StructAnalysis.orthVecs), SuSyBlocks.TURBOPUMP)
                .collect(Collectors.toSet());
        if (nozzleBB.contains(new Vec3d(cChamber))) {
            analysis.status = BuildStat.C_CHAMBER_INSIDE;
            return Optional.empty();
        }
        if (!analysis.world.isAirBlock(cChamber.add(0, -1, 0)) && !analysis.world.getBlockState(cChamber.add(0, -1, 0)).getBlock().equals(Blocks.PLANKS)) {
            analysis.status = BuildStat.NOZZLE_MALFORMED;
            return analysis.errorPos(cChamber.add(0, -1, 0));
        }
        // Analyze turbopumps
        IBlockState chamberState = analysis.world.getBlockState(cChamber);
        int pumpNum = ((BlockCombustionChamber.CombustionType) (((VariantBlock<?>) chamberState.getBlock())
                .getState(chamberState)))
                        .getMinPumps();
        if (pumps.size() < pumpNum) {
            analysis.status = BuildStat.WRONG_NUM_PUMPS;
            return Optional.empty();
        }
        for (BlockPos pumpPos : pumps) {
            EnumFacing dir = analysis.world.getBlockState(pumpPos).getValue(FACING);
            if (dir.equals(EnumFacing.UP) || !pumpPos.add(dir.getDirectionVec()).equals(cChamber)) {
                analysis.status = BuildStat.WEIRD_PUMP;
                return analysis.errorPos(pumpPos);
            }
        }

        // Analyzes match
        Set<BlockPos> stickBlocks = analysis.getOfMaterial(blocks, Materials.Wood).collect(Collectors.toSet());
        if (!stickBlocks.isEmpty()) {
            for (BlockPos stickPos : stickBlocks) {
                if (!StructAnalysis.blockCont(nozzleBB, stickPos)) {
                    analysis.status = BuildStat.MATCH_WRONG;
                    return Optional.empty();
                }
            }
        }

        // Creates engine
        Set<BlockPos> engineBlocks = new HashSet<>(nozzle);
        engineBlocks.addAll(pumps);
        engineBlocks.add(cChamber);
        engineBlocks.addAll(
                analysis.getOfBlockType(blocks, SuSyBlocks.INTERSTAGE).collect(Collectors.toSet()));
        engineBlocks.addAll(stickBlocks);

        if (engineBlocks.size() < blocks.size()) {
            analysis.status = BuildStat.EXTRANEOUS_BLOCKS;
            return Optional.empty();
        }
        analysis.status = BuildStat.SUCCESS;
        // currently a double
        NBTTagCompound tag = new NBTTagCompound();
        tag.setDouble("area_ratio", computedAreaRatio);
        this.areaRatio = computedAreaRatio;
        // Not the default; more of an inner radius
        this.radius = analysis.getRadius(
                blocks.stream().filter(bp -> bp.getY() == nozzleBB.maxY).collect(Collectors.toSet()));
        tag.setDouble("radius", radius);

        collectInfo(analysis, blocks, tag);

        double throughput = 0;

        for (BlockPos pumpPos : pumps) {
            IBlockState pump = analysis.world.getBlockState(pumpPos);
            throughput += (SuSyBlocks.TURBOPUMP.getState(pump)).getThroughput();
        }

        this.fuelThroughput = throughput;
        tag.setDouble("throughput", fuelThroughput);

        tag.setBoolean("has_match", !stickBlocks.isEmpty());

        writeBlocksToNBT(blocks, analysis.world);
        return Optional.of(tag);
    }

    @Override
    public double getFuelThroughput() {
        return fuelThroughput;
    }
}
