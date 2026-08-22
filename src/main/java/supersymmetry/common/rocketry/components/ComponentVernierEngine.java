package supersymmetry.common.rocketry.components;

import static supersymmetry.api.blocks.VariantDirectionalRotatableBlock.FACING;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;

import gregtech.api.block.VariantBlock;
import gregtech.api.unification.material.Materials;
import supersymmetry.api.SusyLog;
import supersymmetry.api.rocketry.NozzleFlow;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.components.MaterialCost;
import supersymmetry.api.rocketry.components.RocketEngine;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.api.util.SuSyUtility;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.rocketry.BlockCombustionChamber;

public class ComponentVernierEngine extends AbstractComponent<ComponentVernierEngine> implements RocketEngine {

    public double areaRatio;
    public double fuelThroughput;
    public double chamberPressure;
    public double exitHalfAngle;
    public double wettedAreaRatio;
    public double contourTurning;

    public ComponentVernierEngine() {
        super("vernier_engine", "engine_small", candidate -> candidate.getSecond().stream()
                .anyMatch(pos -> candidate.getFirst().world.getBlockState(pos).equals(
                        SuSyBlocks.COMBUSTION_CHAMBER.getState(BlockCombustionChamber.CombustionType.MONOPROPELLANT))));
        this.setComponentSlotValidator(x -> x.equals(this.getName()) || x.equals(this.getType()));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setDouble("radius", this.radius);
        tag.setDouble("area_ratio", this.areaRatio);
        tag.setDouble("throughput", this.fuelThroughput);
        tag.setDouble("chamber_pressure", this.chamberPressure);
        tag.setDouble("exit_angle", this.exitHalfAngle);
        tag.setDouble("wetted_ratio", this.wettedAreaRatio);
        tag.setDouble("turning", this.contourTurning);
    }

