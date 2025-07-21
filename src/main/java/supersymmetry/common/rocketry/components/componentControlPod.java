package supersymmetry.common.rocketry.components;

import gregtech.api.block.VariantBlock;
import gregtech.api.capability.*;
import gregtech.api.gui.widgets.*;
import java.util.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.rocketry.*;
import supersymmetry.common.tile.TileEntityCoverable;

public class componentControlPod extends AbstractComponent<componentControlPod> {
  // } else if (blockList.stream().anyMatch(controlPodDetect) && hasAir) {
  //            analyzeSpacecraft(blocksConnected, exterior.getFirst(), exterior.getSecond());
  public double radius;
  public double mass;

  public componentControlPod() {
    super(
        "spacecraft",
        "spacecraft",
        thing -> {
          return false;
        });
    this.setDetectionPredicate(componentControlPod::detect);
  }

  private static boolean detect(Tuple<StructAnalysis, List<BlockPos>> input) {
    AxisAlignedBB aabb = input.getFirst().getBB(input.getSecond());

    Set<BlockPos> blocks =
        input
            .getFirst()
            .getBlockConn(
                aabb, input.getFirst().getBlocks(input.getFirst().world, aabb, true).get(0));
    input
        .getFirst()
        .checkHull(
            aabb, blocks, false); // for some reason this is the thing that sets BuildStat status to
    // HULL_FULL

    boolean hasAir = input.getFirst().status != BuildStat.HULL_FULL;
    boolean has_the_block =
        input.getSecond().stream()
            .anyMatch(
                bp ->
                    input
                        .getFirst()
                        .world
                        .getBlockState(bp)
                        .getBlock()
                        .equals(SuSyBlocks.ROCKET_CONTROL));

    return hasAir && has_the_block;
  }

  @Override
  public Optional<NBTTagCompound> analyzePattern(StructAnalysis analysis, AxisAlignedBB aabb) {
    Set<BlockPos> blocksConnected =
        analysis.getBlockConn(aabb, analysis.getBlocks(analysis.world, aabb, true).get(0));
    Tuple<Set<BlockPos>, Set<BlockPos>> hullCheck =
        analysis.checkHull(aabb, blocksConnected, false);
    Set<BlockPos> exterior = hullCheck.getFirst();
    Set<BlockPos> interior = hullCheck.getSecond();
    return this.spacecraftPattern(blocksConnected, interior, exterior, analysis);
  } /*because it used the same thing with swapped inputs in the original code ;c */

  public Optional<NBTTagCompound> spacecraftPattern(
      Set<BlockPos> blocksConnected,
      Set<BlockPos> interior,
      Set<BlockPos> exterior,
      StructAnalysis analysis) {

    Predicate<BlockPos> lifeSupportCheck =
        bp -> analysis.world.getBlockState(bp).getBlock().equals(SuSyBlocks.LIFE_SUPPORT);
    Set<BlockPos> lifeSupports =
        blocksConnected.stream().filter(lifeSupportCheck).collect(Collectors.toSet());
    NBTTagCompound tag = new NBTTagCompound();

    lifeSupports.forEach(
        bp -> {
          Block block = analysis.world.getBlockState(bp).getBlock();
          NBTTagCompound list = tag.getCompoundTag("parts");
          String part =
              ((VariantBlock<?>) block).getState(analysis.world.getBlockState(bp)).toString();
          int num = list.getInteger(part); // default behavior is 0
          list.setInteger(part, num + 1);
          tag.setTag("parts", list);
        });

    for (BlockPos bp : exterior) {
      if (analysis.world.getBlockState(bp).getBlock().equals(SuSyBlocks.SPACECRAFT_HULL)) {
        TileEntityCoverable te = (TileEntityCoverable) analysis.world.getTileEntity(bp);
        for (EnumFacing side : EnumFacing.VALUES) {
          // either it must be facing the outside without a cover or it must have
          if (te.isCovered(side) ^ exterior.contains(bp.add(side.getDirectionVec()))) {
            analysis.status = BuildStat.HULL_WEAK;
            return Optional.empty();
          }
        }
      } else if (analysis.world.getBlockState(bp).getBlock().equals(SuSyBlocks.SPACE_INSTRUMENT)) {
        {
          Block block = analysis.world.getBlockState(bp).getBlock();
          NBTTagCompound list = tag.getCompoundTag("instruments");
          String part =
              ((VariantBlock<?>) block).getState(analysis.world.getBlockState(bp)).toString();
          int num = list.getInteger(part); // default behavior is 0
          list.setInteger(part, num + 1);
          tag.setTag("parts", list);
        }
      } else {
        analysis.status = BuildStat.HULL_WEAK;
      }
    }

    if (lifeSupports.isEmpty()) {
      // no airspace necessary
      if (!interior.isEmpty()) {
        analysis.status = BuildStat.SPACECRAFT_HOLLOW;
        return Optional.empty();
      }
      tag.setBoolean("hasAir", false);
    } else {
      int volume = interior.size();
      tag.setInteger("volume", volume);
      Set<BlockPos> container = analysis.getPerimeter(interior, StructAnalysis.orthVecs);
      for (BlockPos bp : container) {
        Block block = analysis.world.getBlockState(bp).getBlock();
        if (block.equals(SuSyBlocks.LIFE_SUPPORT)) {
          continue; // we did the math on this earlier
        } else if (analysis.world.getTileEntity(bp) != null) {
          if (analysis.world.getTileEntity(bp) instanceof TileEntityCoverable) {
            TileEntityCoverable te = (TileEntityCoverable) analysis.world.getTileEntity(bp);
            if (block.equals(SuSyBlocks.ROOM_PADDING)) {
              for (EnumFacing side : EnumFacing.VALUES) {
                if (te.isCovered(side) ^ interior.contains(bp.add(side.getDirectionVec()))) {
                  analysis.status = BuildStat.WEIRD_PADDING;
                  return Optional.empty();
                }
              }
            }
          }
        }
      }
      tag.setBoolean("hasAir", true);
    }
    double radius = analysis.getApproximateRadius(blocksConnected);

    // The scan is successful by this point
    analysis.status = BuildStat.SUCCESS;
    tag.setString("type", this.type);
    tag.setString("name", this.name);
    tag.setDouble("radius", (radius));
    double mass = 0;
    for (BlockPos block : blocksConnected) {
      mass += getMass(analysis.world.getBlockState(block));
    }
    tag.setDouble("mass", mass);

    writeBlocksToNBT(blocksConnected, analysis.world, tag);
    return Optional.of(tag);
  }

  @Override
  public Optional<componentControlPod> readFromNBT(NBTTagCompound compound) {
    if (compound.getString("type") != this.type) return Optional.empty();
    componentControlPod controlpod = new componentControlPod();
    if (compound.hasKey("mass", Constants.NBT.TAG_DOUBLE)) {
      controlpod.mass = compound.getDouble("mass");
    } else {
      return Optional.empty();
    }
    if (compound.hasKey("radius", Constants.NBT.TAG_DOUBLE)) {
      controlpod.radius = compound.getDouble("radius");
    } else {
      return Optional.empty();
    }
    return Optional.of(controlpod);
  }
}
