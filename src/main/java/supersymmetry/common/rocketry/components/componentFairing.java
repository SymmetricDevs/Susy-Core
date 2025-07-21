package supersymmetry.common.rocketry.components;

import static supersymmetry.api.blocks.VariantDirectionalRotatableBlock.FACING;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.common.blocks.SuSyBlocks;

/** componentFairing */
public class componentFairing extends AbstractComponent<componentFairing> {
  public int height;
  public double bottom_radius;

  public componentFairing() {
    super(
        "alu_fairing",
        "fairing",
        t -> {
          return t.getSecond().stream()
              .anyMatch(
                  bp ->
                      t.getFirst()
                          .world
                          .getBlockState(bp)
                          .getBlock()
                          .equals(SuSyBlocks.FAIRING_HULL));
        });
  }

  @Override
  public Optional<componentFairing> readFromNBT(NBTTagCompound compound) {
    if (compound.getString("type") != this.type) return Optional.empty();
    componentFairing fairing = new componentFairing();
    if (compound.hasKey("height", Constants.NBT.TAG_INT)) {
      fairing.height = compound.getInteger("height");
    } else {
      return Optional.empty();
    }
    if (compound.hasKey("bottom_radius", Constants.NBT.TAG_DOUBLE)) {
      fairing.bottom_radius = compound.getDouble("bottom_radius");
    } else {
      return Optional.empty();
    }
    return Optional.of(fairing);
  }

  @Override
  public Optional<NBTTagCompound> analyzePattern(StructAnalysis analysis, AxisAlignedBB aabb) {
    // the lsp format breaks it a lot :c
    if (analysis
        .checkHull(
            aabb,
            analysis.getBlockConn(aabb, analysis.getBlocks(analysis.world, aabb, true).get(0)),
            false)
        .getSecond()
        .isEmpty()) {
      analysis.status = BuildStat.WEIRD_FAIRING;
      return Optional.empty();
    }
    Set<BlockPos> blocks =
        analysis.getBlockConn(aabb, analysis.getBlocks(analysis.world, aabb, true).get(0));

    AxisAlignedBB fairingBB = analysis.getBB(blocks);
    Predicate<BlockPos> connCheck =
        bp -> analysis.world.getBlockState(bp).getBlock().equals(SuSyBlocks.FAIRING_CONNECTOR);
    Set<BlockPos> connectorBlocks = blocks.stream().filter(connCheck).collect(Collectors.toSet());
    // These connector blocks should form a ring, with their primary directions not facing any other
    // block
    // Each connector should neighbor two other connectors, and we pick one to start the check
    BlockPos next = connectorBlocks.iterator().next();
    BlockPos start = next;

    // This will keep track of what we've covered
    Set<BlockPos> collectedConnectors = new HashSet<>(Collections.singleton(next));

    Set<BlockPos> neighbors1 =
        analysis.getBlockNeighbors(next).stream().filter(connCheck).collect(Collectors.toSet());
    Set<BlockPos> initialClosest = analysis.getClosest(next, neighbors1);
    if (initialClosest.size() <= 2
        && !initialClosest.isEmpty()
        && analysis.isFacingOutwards(next)) {
      next = initialClosest.iterator().next(); // This may be random, but it doesn't matter
      collectedConnectors.add(next);
    } else {
      analysis.status = BuildStat.WEIRD_FAIRING;
      return Optional.empty();
    }

    // Step 2: Verify that all other blocks are connected by checking the partition
    // If there are more than 2 partitions of air in the bounding box, then there's an abnormality
    // with the hull shape.
    // If there's only one partition, then the hull has a hole in it.
    // We know already that the exterior loop is touching the edge of the bounding box.
    // So, if there are 2 partitions, then the fairing hull is of the desired shape.

    List<HashSet<BlockPos>> partitions = analysis.getPartitions(fairingBB);
    if (partitions.size() != 2) {
      analysis.status = BuildStat.WEIRD_FAIRING;
      return Optional.empty();
    }

    // Objective: We don't really want tiles on the inside of the fairing - that wouldn't make any
    // sense!
    // So, we're going to obtain the fairing with a ceiling (if it didn't have one, it wouldn't have
    // )
    // Note that
    Set<BlockPos> interiorPartition;
    if (analysis.getBB(partitions.get(0)).maxY > analysis.getBB(partitions.get(1)).maxY) {
      interiorPartition = partitions.get(1);
    } else {
      interiorPartition = partitions.get(0);
    }

    while (!collectedConnectors.containsAll(connectorBlocks)) {
      Set<BlockPos> neighbors =
          analysis.getBlockNeighbors(next).stream()
              .filter(connCheck.and(o -> !collectedConnectors.contains(o)))
              .collect(Collectors.toSet());
      Set<BlockPos> closestNeighbors = analysis.getClosest(next, neighbors);
      if (closestNeighbors.size() == 1) { // only one neighbor has not been checked yet
        next = closestNeighbors.iterator().next();
        collectedConnectors.add(next);
        // get direction of block
        EnumFacing facing = analysis.world.getBlockState(next).getValue(FACING);
        // The fairing connector should have its connector face pointing out
        BlockPos pointingTo = next.add(facing.getDirectionVec());
        if (StructAnalysis.blockCont(fairingBB, pointingTo)
            || partitions.get(0).contains(pointingTo)
            || partitions.get(1).contains(pointingTo)) {
          analysis.status = BuildStat.WEIRD_FAIRING;
          return Optional.empty();
        }
      } else if (closestNeighbors.isEmpty()) {
        if (!collectedConnectors.contains(connectorBlocks)
            || !analysis.getBlockNeighbors(next).contains(start)) {
          analysis.status = BuildStat.WEIRD_FAIRING;
          return Optional.empty();
        }
        break;
      } else {
        analysis.status = BuildStat.WEIRD_FAIRING;
        return Optional.empty();
      }
    }
    NBTTagCompound tag = new NBTTagCompound();

    tag.setString("type", this.type);
    tag.setString("name", this.name);
    tag.setInteger("height", (int) fairingBB.maxY - (int) fairingBB.minY);
    this.height = (int) fairingBB.maxY - (int) fairingBB.minY;
    Double bottomRadius = analysis.getApproximateRadius(analysis.getLowestLayer(blocks));
    this.bottom_radius = bottomRadius;
    tag.setDouble("bottom_radius", bottomRadius);

    analysis.status = BuildStat.SUCCESS;

    writeBlocksToNBT(blocks, analysis.world, tag);
    return Optional.of(tag);
  }
}
