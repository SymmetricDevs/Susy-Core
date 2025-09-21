package supersymmetry.api.rocketry.rockets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.Fluid;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.common.rocketry.components.ComponentLavalEngine;
import supersymmetry.common.rocketry.components.ComponentLiquidFuelTank;

public class RocketStage {
  public enum ComponentValidationResult {
    SUCCESS("goog"),
    INVALID_CARD("invalid_card"),
    VALIDATION_FAILURE("validation_failure"),
    INVALID_AMOUNT("invalid_amount"),
    INCOMPATIBLE_CARD("incompatible_card"),
    UNKNOWN("unknown");

    private String name;

    ComponentValidationResult(String name) {
      this.name = name;
    }

    public String getName() {
      return this.name;
    }

    public String getTranslationKey() {
      return "susy.rocketry.components.validation_codes." + this.name;
    }
  }

  public static class Builder {
    String lastComponentName = "";

    String name;
    Map<String, List<Integer>> compLimit = new HashMap<>();

    public Builder(String stagename) {
      this.name = stagename;
    }

    public Builder type(String name) {
      lastComponentName = name;
      compLimit.put(lastComponentName, new ArrayList<>());
      return this;
    }

    public Builder limit(int limit) {
      compLimit.get(lastComponentName).add(limit);
      return this;
    }

    public Builder stageName(String name) {
      this.name = name;
      return this;
    }

