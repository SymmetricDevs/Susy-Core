package supersymmetry.api.rocketry.rockets;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import supersymmetry.Supersymmetry;
import supersymmetry.api.rocketry.AssemblyStep;
import supersymmetry.api.rocketry.costs.RocketBlueprintCosts;
import supersymmetry.api.rocketry.costs.RocketCostGroup;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.space.Planetoid;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.rocketry.SuccessCalculation;
import supersymmetry.common.rocketry.components.ComponentSpacecraft;

public abstract class AbstractRocketBlueprint implements Cloneable {

    public static final String INSTRUMENTS_KEY = "instruments";

    private static Map<String, AbstractRocketBlueprint> blueprintsRegistry = new TreeMap<>();
    public static boolean registryLock = false;

    // default blueprints for stuff.
    public static Map<String, AbstractRocketBlueprint> getBlueprintsRegistry() {
        return new TreeMap<>(blueprintsRegistry);
    }

    public static AbstractRocketBlueprint getRegistered(String name) {
        return blueprintsRegistry.get(name);
    }

    @Nullable public static AbstractRocketBlueprint fromTag(NBTTagCompound tag) {
        AbstractRocketBlueprint prototype = blueprintsRegistry.get(tag.getString("name"));
        if (prototype == null) {
            return null;
        }
        AbstractRocketBlueprint bp = prototype.emptyCopy();
        return bp.readFromNBT(tag) ? bp : null;
    }

    @Nullable public static AbstractRocketBlueprint fromItem(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return null;
        }
        return fromTag(stack.getTagCompound());
    }

    public static void registerBlueprint(AbstractRocketBlueprint bp) {
        if (!getRegistryLock()) {
            blueprintsRegistry.put(bp.getName(), bp);
        }
    }

    public static boolean getRegistryLock() {
        return registryLock;
    }

    public static void setRegistryLock(boolean registryLock) {
        AbstractRocketBlueprint.registryLock = registryLock;
    }

    @Getter
    public String name;

    public ResourceLocation relatedEntity = new ResourceLocation(Supersymmetry.MODID, "rocket_basic");

    @Getter
    public List<RocketStage> stages = new ArrayList<>();

    public AbstractRocketBlueprint(String name, ResourceLocation relatedEntity) {
        setName(name);
        setRelatedEntity(relatedEntity);
    }

    public Optional<RocketStage> getStage(String name) {
        return this.getStages().stream().filter(x -> x.getName().equals(name))
                .findFirst();
    }

    public boolean isFullBlueprint() {
        return (stages.stream().allMatch(RocketStage::isPopulated));
    }

    public abstract boolean readFromNBT(NBTTagCompound tag);

    public abstract NBTTagCompound writeToNBT();

    public double getMass() {
        return this.getStages().stream().mapToDouble(RocketStage::getMass).sum();
    }

    public double getMaxRadius() {
        return this.getStages().stream().mapToDouble(RocketStage::getRadius).max().orElse(0);
    }

    public double getTotalRadiusMismatch() {
        // Sum of the absolute differences between consecutive stages.
        double mismatch = 0;
        for (int i = 0; i < this.getStages().size() - 1; i++) {
            double interstageRadius = this.getStages().get(i).getInterstageRadius();
            mismatch += Math.abs(this.getStages().get(i).getRadius() - interstageRadius) +
                    Math.abs(interstageRadius - this.getStages().get(i + 1).getRadius());
        }
        return mismatch;
    }

    public double getHeight() {
        return this.getStages().stream().mapToDouble(RocketStage::getHeight).sum();
    }

    public double getThrust(RocketFuelEntry entry, double ambientPressure) {
        return this.getStages().stream()
                .mapToDouble((stage) -> stage.getThrust(entry, ambientPressure)).sum();
    }

    public double getFuelVolume() {
        return this.getStages().stream().mapToDouble(RocketStage::getFuelCapacity).sum();
    }

    public int getEngineCount() {
        return this.getStages().stream().mapToInt(RocketStage::getEngineCount).sum();
    }

    /**
     * Everything the rocket assembler has to build, in order: this blueprint's
     * fixed cost groups first, then the components the player actually specified.
     * <p>
     * The overhead leads so that a player who cannot afford the plumbing finds out
     * before sinking twenty minutes into engines. Costs are resolved here, at
     * assembly time, rather than baked into the blueprint — see
     * {@link RocketBlueprintCosts}.
     */
    public List<AssemblyStep> getAssemblySequence() {
        List<AssemblyStep> sequence = new ArrayList<>();
        for (RocketCostGroup group : RocketBlueprintCosts.get(this.getName())) {
            if (!group.isEmpty()) {
                sequence.add(group);
            }
        }
        this.getStages().stream().flatMap(stage -> stage.getComponents().values().stream()).flatMap(List::stream)
                .forEach(sequence::add);
        return sequence;
    }

    private List<ComponentSpacecraft> getSpacecrafts() {
        return this.getStages().stream().map(RocketStage::getComponents).flatMap(list -> list.values().stream())
                .flatMap(List::stream).filter(ComponentSpacecraft.class::isInstance)
                .map(ComponentSpacecraft.class::cast).collect(Collectors.toList());
    }

    public double getGuidanceMultiplier() {
        var spacecrafts = getSpacecrafts();
        return spacecrafts.isEmpty() ? 0 : spacecrafts.get(0).guidanceMultiplier;
    }

    public double getRedundancy() {
        var spacecrafts = getSpacecrafts();
        return spacecrafts.isEmpty() ? 0 : spacecrafts.get(0).redundancy;
    }

    public double getCollectionEfficiency() {
        var spacecrafts = getSpacecrafts();
        return spacecrafts.isEmpty() ? 0 : spacecrafts.get(0).collectionEfficiency;
    }

    public double getCargoVolume() {
        return getSpacecrafts().stream().mapToDouble(spacecraft -> spacecraft.volume).sum();
    }

    public Map<String, Integer> getInstruments() {
        return getSpacecrafts().stream().map(spacecraft -> spacecraft.instruments)
                .reduce(new Object2IntOpenHashMap<String>(), (map, entry) -> {
                    // computeAll basically
                    entry.forEach((key, value) -> map.merge(key, value, Integer::sum));
                    return map;
                });
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

    @Setter
    public Function<AbstractRocketBlueprint, ComponentValidationResult> componentValidationFunction = x -> {
        return ComponentValidationResult.SUCCESS;
    };

    private AbstractRocketBlueprint emptyCopy() {
        try {
            AbstractRocketBlueprint copy = (AbstractRocketBlueprint) super.clone();
            copy.stages = new ArrayList<>();
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract SuccessCalculation.AFSStats calculateInitialSuccess(Planetoid planet, RocketFuelEntry fuel,
                                                                        double turnAltitude, double cargoMass,
                                                                        long augmentation);

    public abstract SuccessCalculation.LaunchResult calculateSuccess(EntityAbstractRocket rocket, long augmentation);

    public abstract boolean isSolidRocket();
}
