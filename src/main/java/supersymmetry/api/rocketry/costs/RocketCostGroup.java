package supersymmetry.api.rocketry.costs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import supersymmetry.api.rocketry.AssemblyStep;

/**
 * A named bundle of fixed materials that a rocket costs regardless of which
 * components were bolted onto it — plumbing, wiring, the control system. One
 * group becomes exactly one step of the rocket assembler's build sequence, so
 * splitting a blueprint's overhead across several groups is how you pace it and
 * keep any single step's ingredient list readable.
 */
public class RocketCostGroup implements AssemblyStep {

    // its in ticks
    public static final double DEFAULT_ASSEMBLY_DURATION = 200;

    private final String name;
    private final List<RocketCostEntry> entries;
    private final double assemblyDuration;

    public RocketCostGroup(String name, List<RocketCostEntry> entries, double assemblyDuration) {
        this.name = name;
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
        this.assemblyDuration = assemblyDuration;
    }

    public String getName() {
        return name;
    }

    @Override
    public double getAssemblyDuration() {
        return assemblyDuration;
    }

    @Override
    public double getElectrodeDamageFactor() {
        return assemblyDuration;
    }

    public List<RocketCostEntry> getEntries() {
        return entries;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public List<GTRecipeInput> getRecipeInputs() {
        return entries.stream().map(RocketCostEntry::toIngredient).collect(Collectors.toList());
    }

    public static class Builder {

        private final String name;
        private final List<RocketCostEntry> entries = new ArrayList<>();
        private double assemblyDuration = DEFAULT_ASSEMBLY_DURATION;

        public Builder(String name) {
            this.name = name;
        }

        public Builder duration(double ticks) {
            this.assemblyDuration = ticks;
            return this;
        }

        public Builder input(String oreDict, int count) {
            entries.add(new RocketCostEntry(oreDict, count));
            return this;
        }

        public Builder input(ItemStack stack, int count) {
            entries.add(new RocketCostEntry(stack, count));
            return this;
        }

        public Builder input(ItemStack stack) {
            return input(stack, stack.getCount());
        }

        public RocketCostGroup build() {
            return new RocketCostGroup(name, entries, assemblyDuration);
        }
    }
}