    public RocketStage build() {
      return new RocketStage(
          compLimit.entrySet().stream()
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey,
                      e -> e.getValue().stream().mapToInt(Integer::intValue).toArray())),
          name);
    }
  }

  public Map<String, List<AbstractComponent<?>>> components = new HashMap<>();

  // allows you to make it so it needs different types of engines for example. ensures compatibility
  // between components of the same type
  public Function<Tuple<String, List<AbstractComponent<?>>>, ComponentValidationResult>
      componentValidationFunction =
          x -> {
            return ComponentValidationResult.SUCCESS;
            // this is done after the type checks in the gui anyways, no need to double check ithink
          };

  // limits on how many of each component it can have
  public Map<String, int[]> componentLimits = new HashMap<>();

  // ex "boosters" or "lander", localized with susy.rocketry.stages.name.<name string>
  public String name;

  public RocketStage(final Map<String, int[]> limits, String name) {
    this.setComponentLimits(limits);
    this.setName(name);
  }

  public RocketStage(final Map<String, int[]> limits) {
    this.setComponentLimits(limits);
  }

  public RocketStage() {
    this.name = "unprocessed"; // meant to be read from nbt later
  }

  public Function<Tuple<String, List<AbstractComponent<?>>>, ComponentValidationResult>
      getComponentValidationFunction() {
    return componentValidationFunction;
  }

  public boolean isPopulated() {
    return components.values().stream().noneMatch(x -> x.isEmpty()) && !components.isEmpty();
  }

  public double getMass() {
    return components.values().stream()
        .flatMap(List::stream)
        .mapToDouble(AbstractComponent::getMass)
        .sum();
  }

  public double getFuelCapacity() {
    return components.values().stream()
            .flatMap(List::stream)
            .filter(c -> c.getType().equals("tank"))
            .mapToInt( tank -> ((ComponentLiquidFuelTank) tank).volume)
            .sum() * 1000; // 1000 L per m^3 by definition
  }

  // In kg/s
  public double getFuelThroughput() {
    return components.values().stream()
            .flatMap(List::stream)
            .filter(c -> c.getType().equals("engine"))
            .mapToDouble(engine -> ((ComponentLavalEngine) engine).fuel_throughput)
            .sum();
  }

  public double getThrust(RocketFuelEntry rocketFuelEntry, double gravity) {
    double power = getFuelThroughput() * rocketFuelEntry.getHeatOfUse(); // kg/s * J/kg
    double specific_heat_ratio = 1.25; // TODO: make this dependent on output
    //double massVelocity =
    return 0;
  }

  public void setComponentValidationFunction(
      Function<Tuple<String, List<AbstractComponent<?>>>, ComponentValidationResult>
          componentValidationPredicate) {
    this.componentValidationFunction = componentValidationPredicate;
  }

  public void setComponentLimits(Map<String, int[]> componentLimits) {

    if (!componentLimits.values().stream().noneMatch(arr -> arr.length == 0))
      throw new IllegalStateException("empty limit array provided");
    this.componentLimits = componentLimits;
  }

  public void setName(String name) {
    this.name = name;
  }

  public RocketStage.ComponentValidationResult setComponentListEntry(
      String name, List<AbstractComponent<?>> componentList) {
    if (IntStream.of(this.componentLimits.get(name)).noneMatch(x -> x == componentList.size())) {
      return ComponentValidationResult
          .INVALID_AMOUNT; // fail if you cant put that amount of components is invalid
    }
    var validation_result =
        componentValidationFunction.apply(
            new Tuple<String, List<AbstractComponent<?>>>(name, componentList));
    if (validation_result != ComponentValidationResult.SUCCESS) return validation_result;
    components.put(name, componentList);
    return ComponentValidationResult.SUCCESS;
  }

  public int maxComponentsOf(String cname) {
    return Arrays.stream(this.getComponentLimits().get(cname)).max().getAsInt();
  }

  public Map<String, List<AbstractComponent<?>>> getComponents() {
    return components;
  }

  public Map<String, int[]> getComponentLimits() {
    return componentLimits;
  }

  public String getName() {
    return name;
  }

  public String getLocalizationKey() {
    return "susy.rocketry.stages.name." + this.name;
  }

  public NBTTagCompound writeToNBT() {
    NBTTagCompound tag = new NBTTagCompound();
    tag.setString("name", this.getName());
    NBTTagCompound componentsListCompound = new NBTTagCompound();

    for (var component : this.getComponents().entrySet()) {
      NBTTagList componentsArray = new NBTTagList();

      component.getValue().stream()
          .forEach(
              x -> {
                var innerTag = new NBTTagCompound();
                x.writeToNBT(innerTag);
                componentsArray.appendTag(innerTag);
              });
      // wrote an array of components with the key equal to the stage component type name
      componentsListCompound.setTag(component.getKey(), componentsArray);
    }
    NBTTagCompound allowedCountCompound = new NBTTagCompound();
    for (var allowedCount : this.getComponentLimits().entrySet()) {
      NBTTagIntArray intArrayTag = new NBTTagIntArray(allowedCount.getValue());
      allowedCountCompound.setTag(allowedCount.getKey(), intArrayTag);
    }
    tag.setTag("components", componentsListCompound);
    tag.setTag("allowedCounts", allowedCountCompound);
    return tag;
  }

  public boolean readFromNBT(NBTTagCompound tag) {
    if (!tag.hasKey("name", NBT.TAG_STRING)) return false;
    if (!tag.hasKey("components", NBT.TAG_COMPOUND)) return false;
    if (!tag.hasKey("allowedCounts", NBT.TAG_COMPOUND)) return false;
    this.componentLimits.clear();
    this.components.clear();
    NBTTagCompound allowedCounts = tag.getCompoundTag("allowedCounts");
    for (String key : allowedCounts.getKeySet()) {
      this.componentLimits.put(key, allowedCounts.getIntArray(key));
    }

    NBTTagCompound components = tag.getCompoundTag("components");
    for (String key : components.getKeySet()) {
      NBTTagList componentsArray =
          components.getTagList(key, NBT.TAG_COMPOUND); // hopefully will work?
      List<AbstractComponent<?>> realComponents = new ArrayList<>();
      componentsArray.forEach(
          x -> {
            AbstractComponent.getComponentFromName(((NBTTagCompound) x).getString("name"))
                .readFromNBT((NBTTagCompound) x)
                .ifPresent(
                    l -> {
                      realComponents.add(l);
                    });
          });
      this.setComponentListEntry(key, realComponents);
    }
    this.name = tag.getString("name");

    return true;
  }
}
