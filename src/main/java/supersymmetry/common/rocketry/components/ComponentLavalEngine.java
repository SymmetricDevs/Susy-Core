package supersymmetry.common.rocketry.components;

import static supersymmetry.api.blocks.VariantDirectionalRotatableBlock.FACING;

import gregtech.api.block.VariantBlock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.components.MaterialCost;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.rocketry.BlockCombustionChamber;
import supersymmetry.common.blocks.rocketry.BlockTurboPump;

public class ComponentLavalEngine extends AbstractComponent<ComponentLavalEngine> {

  public double radius;
  public double area_ratio;
  public double fuel_throughput;

  public ComponentLavalEngine() {
    super(
        "laval_engine",
        "engine",
        t -> {
          return t.getSecond().stream()
              .anyMatch(
                  bp ->
                      t.getFirst()
                          .world
                          .getBlockState(bp)
                          .getBlock()
                          .equals(SuSyBlocks.COMBUSTION_CHAMBER));
        });
    this.setComponentSlotValidator(
        x ->
            x.equals(this.getName())
                || x.equals(this.getType())
                || (x.equals(this.getType() + "_small") && this.radius < 3)
                || (x.equals(this.getName() + "_small") && this.radius < 3));
  }

  @Override
  public void writeToNBT(NBTTagCompound tag) {
    super.writeToNBT(tag);
    tag.setDouble("radius", this.radius);
    tag.setDouble("area_ratio", this.area_ratio);
    tag.setDouble("throughput", this.fuel_throughput);
  }

  @Override
  public Optional<ComponentLavalEngine> readFromNBT(NBTTagCompound compound) {
    if (compound.getString("type") != this.type || compound.getString("name") != this.name)
      Optional.empty();
    ComponentLavalEngine engine = new ComponentLavalEngine();
    if (!compound.hasKey("mass", Constants.NBT.TAG_DOUBLE)) return Optional.empty();
    if (!compound.hasKey("radius", Constants.NBT.TAG_DOUBLE)) return Optional.empty();
    if (!compound.hasKey("area_ratio", Constants.NBT.TAG_DOUBLE)) return Optional.empty();
    if (!compound.hasKey("materials", Constants.NBT.TAG_LIST)) return Optional.empty();
    compound
        .getTagList("materials", Constants.NBT.TAG_COMPOUND)
        .forEach(x -> engine.materials.add(MaterialCost.fromNBT((NBTTagCompound) x)));

    engine.area_ratio = compound.getDouble("area_ratio");
    engine.radius = compound.getDouble("radius");
    engine.mass = compound.getDouble("mass");
    return Optional.of(engine);
  }

