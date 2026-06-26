package supersymmetry.api.rocketry.components;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;

public class MaterialCost {

    public enum SourceType {

        ITEM(RecipeMaps.ASSEMBLER_RECIPES),
        COVER(RecipeMaps.BENDER_RECIPES);

        private final RecipeMap<?> recipeMap;

        SourceType(RecipeMap<?> recipeMap) {
            this.recipeMap = recipeMap;
        }

        public RecipeMap<?> getRecipeMap() {
            return recipeMap;
        }
    }

    private final ItemStack stack; // count=1, identity only
    private int count;
    private final SourceType sourceType;

    public MaterialCost(ItemStack stack, SourceType sourceType, int count) {
        this.stack = new ItemStack(stack.getItem(), 1, stack.getMetadata());
        this.count = count;
        this.sourceType = sourceType;
    }

    public static MaterialCost fromNBT(NBTTagCompound tag) {
        ItemStack stack = new ItemStack(tag);
        int count = tag.getInteger("c");
        SourceType type = SourceType.valueOf(tag.getString("s"));
        return new MaterialCost(stack, type, count);
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        ItemStack normalized = stack.copy();
        normalized.setCount(1);
        normalized.writeToNBT(tag);
        tag.setInteger("c", count);
        tag.setString("s", sourceType.name());
        return tag;
    }

    public int getCount() {
        return count;
    }

    public GTRecipeInput toIngredient() {
        return new GTRecipeItemInput(stack.copy(), count);
    }

    public List<GTRecipeInput> expandRecipe() {
        return Collections.singletonList(toIngredient());
    }
}
