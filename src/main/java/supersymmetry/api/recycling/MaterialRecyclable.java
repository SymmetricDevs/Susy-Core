package supersymmetry.api.recycling;

import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.math.Fraction;

import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

public record MaterialRecyclable(Material material) implements Recyclable {

    public MaterialRecyclable(UnificationEntry unificationEntry) {
        this(unificationEntry.material);
    }

    public MaterialRecyclable(MaterialStack mStack) {
        this(mStack.material);
    }

    @Override
    public ItemStack asStack(int size) {
        throw new UnsupportedOperationException("Cannot create an ItemStack from a MaterialRecyclable instance!");
    }

    @Override
    public void addToMStack(Object2ObjectMap<Material, Fraction> mStacks, Fraction count) {
        if (Fraction.ZERO.equals(count)) return;
        if (material instanceof MarkerMaterial) return; /// Do nothing if this is a marker material
        mStacks.put(material, mStacks.getOrDefault(material, Fraction.ZERO).add(count));
    }

    @Override
    public int value(Object obj) {
        if (obj instanceof MaterialStack ms) {
            return (int) ms.amount;
        }
        // TODO:?
        return 1;
    }

    @Override
    public int hashCode() {
        return material.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof MaterialRecyclable other) {
            /// Not comparing registries here since they'll have the same oreDict in the end anyway...
            /// This is better for performance, and it's generally not a good practice
            /// to have materials with the same name in two registries.
            return material.getName().equals(other.material.getName());
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("M[%s]", material.getRegistryName());
    }
}
