package supersymmetry.api.rocketry.rockets;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraftforge.common.util.Constants.NBT;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Setter;
import supersymmetry.SuSyValues;
import supersymmetry.api.SusyLog;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.components.RocketEngine;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.common.rocketry.components.ComponentInterstage;
import supersymmetry.common.rocketry.components.IComponentTank;

public class RocketStage implements Cloneable {

    public static class Builder {

        String lastComponentName = "";

        String name;
        Map<String, List<Integer>> compLimit = new TreeMap<>();

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

        public Builder range(int min, int max) {
            List<Integer> possibilities = compLimit.get(lastComponentName);
            for (int i = min; i <= max; i++) {
                possibilities.add(i);
            }
            return this;
        }

        public Builder stageName(String name) {
            this.name = name;
            return this;
        }

        public RocketStage build() {
            Map<String, int[]> limits = new TreeMap<>();
            compLimit.forEach((k, v) -> limits.put(k, v.stream().mapToInt(Integer::intValue).toArray()));
            return new RocketStage(limits, name);
        }
    }

    public Map<String, List<AbstractComponent<?>>> components = new TreeMap<>();

    // allows you to make it so it needs different types of engines for example.
    // ensures compatibility
    // between components of the same type

    // limits on how many of each component it can have
    public Map<String, int[]> componentLimits = new TreeMap<>();

    // ex "boosters" or "lander", localized with susy.rocketry.stages.name.<name
    // string>
    @Setter
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

    public boolean isPopulated() {
        return components.values().stream().noneMatch(List::isEmpty) && !components.isEmpty();
    }

    private Stream<AbstractComponent<?>> stream() {
        return components.values().stream().flatMap(List::stream);
    }

    public double getMass() {
        return stream().mapToDouble(AbstractComponent::getMass).sum();
    }

    private Stream<IComponentTank> tanks() {
        return stream().filter(IComponentTank.class::isInstance).map(IComponentTank.class::cast);
    }

    private Stream<RocketEngine> engines() {
        return stream().filter(RocketEngine.class::isInstance).map(RocketEngine.class::cast);
    }

    public double getFuelCapacity() {
        return tanks().mapToDouble(IComponentTank::getVolume).sum() * 1000; // 1000 L per m^3 by definition
    }

    // In kg/s
    public double getFuelThroughput() {
        return engines().mapToDouble(RocketEngine::getFuelThroughput).sum();
    }

    public int getEngineCount() {
        return (int) engines().count();
    }

    /**
     * Vacuum exhaust velocity, averaged over every engine on the stage in
     * proportion to how much propellant each one is actually pushing. Delta-v is
     * spent almost entirely out of the atmosphere, so the bells get judged against
     * vacuum here even though liftoff thrust is not.
     */
    public double getEffectiveFuelVelocity(RocketFuelEntry rocketFuelEntry) {
        return rocketFuelEntry.getSpecificImpulse() * SuSyValues.G0 * getNozzleEfficiency(0);
    }

    /**
     * Flow-weighted nozzle efficiency across the stage's engines, or 1 if the stage
     * has none to speak for it.
     */
    public double getNozzleEfficiency(double ambientPressure) {
        double flow = 0;
        double weighted = 0;
        for (RocketEngine engine : engines().toList()) {
            double throughput = engine.getFuelThroughput();
            flow += throughput;
            weighted += throughput * engine.getNozzleEfficiency(ambientPressure);
        }
        return flow > 0 ? weighted / flow : 1;
    }

    /**
     * Thrust from the stage's engines, in N. Summed per engine rather than off the
     * stage total, since every nozzle answers to its own expansion ratio.
     */
    public double getThrust(RocketFuelEntry rocketFuelEntry, double ambientPressure) {
        double exhaustVelocity = rocketFuelEntry.getSpecificImpulse() * SuSyValues.G0;
        return engines()
                .mapToDouble(engine -> engine.getFuelThroughput() * exhaustVelocity *
                        engine.getNozzleEfficiency(ambientPressure) * engine.getEfficiency())
                .sum();
    }

    public double getRadius() {
        // Max radius, in meters
        return stream().mapToDouble(AbstractComponent::getRadius).max().orElse(0);
    }

    public double getInterstageRadius() {
        return stream().filter(ComponentInterstage.class::isInstance)
                .mapToDouble(AbstractComponent::getRadius).findFirst().orElse(getRadius());
    }

    public double getHeight() {
        // Height (again max), in meters
        return stream().mapToDouble(AbstractComponent::getHeight).max().orElse(0);
    }

    private void setComponentLimits(Map<String, int[]> componentLimits) {
        if (componentLimits.values().stream().anyMatch(arr -> arr.length == 0))
            throw new IllegalStateException("empty possibility array provided");
        this.componentLimits = componentLimits;
    }

