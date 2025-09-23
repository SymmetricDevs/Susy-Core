package supersymmetry.api.rocketry.rockets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import supersymmetry.Supersymmetry;

public abstract class AbstractRocketBlueprint {

  public String name;
  private static Map<String, AbstractRocketBlueprint> blueprintsRegistry = new HashMap<>();

  // default blueprints for stuff.
  public static Map<String, AbstractRocketBlueprint> getBlueprintsRegistry() {
    return new HashMap<>(blueprintsRegistry);
  }

  public static AbstractRocketBlueprint getCopyOf(String name) {
    try {

      AbstractRocketBlueprint bp = AbstractRocketBlueprint.getBlueprintsRegistry().get(name);
      AbstractRocketBlueprint newbp =
          (AbstractRocketBlueprint)
              bp.getClass()
                  .getDeclaredConstructors()[0]
                  .newInstance(bp.getName(), bp.getRelatedEntity());

      newbp.readFromNBT(bp.writeToNBT());
      return newbp;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("failed to create a blueprint copy");
    }
  }

  public static void registerBlueprint(AbstractRocketBlueprint bp) {
    if (!getRegistryLock()) {
      blueprintsRegistry.put(bp.getName(), bp);
    }
  }

  public static boolean registryLock = false;

  public static boolean getRegistryLock() {
    return registryLock;
  }

  public static void setRegistryLock(boolean registryLock) {
    AbstractRocketBlueprint.registryLock = registryLock;
  }

  public ResourceLocation relatedEntity = new ResourceLocation(Supersymmetry.MODID, "rocket_basic");
  public List<int[]> ignitionStages =
      new ArrayList<>(); // allows for multiple stages to be ignited at once, ex.
  // boosters together with the main stage, or the second stage together with the EES which im
  // definitely not adding

  // meant to contain the INDEX of the stages in the list bellow
  // actually i dont remember why i added this
  public List<RocketStage> stages = new ArrayList<>();

  public AbstractRocketBlueprint(String name, ResourceLocation relatedEntity) {
    setName(name);
    setRelatedEntity(relatedEntity);
  }

  public List<int[]> getIgnitionStages() {
    return ignitionStages;
  }

  public void setIgnitionStages(List<int[]> ignitionStages) {
    this.ignitionStages = ignitionStages;
  }

  public List<RocketStage> getStages() {
    return this.stages;
  }

  public boolean isFullBlueprint() {
    return (stages.stream().allMatch(x -> x.isPopulated()));
  }

  public abstract boolean readFromNBT(NBTTagCompound tag);

  public abstract NBTTagCompound writeToNBT();

  public String getName() {
    return name;
  }

  public double getMass() {
    return this.getStages().stream().mapToDouble(RocketStage::getMass).sum();
  }

  public void setName(String name) {
    this.name = name;
  }

  public ResourceLocation getRelatedEntity() {
    return relatedEntity;
  }

  public void setRelatedEntity(ResourceLocation relatedEntity) {
    this.relatedEntity = relatedEntity;
  }

  public void setStages(List<RocketStage> stages) {
    this.stages = stages;
  }
}