  @Override
  public Optional<NBTTagCompound> analyzePattern(StructAnalysis analysis, AxisAlignedBB aabb) {

    Set<BlockPos> blocks =
        analysis.getBlockConn(aabb, analysis.getBlocks(analysis.world, aabb, true).get(0));
    Set<BlockPos> nozzle =
        analysis.getOfBlockType(blocks, SuSyBlocks.ROCKET_NOZZLE).collect(Collectors.toSet());
    if (nozzle.isEmpty()) {
      analysis.status = BuildStat.NO_NOZZLE;
      return Optional.empty();
    }
    ArrayList<Integer> areas = new ArrayList<>();
    AxisAlignedBB nozzleBB = analysis.getBB(nozzle);
    for (int i = (int) nozzleBB.maxY - 1; i >= (int) nozzleBB.minY; i--) {
      Set<BlockPos> airLayer = analysis.getLayerAir(nozzleBB, i);
      if (airLayer == null) { // there should be an error here
        analysis.status = BuildStat.NOZZLE_MALFORMED;
        return Optional.empty();
      }
      Set<BlockPos> airPerimeter = analysis.getPerimeter(airLayer, StructAnalysis.layerVecs);
      if ((double) airPerimeter.size()
          < 3 * Math.sqrt((double) airLayer.size())) { // Establishes a roughly circular pattern
        analysis.status = BuildStat.NOZZLE_MALFORMED;
        return Optional.empty();
      }
      areas.add(airLayer.size() + airPerimeter.size() / 2);
    }

    // For all rocket nozzles, the air layer list should be increasing. 3 blocks should be a minimum
    // length under that assumption.
    if (areas.size() < 3 || areas.get(0) > 5) {
      analysis.status = BuildStat.NOZZLE_MALFORMED;
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
    float area_ratio = ((float) fin) / initial;
    if (area_ratio < 1.5) {
      analysis.status = BuildStat.NOT_LAVAL;
    }

    // One combustion chamber is, I think, reasonable
    List<BlockPos> cChambers =
        analysis.getOfBlockType(blocks, SuSyBlocks.COMBUSTION_CHAMBER).collect(Collectors.toList());
    if (cChambers.size() != 1) {
      analysis.status = BuildStat.WRONG_NUM_C_CHAMBERS;
      return Optional.empty();
    }
    // Below the chamber: Open space
    BlockPos cChamber = cChambers.get(0);
    Set<BlockPos> pumps =
        analysis
            .getOfBlockType(
                analysis.getBlockNeighbors(cChamber, StructAnalysis.orthVecs), SuSyBlocks.TURBOPUMP)
            .collect(Collectors.toSet());
    if (nozzleBB.contains(new Vec3d(cChamber))) {
      analysis.status = BuildStat.C_CHAMBER_INSIDE;
      return Optional.empty();
    }
    if (!analysis.world.isAirBlock(cChamber.add(0, -1, 0))) {
      analysis.status = BuildStat.NOZZLE_MALFORMED;
      return Optional.empty();
    }
    // Analyze turbopumps
    IBlockState chamberState = analysis.world.getBlockState(cChamber);
    int pumpNum =
        ((BlockCombustionChamber.CombustionType)
                (((VariantBlock<?>) chamberState.getBlock()).getState(chamberState)))
            .getMinPumps();
    if (pumps.size() < pumpNum) {
      analysis.status = BuildStat.WRONG_NUM_PUMPS;
      return Optional.empty();
    }
    for (BlockPos pumpPos : pumps) {
      EnumFacing dir = analysis.world.getBlockState(pumpPos).getValue(FACING);
      if (!dir.equals(EnumFacing.DOWN)
          && !pumpPos.add(dir.getOpposite().getDirectionVec()).equals(cChamber)) {
        analysis.status = BuildStat.WEIRD_PUMP;
        return Optional.empty();
      }

    }
    // Creates engine
    Set<BlockPos> engineBlocks = new HashSet<>(nozzle);
    engineBlocks.addAll(pumps);
    engineBlocks.add(cChamber);
    engineBlocks.addAll(
        analysis.getOfBlockType(blocks, SuSyBlocks.INTERSTAGE).collect(Collectors.toSet()));
    if (engineBlocks.size() < blocks.size()) {
      analysis.status = BuildStat.EXTRANEOUS_BLOCKS;
      return Optional.empty();
    }
    analysis.status = BuildStat.SUCCESS;
    // currently a double
    NBTTagCompound tag = new NBTTagCompound();
    tag.setDouble("area_ratio", area_ratio);
    this.area_ratio = area_ratio;
    tag.setString("type", this.type);
    tag.setString("name", this.name);
    double mass = 0;
    for (BlockPos block : blocks) {
      mass += getMass(analysis.world.getBlockState(block));
    }
    double innerRadius =
        analysis.getApproximateRadius(
            blocks.stream().filter(bp -> bp.getY() == nozzleBB.maxY).collect(Collectors.toSet()));
    tag.setDouble("radius", Double.valueOf(innerRadius));
    this.radius = innerRadius;
    tag.setDouble("mass", Double.valueOf(mass));
    this.mass = mass;

    double throughput = 0;

    for (BlockPos pumpPos : pumps) {
      IBlockState pump = analysis.world.getBlockState(pumpPos);
      throughput += ((BlockTurboPump.HPPType)
              (((VariantBlock<?>)pump)).getState(pump)).getThroughput();
    }

    this.fuel_throughput = throughput;
    tag.setDouble("throughput", fuel_throughput);

    writeBlocksToNBT(blocks, analysis.world, tag);
    return Optional.of(tag);
  }
}
