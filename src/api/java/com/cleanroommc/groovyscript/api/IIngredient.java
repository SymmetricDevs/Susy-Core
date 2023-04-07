package com.cleanroommc.groovyscript.api;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;


/**
 * Base ingredient class for every ingredient. Most useful for item stacks and ore dicts.
 */
public interface IIngredient {

    IIngredient exactCopy();

    Ingredient toMcIngredient();

    ItemStack[] getMatchingStacks();

    default ItemStack applyTransform(ItemStack matchedInput) {
        return matchedInput.getItem().hasContainerItem(matchedInput) ? matchedInput.getItem().getContainerItem(matchedInput) : ItemStack.EMPTY;
    }
}
