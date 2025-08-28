package supersymmetry.api.rocketry.components;

import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class MaterialCost {
  String resource;
  String type;
  int meta;
  int count;

  public MaterialCost(String resource, String type, int meta, int count) {
    this.resource = resource;
    this.type = type;
    this.meta = meta;
    this.count = count;
  }

  public NBTTagCompound toNBT() {
    NBTTagCompound tag = new NBTTagCompound();
    tag.setString("resource", this.resource);
    tag.setString("type", this.type);
    tag.setInteger("meta", this.meta);
    tag.setInteger("count", this.count);
    return tag;
  }

  public GTRecipeInput toIngredient() {
    Item item = Item.REGISTRY.getObject(new ResourceLocation(this.resource));
    ItemStack stack = new ItemStack(item, this.count);
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
