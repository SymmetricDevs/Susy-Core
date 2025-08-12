package supersymmetry.api.rocketry.rockets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants.NBT;
import supersymmetry.api.rocketry.components.AbstractComponent;

public class rocketStage {
  public static class Builder {
    public Builder(String stagename) {
      this.name = stagename;
    }

    String lastComponentName = "";
    String name;
    Map<String, List<Integer>> compLimit = new HashMap<>();

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

    public rocketStage build() {
      return new rocketStage(
          compLimit.entrySet().stream()
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey,
                      e -> e.getValue().stream().mapToInt(Integer::intValue).toArray())),
          name);
    }
  }

  public Map<String, List<AbstractComponent<?>>> components = new HashMap<>();

  // allows you to make it so it needs different types of engines for example, but it should
  // probably return true for most things
  // TODO: make this return some additional context to why it failed when a rocket uses this thing
  public Predicate<Tuple<String, List<AbstractComponent<?>>>> componentValidationPredicate =
      x -> {
        return true;
        // this is done after the type checks in the gui anyways, no need to double check ithink
      };
  // limits on how many of each component it can have
  public Map<String, int[]> componentLimits = new HashMap<>();
  public String
      name; // ex "boosters" or "lander", localized with susy.rocketry.stages.name.<name string>

  public rocketStage(final Map<String, int[]> limits, String name) {
    this.componentLimits = limits;
    this.setName(name);
  }

  public rocketStage(final Map<String, int[]> limits) {
    this.componentLimits = limits;
  }

  public rocketStage() {
    this.name = "unprocessed"; // meant to be read from nbt later
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

  public void setComponentValidationPredicate(
      Predicate<Tuple<String, List<AbstractComponent<?>>>> componentValidationPredicate) {
    this.componentValidationPredicate = componentValidationPredicate;
  }

  public void setComponentLimits(Map<String, int[]> componentLimits) {
    this.componentLimits = componentLimits;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValidationPredicate(
      final Predicate<Tuple<String, List<AbstractComponent<?>>>> predicate) {
    this.componentValidationPredicate = predicate;
  }

  // doesnt actually run the component validation predicate to avoid issues with its order
  public boolean setComponentListEntry(
      final String name, final List<AbstractComponent<?>> componentList) {
    if (IntStream.of(this.componentLimits.get(name)).noneMatch(x -> x == componentList.size())) {
      return false; // fail if you cant put that amount of components is invalid
    }
    if (!componentValidationPredicate.test(
        new Tuple<String, List<AbstractComponent<?>>>(name, componentList))) return false;
    components.put(name, componentList);
    return true;
  }

  public Map<String, List<AbstractComponent<?>>> getComponents() {
    return components;
  }

  public Predicate<Tuple<String, List<AbstractComponent<?>>>> getComponentValidationPredicate() {
    return componentValidationPredicate;
  }

  public Map<String, int[]> getComponentLimits() {
    return componentLimits;
  }

  public String getName() {
    return name;
  }

  public String getNameLocalzationkey() {
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

  public boolean readfromNBT(NBTTagCompound tag) {
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
                      // SusyLog.logger.info("successfully read {} from nbt", l.getName());
                      realComponents.add(l);
                    });
          });
      this.setComponentListEntry(key, realComponents);
    }
    this.name = tag.getString("name");

    return true;
  }
}
