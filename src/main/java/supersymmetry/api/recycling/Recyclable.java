package supersymmetry.api.recycling;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.math.Fraction;

import com.cleanroommc.groovyscript.api.IIngredient;

import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import supersymmetry.loaders.recipes.handlers.RecyclingManager;

public interface Recyclable {

    Recyclable EMPTY = _ -> ItemStack.EMPTY;

    // TODO: registry?
    static Recyclable from(Object obj) {
        return switch (obj) {
            case ItemStack itemStack -> new ItemLikeRecyclable(itemStack);
            case Item item -> new ItemLikeRecyclable(item);
            case Block block -> new ItemLikeRecyclable(block);
            case MetaValueItem metaValueItem -> new ItemLikeRecyclable(metaValueItem.getStackForm());
            case UnificationEntry unificationEntry -> new ItemLikeRecyclable(OreDictUnifier.get(unificationEntry));
            case String oreDict -> new ItemLikeRecyclable(OreDictUnifier.get(oreDict));
            case IIngredient ingredient -> new ItemLikeRecyclable(ingredient.getFirst());
            case MaterialStack ms -> new MaterialRecyclable(ms);
            default -> EMPTY;
        };
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
