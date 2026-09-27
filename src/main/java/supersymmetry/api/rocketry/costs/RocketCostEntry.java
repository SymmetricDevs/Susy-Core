package supersymmetry.api.rocketry.costs;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.ingredients.GTRecipeOreInput;

/**
 * One line of a {@link RocketCostGroup}: an item or ore dictionary entry, and how
 * many of it a single unit of that group costs.
 * <p>
 * Unlike {@link supersymmetry.api.rocketry.components.MaterialCost} — which is a
 * snapshot of blocks somebody actually placed, and therefore has to survive an NBT
 * round trip — a cost entry is resolved fresh every time assembly starts. That is
 * what lets it be an ore dictionary name, and what lets a pack rewrite it without
 * invalidating blueprints already sitting in a chest.
 */
public class RocketCostEntry {

    private final @Nullable String oreDict;
    private final @Nullable ItemStack stack;
    private final int count;

    public RocketCostEntry(@NotNull String oreDict, int count) {
        this.oreDict = oreDict;
        this.stack = null;
        this.count = count;
    }

    public RocketCostEntry(@NotNull ItemStack stack, int count) {
        this.oreDict = null;
        this.stack = new ItemStack(stack.getItem(), 1, stack.getMetadata());
        this.stack.setTagCompound(stack.getTagCompound());
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public GTRecipeInput toIngredient() {
        return oreDict != null ? new GTRecipeOreInput(oreDict, count) :
                new GTRecipeItemInput(stack.copy(), count);
    }

    @Override
    public String toString() {
        return (oreDict != null ? oreDict : stack.getTranslationKey()) + " x" + count;
    }
}
