package supersymmetry.api.recycling;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import supersymmetry.loaders.recipes.handlers.RecyclingManager;

public interface Recyclable {

    Recyclable EMPTY = size -> ItemStack.EMPTY;

    // TODO: registry?
    static Recyclable from(Object obj) {
        // TODO: ugly code
        if (obj instanceof ItemStack itemStack) {
            return new ItemLikeRecyclable(itemStack);
        } else if (obj instanceof Item item) {
            return new ItemLikeRecyclable(item);
        } else if (obj instanceof Block block) {
            return new ItemLikeRecyclable(block);
        } else if (obj instanceof MetaItem<?>.MetaValueItem metaValueItem) {
            return new ItemLikeRecyclable(metaValueItem.getStackForm());
        } else if (obj instanceof UnificationEntry unificationEntry) {
            return new ItemLikeRecyclable(OreDictUnifier.get(unificationEntry));
        } else if (obj instanceof String oreDict) {
            return new ItemLikeRecyclable(OreDictUnifier.get(oreDict));
        } else if (obj instanceof MaterialStack ms) {
            return new MaterialRecyclable(ms);
        }

        // TODO: damn it

//        throw new AssertionError();
        /// Fallback to empty if object is null
        return EMPTY;
    }

    default int value(Object obj) {
        return 1;
    }

    default boolean isEmpty() {
        return EMPTY.equals(this);
    }

    ItemStack asStack(int size);

    default ItemStack asStack() {
        return asStack(1);
    }

    default void addToMStack(Object2ObjectMap<Material, Fraction> mStacks, Fraction count) {
        if (Fraction.ZERO.equals(count)) return;
        RecyclingManager.addItemStackToMaterialStacks(asStack(), mStacks, count);
    }
}