    private static ComponentValidationResult validateRow(Map<String, int[]> limits, String stageName, String name,
                                                         List<AbstractComponent<?>> componentList) {
        int[] allowed = limits.get(name);
        if (allowed == null) {
            SusyLog.logger.error("stage {} has no row for type {}, dropping it", stageName, name);
            return ComponentValidationResult.INVALID_CARD;
        }
        if (IntStream.of(allowed).noneMatch(x -> x == componentList.size())) {
            return ComponentValidationResult.INVALID_AMOUNT; // fail if you cant put that amount of components is
            // invalid
        }
        return ComponentValidationResult.SUCCESS;
    }

    public ComponentValidationResult setComponentListEntry(String name,
                                                           List<AbstractComponent<?>> componentList) {
        ComponentValidationResult result = validateRow(this.componentLimits, this.name, name, componentList);
        if (result != ComponentValidationResult.SUCCESS) {
            return result;
        }
        if (componentList.stream().anyMatch(x -> x.getMaterials().isEmpty())) {
            SusyLog.logger.info("empty material list in entry {}", name);
        }
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
        Object2IntOpenHashMap<NBTTagCompound> tags = new Object2IntOpenHashMap<>();
        for (Map.Entry<String, List<AbstractComponent<?>>> component : this.getComponents().entrySet()) {
            String componentKey = component.getKey();
            List<Integer> pos = new ArrayList<>();

            component.getValue().stream().forEach(x -> {
                NBTTagCompound innerTag = new NBTTagCompound();
                x.writeToNBT(innerTag);
                if (!tags.containsKey(innerTag)) {
                    tags.put(innerTag, tags.size());
                }
                pos.add(tags.get(innerTag));
            });

            componentsListCompound.setTag(componentKey, new NBTTagIntArray(pos));
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
        if (!tag.hasKey("name", NBT.TAG_STRING))
            return false;
        if (!tag.hasKey("components", NBT.TAG_COMPOUND))
            return false;
        if (!tag.hasKey("allowedCounts", NBT.TAG_COMPOUND))
            return false;
        if (!tag.hasKey("componentValues", NBT.TAG_COMPOUND))
            return false;

        NBTTagCompound allowedCounts = tag.getCompoundTag("allowedCounts");
        Map<String, int[]> limits = new TreeMap<>();
        allowedCounts.getKeySet().stream().sorted()
                .forEach(key -> limits.put(key, allowedCounts.getIntArray(key)));

        NBTTagCompound lookup = tag.getCompoundTag("componentValues");
        NBTTagCompound components = tag.getCompoundTag("components");
        Map<String, List<AbstractComponent<?>>> parsed = new TreeMap<>();
        for (String key : components.getKeySet().stream().sorted().collect(Collectors.toList())) {
            List<AbstractComponent<?>> realComponents = new ArrayList<>();
            for (int componentIndex : components.getIntArray(key)) {
                NBTTagCompound componentTag = (NBTTagCompound) lookup.getTag(Integer.toString(componentIndex));
                if (componentTag == null) {
                    SusyLog.logger.error(
                            "component index {} missing from componentValues, not loading blueprint. full nbt:\n{}",
                            componentIndex, tag);
                    return false;
                }
                AbstractComponent<?> prototype = AbstractComponent.getComponentFromName(componentTag.getString("name"));
                if (prototype == null) {
                    SusyLog.logger.error(
                            "component {} not found, not loading blueprint with invalid data. full nbt:\n{}",
                            componentTag.getString("name"), tag);
                    return false;
                }
                Optional<? extends AbstractComponent<?>> extracted = prototype.readFromNBT(componentTag);
                if (extracted.isPresent()) {
                    realComponents.add(extracted.get());
                } else {
                    SusyLog.logger.error("component {} failed to read, stage {} row {}. full nbt:\n{}",
                            componentTag.getString("name"), this.getName(), key, tag);
                }
            }
            parsed.put(key, realComponents);
        }

        String stageName = tag.getString("name");
        for (Map.Entry<String, List<AbstractComponent<?>>> entry : parsed.entrySet()) {
            if (validateRow(limits, stageName, entry.getKey(), entry.getValue()) != ComponentValidationResult.SUCCESS) {
                SusyLog.logger.error(
                        "stage {} row {} has {} component(s), allowed counts {}. not loading blueprint. full nbt:\n{}",
                        stageName, entry.getKey(), entry.getValue().size(),
                        Arrays.toString(limits.get(entry.getKey())), tag);
                return false;
            }
        }

        this.componentLimits.clear();
        this.componentLimits.putAll(limits);
        this.components.clear();
        this.components.putAll(parsed);
        this.name = stageName;

        return true;
    }

    @Override
    public RocketStage clone() {
        try {
            RocketStage cloned = (RocketStage) super.clone();
            cloned.components = new TreeMap<>();
            for (Map.Entry<String, List<AbstractComponent<?>>> entry : this.components.entrySet()) {
                cloned.components.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            cloned.componentLimits = new TreeMap<>(this.componentLimits);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
