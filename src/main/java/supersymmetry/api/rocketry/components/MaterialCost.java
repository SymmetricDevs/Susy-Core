package supersymmetry.api.rocketry.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;

// item / block -> nbt serialization
public class MaterialCost {

    private static class Pair<A, B> {

        public A left;
        public B right;

        public Pair(A left, B right) {
            this.left = left;
            this.right = right;
        }
    }

    public static Map<Pair<Integer, Item>, Recipe> itemCache = new HashMap<>();
    public static final MaterialCost dirt = new MaterialCost("minecraft:dirt", "block", 0, 1);

    // not checked since you should probably only ever use the function above to make it
    public static MaterialCost fromNBT(NBTTagCompound tag) {
        return new MaterialCost(
                tag.getString("r"),
                tag.getString("t"),
                tag.getInteger("m"),
                tag.getInteger("c"));
    }

    String resource;
    String type;
    int meta;

    int count;

    public MaterialCost(String resource, String type, int meta, int count) {
        if (this.type == "block") {
            Block block = Block.REGISTRY.getObject(new ResourceLocation(resource));
            if (block != null) {
                Item item = Item.getItemFromBlock(block);
                this.resource = item.getRegistryName().toString();
                this.meta = meta;
                this.type = "item";
                this.count = count;
                return;
            } else {
                throw new IllegalStateException("weird block");
            }
        }
        this.resource = resource;
        this.count = count;
        this.type = type;
        this.meta = meta;
    }

    public NBTTagCompound toNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("r", this.resource);
        tag.setString("t", this.type);
        tag.setInteger("m", this.meta);
        tag.setInteger("c", this.count);
        return tag;
    }

    public List<GTRecipeInput> expandRecipe(RecipeMap<?> map, long maxEnergy) {
        ItemStack selfStack = this.toStack();
        if (itemCache.containsKey(new Pair<Integer, Item>(this.meta, this.toItem()))) {
            Recipe r = itemCache.get(new Pair<Integer, Item>(this.meta, this.toItem()));
            ItemStack outStack = r.getOutputs().get(0);
            float mul = (float) this.count / (float) outStack.getCount();
            return r.getInputs().stream()
                    .map(x -> x.copyWithAmount((int) Math.ceil(x.getAmount() * mul)))
                    .collect(Collectors.toList());
        }
        Collection<Recipe> mapRecipes = map.getRecipeList();
        List<Recipe> possibleRecipes = mapRecipes.parallelStream()
                .filter(x -> x.getEUt() <= maxEnergy)
                .filter(x -> x.getOutputs().size() == 1 && x.getFluidInputs().size() == 0)
                .filter(x -> x.getOutputs().get(0).isItemEqual(selfStack))
                .distinct()
                .collect(Collectors.toList());
        if (possibleRecipes.stream().map(Recipe::getInputs).distinct().count() == 1) {
            Recipe r = possibleRecipes.get(0);
            this.itemCache.put(new Pair<Integer, Item>(this.meta, this.toItem()), r);
            ItemStack outStack = r.getOutputs().get(0);
            float mul = (float) this.count / (float) outStack.getCount();

            return r.getInputs().stream()
                    .map(x -> x.copyWithAmount((int) Math.ceil(x.getAmount() * mul)))
                    .collect(Collectors.toList());
        } else {
            return Arrays.asList(this.toIngredient());
        }
    }

    public Item toItem() {
        // probably should check the type here but oh well
        return Item.REGISTRY.getObject(new ResourceLocation(this.resource));
    }

    public ItemStack toStack() {
        ItemStack stack = new ItemStack(this.toItem());
        stack.setCount(this.count);
        stack.setItemDamage(this.meta);
        return stack;
    }

    public GTRecipeInput toIngredient() {
        ItemStack stack = new ItemStack(this.toItem());
        stack.setItemDamage(this.meta);
        return new GTRecipeItemInput(stack, this.count);
    }
}