    @Override
    public Optional<ComponentVernierEngine> readFromNBT(NBTTagCompound compound) {
        if (compound.getString("type").isEmpty() || compound.getString("name").isEmpty()) {
            return Optional.empty();
        }
        ComponentVernierEngine engine = new ComponentVernierEngine();
        if (!compound.hasKey("mass", Constants.NBT.TAG_DOUBLE))
            return Optional.empty();
        if (!compound.hasKey("radius", Constants.NBT.TAG_DOUBLE))
            return Optional.empty();
        if (!compound.hasKey("area_ratio", Constants.NBT.TAG_DOUBLE))
            return Optional.empty();
        if (!compound.hasKey("materials", Constants.NBT.TAG_LIST))
            return Optional.empty();
        if (!compound.hasKey("throughput", Constants.NBT.TAG_DOUBLE))
            return Optional.empty();
        compound.getTagList("materials", Constants.NBT.TAG_COMPOUND)
                .forEach(x -> engine.materials.add(MaterialCost.fromNBT((NBTTagCompound) x)));

        engine.areaRatio = compound.getDouble("area_ratio");
        engine.radius = compound.getDouble("radius");
        engine.mass = compound.getDouble("mass");
        engine.fuelThroughput = compound.getDouble("throughput");
        // not required: cards written before the nozzle got a flow model have none, and
        // read back as a nozzle that is neither rewarded nor punished for its shape
        engine.chamberPressure = compound.getDouble("chamber_pressure");
        engine.exitHalfAngle = compound.hasKey("exit_angle", Constants.NBT.TAG_DOUBLE) ?
                compound.getDouble("exit_angle") : NozzleFlow.REFERENCE_EXIT_HALF_ANGLE;
        engine.wettedAreaRatio = compound.hasKey("wetted_ratio", Constants.NBT.TAG_DOUBLE) ?
                compound.getDouble("wetted_ratio") : NozzleFlow.REFERENCE_WETTED_AREA_RATIO;
        engine.contourTurning = compound.hasKey("turning", Constants.NBT.TAG_DOUBLE) ?
                compound.getDouble("turning") : NozzleFlow.REFERENCE_CONTOUR_TURNING;

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
        ArrayList<Double> wallRadii = new ArrayList<>();
        double throatRadius = 0;
        AxisAlignedBB nozzleBB = analysis.getBB(nozzle);
        for (int i = (int) nozzleBB.maxY - 1; i >= (int) nozzleBB.minY; i--) {
            Set<BlockPos> airLayer = analysis.getLayerAir(nozzleBB, i);
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
            double welzlRadius = analysis.getRadius(airLayer);
            if (areas.isEmpty()) {
                // topmost layer, right under the chamber: the throat that chokes the flow
                throatRadius = welzlRadius;
            }
            wallRadii.add(NozzleFlow.wallRadius(welzlRadius));
            areas.add(airLayer.size() + airPerimeter.size() / 2);
        }

        // For vernier engine, 1 air layer minimum
        // length under that assumption.
        if (areas.size() < 1 || areas.get(0) > 5) {
            analysis.status = BuildStat.NOZZLE_MALFORMED;
            return Optional.empty();
        }

        int initial = areas.get(0);
        int fin = initial;

        /*
         * for (int a : areas) { if (fin <= a) { fin = a; } else { analysis.status =
         * BuildStat.NOT_LAVAL; return Optional.empty(); } }
         */
        float computedAreaRatio = ((float) fin) / initial;
        if (computedAreaRatio < 1) {
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
                .getOfBlockType(analysis.getBlockNeighbors(cChamber, StructAnalysis.orthVecs), SuSyBlocks.TURBOPUMP)
                .collect(Collectors.toSet());
        if (nozzleBB.contains(new Vec3d(cChamber))) {
            analysis.status = BuildStat.C_CHAMBER_INSIDE;
            return Optional.empty();
        }
        if (!analysis.world.isAirBlock(cChamber.add(0, -1, 0))) {
            analysis.status = BuildStat.NOZZLE_MALFORMED;
            return analysis.errorPos(cChamber.add(0, -1, 0));
        }
        // Analyze turbopumps
        IBlockState chamberState = analysis.world.getBlockState(cChamber);
        int pumpNum = ((BlockCombustionChamber.CombustionType) (((VariantBlock<?>) chamberState.getBlock())
                .getState(chamberState))).getMinPumps();

        if (!((VariantBlock<?>) chamberState.getBlock()).getState(chamberState)
                .equals(BlockCombustionChamber.CombustionType.MONOPROPELLANT)) {
            analysis.status = BuildStat.WRONG_CHAMBER_TYPE;
            return Optional.empty();
        }

        if (pumps.size() < pumpNum) {
            analysis.status = BuildStat.WRONG_NUM_PUMPS;
            return Optional.empty();
        }
        for (BlockPos pumpPos : pumps) {
            EnumFacing dir = analysis.world.getBlockState(pumpPos).getValue(FACING);
            if (!dir.equals(EnumFacing.DOWN) && !pumpPos.add(dir.getOpposite().getDirectionVec()).equals(cChamber)) {
                analysis.status = BuildStat.WEIRD_PUMP;
                return analysis.errorPos(pumpPos);
            }
        }

        // Analyzes match
        Set<BlockPos> stickBlocks = analysis.getOfMaterial(blocks, Materials.Wood).collect(Collectors.toSet());
        if (!stickBlocks.isEmpty()) {
            for (BlockPos stickPos : stickBlocks) {
                if (!nozzleBB.contains(new Vec3d(stickPos))) {
                    analysis.status = BuildStat.MATCH_WRONG;
                    return Optional.empty();
                }
            }
        }

        // Creates engine
        Set<BlockPos> engineBlocks = new HashSet<>(nozzle);
        engineBlocks.addAll(pumps);
        engineBlocks.add(cChamber);
        engineBlocks.addAll(analysis.getOfBlockType(blocks, SuSyBlocks.INTERSTAGE).collect(Collectors.toSet()));
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
        this.radius = analysis
                .getRadius(blocks.stream().filter(bp -> bp.getY() == nozzleBB.maxY).collect(Collectors.toSet()));
        tag.setDouble("radius", radius);

        collectInfo(analysis, blocks, tag);

        double pumpThroughput = 0;

        for (BlockPos pumpPos : pumps) {
            IBlockState pump = analysis.world.getBlockState(pumpPos);
            pumpThroughput += (SuSyBlocks.TURBOPUMP.getState(pump)).getThroughput();
        }

        double throatArea = NozzleFlow.crossSectionArea(throatRadius);
        this.chamberPressure = NozzleFlow.equilibriumChamberPressure(throatArea, pumpThroughput);
        this.fuelThroughput = NozzleFlow.chokedMassFlow(throatArea, this.chamberPressure);
        tag.setDouble("throughput", fuelThroughput);
        tag.setDouble("chamber_pressure", chamberPressure);

        // the contour itself is thrown away here; only what the thrust calculation
        // needs later survives the scan
        double[] contour = wallRadii.stream().mapToDouble(Double::doubleValue).toArray();
        this.exitHalfAngle = NozzleFlow.contourExitHalfAngle(contour);
        this.wettedAreaRatio = NozzleFlow.wettedAreaRatio(contour, throatArea);
        this.contourTurning = NozzleFlow.contourTurning(contour);
        tag.setDouble("exit_angle", exitHalfAngle);
        tag.setDouble("wetted_ratio", wettedAreaRatio);
        tag.setDouble("turning", contourTurning);

        tag.setBoolean("has_match", !stickBlocks.isEmpty());

        writeBlocksToNBT(blocks, analysis.world);
        return Optional.of(tag);
    }

    @Override
    public double getFuelThroughput() {
        return fuelThroughput;
    }

    @Override
    public double getAreaRatio() {
        return areaRatio;
    }

    @Override
    public double getChamberPressure() {
        return chamberPressure;
    }

    @Override
    public double getExitHalfAngle() {
        return exitHalfAngle;
    }

    @Override
    public double getWettedAreaRatio() {
        return wettedAreaRatio;
    }

    @Override
    public double getContourTurning() {
        return contourTurning;
    }

    @Override
    public boolean configureDefaults() {
        this.materials.add(new MaterialCost(new ItemStack(Items.DIAMOND), MaterialCost.SourceType.ITEM, 1));

        this.radius = 1.0;
        this.areaRatio = 1.0;
        this.fuelThroughput = 50.0;
        this.chamberPressure = NozzleFlow.chamberPressureFor(NozzleFlow.crossSectionArea(0), this.fuelThroughput);
        this.exitHalfAngle = NozzleFlow.REFERENCE_EXIT_HALF_ANGLE;
        this.wettedAreaRatio = NozzleFlow.REFERENCE_WETTED_AREA_RATIO;
        this.contourTurning = NozzleFlow.REFERENCE_CONTOUR_TURNING;
        this.mass = 550.0;
        return true;
    }

    @Override
    public List<String> getTooltipLines(NBTTagCompound tag) {
        List<String> lines = super.getTooltipLines(tag);
        if (tag.hasKey("area_ratio")) {
            lines.add(SuSyUtility.formatDouble("susy.rocketry.tooltip.area_ratio", "%.2f", tag.getDouble("area_ratio")));
        }
        if (tag.hasKey("throughput")) {
            lines.add(SuSyUtility.formatDouble("susy.rocketry.tooltip.throughput", "%.2f", tag.getDouble("throughput")));
        }
        if (tag.hasKey("chamber_pressure")) {
            lines.add(SuSyUtility.formatDouble("susy.rocketry.tooltip.chamber_pressure", "%.2f",
                    tag.getDouble("chamber_pressure") / 1e6));
        }
        if (tag.hasKey("exit_angle") && tag.hasKey("wetted_ratio") && tag.hasKey("turning")) {
            lines.add(SuSyUtility.formatDouble("susy.rocketry.tooltip.exit_angle", "%.2f",
                    Math.toDegrees(tag.getDouble("exit_angle"))));
            lines.add(SuSyUtility.formatDouble("susy.rocketry.tooltip.contour_efficiency", "%.2f",
                    100 * NozzleFlow.contourEfficiency(tag.getDouble("exit_angle"),
                            tag.getDouble("wetted_ratio"), tag.getDouble("turning"))));
        }
        return lines;
    }
}
