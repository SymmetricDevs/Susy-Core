package supersymmetry.api.rocketry.components;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import gregtech.api.recipes.Recipe;
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

    private static final Map<String, Recipe> RECIPE_CACHE = new HashMap<>();

    private final ItemStack stack; // count=1, identity only
    private int count;
    private final SourceType sourceType;

    public MaterialCost(ItemStack stack, SourceType sourceType) {
        this(stack, sourceType, stack.getCount());
    }

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

    public ItemStack toStack() {
        ItemStack out = stack.copy();
        out.setCount(count);
        return out;
    }

    public Item toItem() {
        return stack.getItem();
    }

    public int getCount() {
        return count;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public GTRecipeInput toIngredient() {
        return new GTRecipeItemInput(stack.copy(), count);
    }

    public List<GTRecipeInput> expandRecipe(long maxVoltage) {
        return expandRecipe(sourceType.getRecipeMap(), maxVoltage);
    }

    public List<GTRecipeInput> expandRecipe(RecipeMap<?> map, long maxVoltage) {
        ItemStack selfStack = toStack();
        String cacheKey = stack.getItem().getRegistryName() + "@" + stack.getMetadata();

        if (RECIPE_CACHE.containsKey(cacheKey)) {
            Recipe r = RECIPE_CACHE.get(cacheKey);
            ItemStack outStack = r.getOutputs().get(0);
            float mul = (float) count / (float) outStack.getCount();
            return r.getInputs().stream()
                    .map(x -> x.copyWithAmount((int) Math.ceil(x.getAmount() * mul)))
                    .collect(Collectors.toList());
        }

        Collection<Recipe> mapRecipes = map.getRecipeList();
        List<Recipe> possibleRecipes = mapRecipes.parallelStream()
                .filter(x -> x.getEUt() <= maxVoltage)
                .filter(x -> x.getOutputs().size() == 1 && x.getFluidInputs().isEmpty())
                .filter(x -> x.getOutputs().get(0).isItemEqual(selfStack))
                .distinct()
                .collect(Collectors.toList());

        if (possibleRecipes.stream().map(Recipe::getInputs).distinct().count() == 1) {
            Recipe r = possibleRecipes.get(0);
            RECIPE_CACHE.put(cacheKey, r);
            ItemStack outStack = r.getOutputs().get(0);
            float mul = (float) count / (float) outStack.getCount();
            return r.getInputs().stream()
                    .map(x -> x.copyWithAmount((int) Math.ceil(x.getAmount() * mul)))
                    .collect(Collectors.toList());
        } else {
            return Collections.singletonList(toIngredient());
        }
    }
}
