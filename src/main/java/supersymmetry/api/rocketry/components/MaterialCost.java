package supersymmetry.api.rocketry.components;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
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

// only supports items so uhh just throw in 5000L of tin alloy during the recipe creation i guess
public class MaterialCost {
  String resource;
  String type;
  int meta;
  int count;

  public static Map<ItemStack, Recipe> itemCache = new HashMap<>();

  public MaterialCost(String resource, String type, int meta, int count) {
    this.resource = resource;
    this.type = type;
    this.meta = meta;
    this.count = count;

    if (this.type == "block") {
      Block block = Block.REGISTRY.getObject(new ResourceLocation(this.resource));
      if (block != null) {
        Item item = Item.getItemFromBlock(block);
        this.resource = item.getRegistryName().toString();
        this.type = "item";
      } else {
        throw new IllegalStateException("weird block");
      }
    }
  }

  public NBTTagCompound toNBT() {
    NBTTagCompound tag = new NBTTagCompound();
    tag.setString("resource", this.resource);
    tag.setString("type", this.type);
    tag.setInteger("meta", this.meta);
    tag.setInteger("count", this.count);
    return tag;
  }

  public List<GTRecipeInput> expandRecipe(RecipeMap<?> map, long maxEnergy) {
    ItemStack selfStack = this.toStack();
    // this is used as a key for a hashmap and it wouldnt be nice to have a separate entry
    // for each amount of blocks
    selfStack.setCount(1);

    Recipe selected = null;

    if (itemCache.containsKey(selfStack)) {
      var recipe = itemCache.get(selfStack);
      if (recipe == null) return Arrays.asList(this.toIngredient());
      selected = recipe;
      // set the recipe if it was already in cache, look into the recipemap if it wasnt
      // please forgive the ugly
    } else {
      Collection<Recipe> mapRecipes = map.getRecipeList();
      List<Recipe> possibleRecipes =
          mapRecipes.parallelStream()
              .filter(x -> x.getEUt() <= maxEnergy)
              .filter(
                  x -> {
                    return x.getOutputs().stream()
                        // check if any of the outputs are the item we need..
                        .anyMatch(
                            x2 -> {
                              return x2.getMetadata() == selfStack.getMetadata()
                                  && x2.getItem() == selfStack.getItem();
                            });
                  })
              .distinct()
              .collect(Collectors.toList());
      List<List<GTRecipeInput>> possibleInputs =
          possibleRecipes.stream()
              .map(x -> x.getInputs())
              .distinct()
              .collect(Collectors.toList()); // should filter out
      // the recipes with different liquid inputs since we dont care about those i guess

      // there is more than 1 way to craft some item, turn it into a recipe input to not force the
      // player onto a more expensive recipe
      if (possibleInputs.size() != 1) {

        itemCache.put(selfStack, null);
        return Arrays.asList(this.toIngredient());
        // if there are multiple /none crafting options available, just return the original item
        //
      }
      List<GTRecipeInput> recipeInputs = possibleInputs.get(0);
      selected =
          possibleRecipes.stream()
              .filter(x -> x.getInputs() == recipeInputs)
              .findFirst()
              .get(); // should ignore
      // different solders i think
    }

    List<GTRecipeInput> recipeInputs = selected.getInputs();
    itemCache.put(selfStack, selected);

    ItemStack outputStack =
        selected.getOutputs().stream()
            .filter(
                x -> {
                  return x.getItem() == selfStack.getItem()
                      && x.getMetadata() == selfStack.getMetadata();
                })
            .findFirst()
            // this can fail but i hope it will just be sane and not fail a check it
            // has passed previously
            .get();
    double multiplicator = outputStack.getCount() / this.count;

    return recipeInputs.stream()
        .map(
            x -> {
              return x.copyWithAmount((int) Math.ceil(x.getAmount() * multiplicator));
            })
        .collect(Collectors.toList());
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

  // not checked since you should probably only ever use the function above to make it
  public static MaterialCost fromNBT(NBTTagCompound tag) {
    return new MaterialCost(
        tag.getString("resource"),
        tag.getString("type"),
        tag.getInteger("meta"),
        tag.getInteger("count"));
  }
}
