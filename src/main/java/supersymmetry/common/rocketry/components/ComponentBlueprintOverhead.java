package supersymmetry.common.rocketry.components;

import java.util.List;
import java.util.Optional;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.costs.RocketCostGroup;
import supersymmetry.api.util.StructAnalysis;

/**
 * A {@link RocketCostGroup} wearing a component's clothes, so the rocket
 * assembler can charge for a blueprint's fixed materials using the same
 * one-recipe-per-component machinery it already uses for everything else.
 * <p>
 * This is never scanned, never registered, never put on a data card, and never
 * serialized — it is built on the fly by
 * {@link supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint#getAssemblySequence()}
 * and thrown away with the assembly. It deliberately stays out of
 * {@code RocketStage#components} so it cannot leak into a blueprint's mass,
 * radius, or NBT.
 */
public class ComponentBlueprintOverhead extends AbstractComponent<ComponentBlueprintOverhead> {

    public static final String TYPE = "blueprint_overhead";

    private final RocketCostGroup group;

    public ComponentBlueprintOverhead(RocketCostGroup group, double radius) {
        super(group.getName(), TYPE, candidate -> false);
        this.group = group;
        this.radius = radius;
        this.mass = 0;
        this.height = 0;
    }

    public RocketCostGroup getGroup() {
        return group;
    }

    @Override
    public List<GTRecipeInput> getRecipeInputs() {
        return group.toIngredients();
    }

    @Override
    public double getAssemblyDuration() {
        return group.getAssemblyDuration();
    }

    @Override
    public String getLocalizationKey() {
        return group.getLocalizationKey();
    }

    @Override
    public Optional<NBTTagCompound> analyzePattern(StructAnalysis analysis, AxisAlignedBB aabb) {
        return Optional.empty();
    }

    @Override
    public Optional<ComponentBlueprintOverhead> readFromNBT(NBTTagCompound compound) {
        return Optional.empty();
    }
}
