package supersymmetry.api.rocketry.components;

import static supersymmetry.common.blocks.SuSyBlocks.TANK_SHELL;

import gregtech.api.block.VariantBlock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.api.util.StructAnalysis.BuildStat;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.rocketry.BlockCombustionChamber;
import supersymmetry.common.blocks.rocketry.BlockRocketNozzle;
import supersymmetry.common.blocks.rocketry.BlockTankShell;
import supersymmetry.common.blocks.rocketry.BlockTankShell1;
import supersymmetry.common.blocks.rocketry.BlockTurboPump;
import supersymmetry.common.tile.TileEntityCoverable;

public abstract class AbstractComponent<T extends AbstractComponent<T>> {
  protected Predicate<Tuple<StructAnalysis, List<BlockPos>>> detectionPredicate;
  private static final Set<AbstractComponent<?>> registry = new HashSet<>();
  private static boolean registrylock = false;
  private static final Map<String, Class<? extends AbstractComponent<?>>> nameToComponentRegistry =
      new HashMap<>();
  protected String name;
  // ex name="laval_engine", type="engine" so that you can do some silly things with engine types
  protected String type;
  protected BuildStat status = BuildStat.ERROR;

  public AbstractComponent(
      String name,
      String type,
      Predicate<Tuple<StructAnalysis, List<BlockPos>>> detectionPredicate) {
    this.detectionPredicate = detectionPredicate;
    this.name = name;
    this.type = type;
    // if (!registrylock) {
    //   registry.add(this);
    //   nameToComponentRegistry.put(name, this.getClass());
    // }
  }

  public String getName() {
    return this.name;
  }

  public String getType() {
    return this.type;
  }

  public static Map<String, Class<? extends AbstractComponent<?>>> getNameRegistry() {
    return Map.copyOf(nameToComponentRegistry);
  }

  @SuppressWarnings("unchecked")
  // probably fine because if you manage to put in an AbstractComponent<?> and it doesnt
  // extend from AbstractComponent<?> you deserved to get a crash
  public static void registerComponent(AbstractComponent<?> component) {
    if (getRegistryLock()) {
      nameToComponentRegistry.put(
          component.getName(), (Class<? extends AbstractComponent<?>>) component.getClass());
      if (registry.stream().noneMatch(x -> x.getName() == component.getName())) {
        registry.add(component);
      }
    } else {
      throw new IllegalStateException(
          "tried to register a component after the registry was closed. dumbass.");
    }
  }

  public static void lockRegistry() {
    registrylock = true;
  }

  public static boolean getRegistryLock() {
    return registrylock;
  }

  public static List<AbstractComponent<?>> getRegistry() {
    return List.copyOf(registry);
  }

  public void setDetectionPredicate(Predicate<Tuple<StructAnalysis, List<BlockPos>>> predicate) {
    this.detectionPredicate = predicate;
  }

  public abstract Optional<NBTTagCompound> analyzePattern(
      StructAnalysis analysis, AxisAlignedBB aabb);

  public abstract Optional<T> readFromNBT(NBTTagCompound compound);

  public static void writeBlocksToNBT(
      Set<BlockPos> blocks, World world, NBTTagCompound mutableTagCompound) {
    Map<String, Integer> counts = new HashMap<String, Integer>();
    for (BlockPos blockpos : blocks) {
      IBlockState state = world.getBlockState(blockpos);
      Block block = state.getBlock();
      int meta = block.getMetaFromState(state);
      TileEntity te = world.getTileEntity(blockpos);
      if (te != null) {
        if (te instanceof TileEntityCoverable) {
          TileEntityCoverable teCoverable = (TileEntityCoverable) te;
          if (teCoverable != null
              && teCoverable.getCoverType().getItem().getRegistryName()
                  != Items.AIR.getRegistryName()) {
            String key =
                teCoverable.getCoverType().getItem().getRegistryName().toString()
                    + "#"
                    + teCoverable.getCoverType().getMetadata()
                    + "#cover"; // i am sorry for this
            counts.put(key, counts.getOrDefault(key, 0) + 1);
          }
        }
      }
      String key =
          block.getRegistryName().toString()
              + "#"
              + meta
              + "#block"; // i am sorry for this, but it kinda works
      counts.put(key, counts.getOrDefault(key, 0) + 1);
    }

    NBTTagCompound root = new NBTTagCompound();
    NBTTagList list = new NBTTagList();
    for (Map.Entry<String, Integer> e : counts.entrySet()) {
      String[] p = e.getKey().split("#", 3);
      NBTTagCompound c = new NBTTagCompound();
      c.setString("registryName", p[0]);
      c.setInteger("meta", Integer.parseInt(p[1]));
      c.setString("type", p[2]);
      c.setInteger("count", e.getValue());
      list.appendTag(c);
    }

    root.setTag("blockCounts", list);
    mutableTagCompound.setTag("structureInfo", root);
  }

  public static double getMass(IBlockState state) {
    Block block = state.getBlock();
    if (!(block instanceof VariantBlock<?>)) {
      return 50.0;
    }
    Enum<?> variant = ((VariantBlock<?>) block).getState(state);
    if (block.equals(SuSyBlocks.COMBUSTION_CHAMBER)) {
      return 800
          + 100
              * switch ((BlockCombustionChamber.CombustionType) variant) {
                case BIPROPELLANT -> 200.0;
                case MONOPROPELLANT -> 150.0;
                case OXIDISER -> 200.00;
              };

    } else if (block.equals(TANK_SHELL)) {
      return 25
          + 50
              * switch ((BlockTankShell.TankCoverType) variant) {
                case TANK_SHELL -> 5;
                case STEEL_SHELL -> 8;
              };
    } else if (block.equals(SuSyBlocks.TANK_SHELL1)) {
      return 25
          + 50
              * switch ((BlockTankShell1.TankCoverType) variant) {
                case CARBON_COMPOSITE -> 3;
              };
    } else if (block.equals(SuSyBlocks.ROCKET_NOZZLE)) {
      return 500
          + 100
              * switch ((BlockRocketNozzle.NozzleShapeType) variant) {
                case BELL_NOZZLE -> 60.0;
                case PLUG_NOZZLE -> 65.0;
                case EXPANDING_NOZZLE -> 80.0;
              };
    } else if (block.equals(SuSyBlocks.TURBOPUMP)) {
      return 1000
          + 100
              * switch ((BlockTurboPump.HPPType) variant) {
                case BASIC -> 150.0;
              };
    }
    return 50.0;
  }
}
