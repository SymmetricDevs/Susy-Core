package supersymmetry.common.rocketry.components;

import gregtech.api.block.VariantBlock;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.tile.TileEntityCoverable;

public class ComponentControlPod extends AbstractComponent<ComponentControlPod> {
  public double radius;
  public Map<String, Integer> parts = new HashMap<>();
  public Map<String, Integer> instruments = new HashMap<>();
  public boolean hasAir;
  public double volume;

  public ComponentControlPod() {
    super(
        "spacecraft_control_pod",
        "spacecraft_control_pod",
        thing -> {
          return false;
        });
    this.setDetectionPredicate(ComponentControlPod::detect);
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
    return spacecraftPattern(blocksConnected, interior, exterior, analysis);
  }

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
          this.parts.put(part, num + 1);
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
          this.instruments.put(part, num + 1);
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
      this.hasAir = false; // goog..?
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
      this.hasAir = true;
    }
    double radius = analysis.getApproximateRadius(blocksConnected);

    // The scan is successful by this point
    analysis.status = BuildStat.SUCCESS;
    tag.setString("type", type);
    tag.setString("name", name);
    tag.setDouble("radius", (radius));
    this.radius = radius;
    double mass = 0;
    for (BlockPos block : blocksConnected) {
      mass += getMass(analysis.world.getBlockState(block));
    }
    tag.setDouble("mass", mass);
    this.mass = mass;
    writeBlocksToNBT(blocksConnected, analysis.world, tag);
    return Optional.of(tag);
  }

  @Override
  public void writeToNBT(NBTTagCompound tag) {
    tag.setString("name", this.name);
    tag.setString("type", this.type);
    tag.setDouble("radius", this.radius);
    tag.setDouble("volume", this.volume);
    tag.setBoolean("hasAir", this.hasAir);
    NBTTagCompound instruments = new NBTTagCompound();
    NBTTagCompound parts = new NBTTagCompound();
    for (var part : this.parts.entrySet()) {
      parts.setInteger(part.getKey(), part.getValue());
    }
    for (var instrument : this.instruments.entrySet()) {
      instruments.setInteger(instrument.getKey(), instrument.getValue());
    }
    tag.setTag("instruments", instruments);
    tag.setTag("tools", parts);
  }

  @Override
  public Optional<ComponentControlPod> readFromNBT(NBTTagCompound compound) {
    ComponentControlPod controlpod = new ComponentControlPod();

    if (!compound.getString("name").equals(controlpod.name)) return Optional.empty();
    if (!compound.getString("type").equals(controlpod.type)) return Optional.empty();
    if (!compound.hasKey("radius", NBT.TAG_DOUBLE)) return Optional.empty();
    if (!compound.hasKey("mass", NBT.TAG_DOUBLE)) return Optional.empty();
    if (!compound.hasKey("hasAir")) return Optional.empty();
    if (!compound.hasKey("volume", NBT.TAG_DOUBLE)) return Optional.empty();
    if (!compound.hasKey("parts", NBT.TAG_COMPOUND)) return Optional.empty();
    if (!compound.hasKey("instruments", NBT.TAG_COMPOUND)) return Optional.empty();

    controlpod.radius = compound.getDouble("radius");
    controlpod.mass = compound.getDouble("mass");
    controlpod.volume = compound.getDouble("volume");
    controlpod.hasAir = compound.getBoolean("hasAir");

    NBTTagCompound instrumentsList = compound.getCompoundTag("instruments");
    for (String key : instrumentsList.getKeySet()) {
      controlpod.instruments.put(key, compound.getInteger(key));
    }

    NBTTagCompound partsList = compound.getCompoundTag("parts");
    for (String key : partsList.getKeySet()) {
      controlpod.parts.put(key, compound.getInteger(key));
    }

    return Optional.of(controlpod);
  }
}
