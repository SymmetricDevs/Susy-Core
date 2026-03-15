package supersymmetry.api.rocketry.rockets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import supersymmetry.Supersymmetry;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;

public abstract class AbstractRocketBlueprint implements Cloneable {

    private static Map<String, AbstractRocketBlueprint> blueprintsRegistry = new HashMap<>();
    public static boolean registryLock = false;

    // default blueprints for stuff.
    public static Map<String, AbstractRocketBlueprint> getBlueprintsRegistry() {
        return new HashMap<>(blueprintsRegistry);
    }

    public static AbstractRocketBlueprint getCopyOf(String name) {
        if (blueprintsRegistry.containsKey(name)) {
            return blueprintsRegistry.get(name).clone();
        }
        return null;
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

    public String name;

    public ResourceLocation relatedEntity = new ResourceLocation(Supersymmetry.MODID, "rocket_basic");

    public List<RocketStage> stages = new ArrayList<>();

    public AbstractRocketBlueprint(String name, ResourceLocation relatedEntity) {
        setName(name);
        setRelatedEntity(relatedEntity);
    }

    public abstract double getMinimalSuccessChance();

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

    public double getMaxRadius() {
        return this.getStages().stream().mapToDouble(RocketStage::getRadius).max().orElse(0);
    }

    public double getTotalRadiusMismatch() {
        // Sum of the absolute differences between consecutive stages.
        double mismatch = 0;
        for (int i = 0; i < this.getStages().size() - 1; i++) {
            mismatch += Math.abs(this.getStages().get(i).getRadius() - this.getStages().get(i + 1).getRadius());
        }
        return mismatch;
    }

    public double getHeight() {
        return this.getStages().stream().mapToDouble(RocketStage::getHeight).sum();
    }

    public double getThrust(RocketFuelEntry entry, double gravity, String componentType) {
        return this.getStages().stream()
                .mapToDouble((stage) -> stage.getThrust(entry, gravity, componentType))
                .sum();
    }

    public double getEffectiveFuelVelocity(RocketFuelEntry entry, double gravity, String componentType) {
        double totalFuelThroughput = this.getStages().stream()
                .mapToDouble((stage) -> stage.getFuelThroughput(componentType)).sum();

        return this.getStages().stream().mapToDouble((stage) -> stage.getEffectiveFuelVelocity(entry, gravity) *
                stage.getFuelThroughput(componentType) / totalFuelThroughput).sum();
    }

    public double getFuelVolume() {
        return this.getStages().stream().mapToDouble(RocketStage::getFuelCapacity).sum();
    }

    public int getComponentCount(String componentType) {
        return this.getStages().stream()
                .mapToInt((comp) -> comp.getComponentCount(componentType))
                .sum();
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

    @Override
    public AbstractRocketBlueprint clone() {
        try {
            AbstractRocketBlueprint cloned = (AbstractRocketBlueprint) super.clone();
            cloned.stages = new ArrayList<>();
            for (RocketStage stage : this.stages) {
                cloned.stages.add((RocketStage) stage.clone());
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
