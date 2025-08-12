package supersymmetry.api.rocketry.rockets;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import supersymmetry.Supersymmetry;

public abstract class AbstractRocketBlueprint {

  public String name;
  public ResourceLocation relatedEntity = new ResourceLocation(Supersymmetry.MODID, "rocket_basic");
  public List<int[]> ignitionStages =
      new ArrayList<>(); // allows for multiple stages to be ignited at once, ex.

  // boosters together with the main stage, or the second stage together with the EES which im
  // definitely not adding
  // meant to contain the INDEX of the stages in the list bellow
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
