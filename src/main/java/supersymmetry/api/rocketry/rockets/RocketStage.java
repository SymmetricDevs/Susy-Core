package supersymmetry.api.rocketry.rockets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants.NBT;

import supersymmetry.api.SusyLog;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.common.rocketry.components.ComponentLavalEngine;
import supersymmetry.common.rocketry.components.ComponentLiquidFuelTank;

public class RocketStage {

    public enum ComponentValidationResult {

        SUCCESS("success"),
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
    public Function<Tuple<String, List<AbstractComponent<?>>>, ComponentValidationResult> componentValidationFunction = x -> {
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

    public Function<Tuple<String, List<AbstractComponent<?>>>, ComponentValidationResult> getComponentValidationFunction() {
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
                .mapToInt(tank -> ((ComponentLiquidFuelTank) tank).volume)
                .sum() * 1000; // 1000 L per m^3 by definition
    }

    // In kg/s
    public double getFuelThroughput(String componentType) {
        return components.values().stream()
                .flatMap(List::stream)
                .filter(c -> c.getType().equals(componentType))
                .mapToDouble(engine -> ((ComponentLavalEngine) engine).fuelThroughput)
                .sum();
    }

    public int getComponentCount(String componentType) {
        return components.values().stream()
                .flatMap(List::stream)
                .filter(c -> c.getType().equals(componentType))
                .mapToInt(engine -> 1)
                .sum();
    }

    public double getThrust(RocketFuelEntry rocketFuelEntry, double gravity, String componentType) {
        return getFuelThroughput(componentType) * rocketFuelEntry.getSpecificImpulse() * gravity;
    }

    public double getRadius() {
        // Max radius, in meters
        return components.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AbstractComponent::getRadius)
                .max()
                .orElse(0);
    }

    public double getHeight() {
        // Height (again max), in meters
        return components.values().stream()
                .flatMap(List::stream)
                .mapToDouble(AbstractComponent::getHeight)
                .max()
                .orElse(0);
    }

    public void setComponentValidationFunction(
                                               Function<Tuple<String, List<AbstractComponent<?>>>, ComponentValidationResult> componentValidationPredicate) {
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
                                                                       String name,
                                                                       List<AbstractComponent<?>> componentList) {
        if (componentList.stream().anyMatch(x -> x.materials.isEmpty())) {
            SusyLog.logger.info(
                    "empty material list. {}",
                    componentList.stream()
                            .map(x -> x.materials)
                            .flatMap(m -> m.stream())
                            .map(x -> x.toNBT())
                            .collect(Collectors.toList()));
        }
        if (IntStream.of(this.componentLimits.get(name)).noneMatch(x -> x == componentList.size())) {
            return ComponentValidationResult.INVALID_AMOUNT; // fail if you cant put that amount of components is
            // invalid
        }
        ComponentValidationResult validation_result = componentValidationFunction.apply(
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
        return "susy.rocketry.stages." + this.name + ".name";
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("name", this.getName());
        NBTTagCompound componentsListCompound = new NBTTagCompound();
        HashMap<NBTTagCompound, Integer> tags = new HashMap<>();
        for (Map.Entry<String, List<AbstractComponent<?>>> component : this.getComponents().entrySet()) {
            List<Integer> pos = new ArrayList<>();

            component.getValue().stream()
                    .forEach(
                            x -> {
                                NBTTagCompound innerTag = new NBTTagCompound();
                                x.writeToNBT(innerTag);
                                // componentsArray.appendTag(innerTag);
                                if (!tags.containsKey(innerTag)) {
                                    tags.put(innerTag, tags.size());
                                }
                                pos.add(tags.get(innerTag));
                            });

            componentsListCompound.setTag(component.getKey(), new NBTTagIntArray(pos));
        }
        NBTTagCompound tagComponents = new NBTTagCompound();
        for (Entry<NBTTagCompound, Integer> entry : tags.entrySet()) {
            tagComponents.setTag(entry.getValue().toString(), entry.getKey());
        }

        NBTTagCompound allowedCountCompound = new NBTTagCompound();
        for (Entry<String, int[]> allowedCount : this.getComponentLimits().entrySet()) {
            NBTTagIntArray intArrayTag = new NBTTagIntArray(allowedCount.getValue());
            allowedCountCompound.setTag(allowedCount.getKey(), intArrayTag);
        }
        tag.setTag("componentValues", tagComponents);
        tag.setTag("components", componentsListCompound);
        tag.setTag("allowedCounts", allowedCountCompound);
        return tag;
    }

    public boolean readFromNBT(NBTTagCompound tag) {
        if (!tag.hasKey("name", NBT.TAG_STRING)) return false;
        if (!tag.hasKey("components", NBT.TAG_COMPOUND)) return false;
        if (!tag.hasKey("allowedCounts", NBT.TAG_COMPOUND)) return false;
        if (!tag.hasKey("componentValues", NBT.TAG_COMPOUND)) return false;

        this.componentLimits.clear();
        this.components.clear();
        NBTTagCompound allowedCounts = tag.getCompoundTag("allowedCounts");
        for (String key : allowedCounts.getKeySet()) {
            this.componentLimits.put(key, allowedCounts.getIntArray(key));
        }
        NBTTagCompound lookup = (NBTTagCompound) tag.getTag("componentValues");

        NBTTagCompound components = tag.getCompoundTag("components");
        for (String key : components.getKeySet()) {
            int[] componentIndexes = components.getIntArray(key);
            List<AbstractComponent<?>> realComponents = new ArrayList<>();
            for (int i = 0; i < componentIndexes.length; i++) {
                NBTTagCompound componentTag = (NBTTagCompound) lookup
                        .getTag(Integer.valueOf(componentIndexes[i]).toString());
                Optional<? extends AbstractComponent<?>> extractedComponent = AbstractComponent
                        .getComponentFromName(componentTag.getString("name"))
                        .readFromNBT(componentTag);
                if (extractedComponent.isPresent()) {
                    realComponents.add(extractedComponent.get());
                } else {
                    SusyLog.logger.error(
                            "failed to read a component somehow, index: {} nbt at index: {}", i, componentTag);
                }
            }
            this.setComponentListEntry(key, realComponents);
        }
        this.name = tag.getString("name");

        return true;
    }
}
