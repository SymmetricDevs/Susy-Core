package supersymmetry.common.command;

import com.google.gson.*;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.common.crafting.GTFluidCraftingIngredient;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import supersymmetry.api.SusyLog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CommandRecipemapDump extends CommandBase {

    public static JsonElement nbtToJson(NBTBase nbt) {
        if (nbt == null) {
            return JsonNull.INSTANCE;
        }
        if (nbt instanceof NBTTagCompound comp) {
            JsonObject jo = new JsonObject();
            for (String key : comp.getKeySet()) {
                jo.add(key, nbtToJson(comp.getTag(key)));
            }
            return jo;
        } else if (nbt instanceof NBTTagList list) {
            JsonArray ja = new JsonArray();
            for (int i = 0; i < list.tagCount(); i++) {
                ja.add(nbtToJson(list.get(i)));
            }
            return ja;
        } else if (nbt instanceof NBTTagByte) return new JsonPrimitive(((NBTTagByte) nbt).getByte());
        else if (nbt instanceof NBTTagShort) return new JsonPrimitive(((NBTTagShort) nbt).getShort());
        else if (nbt instanceof NBTTagInt) return new JsonPrimitive(((NBTTagInt) nbt).getInt());
        else if (nbt instanceof NBTTagLong) return new JsonPrimitive(((NBTTagLong) nbt).getLong());
        else if (nbt instanceof NBTTagFloat) return new JsonPrimitive(((NBTTagFloat) nbt).getFloat());
        else if (nbt instanceof NBTTagDouble)
            return new JsonPrimitive(((NBTTagDouble) nbt).getDouble());
        else if (nbt instanceof NBTTagByteArray)
            return new JsonPrimitive(nbt.toString());
        else if (nbt instanceof NBTTagString)
            return new JsonPrimitive(((NBTTagString) nbt).getString());
        else if (nbt instanceof NBTTagIntArray)
            return new JsonPrimitive(nbt.toString());
        else if (nbt instanceof NBTTagLongArray)
            return new JsonPrimitive(nbt.toString());
        else throw new IllegalArgumentException("weird nbt class" + nbt.getClass());
    }

    @Override
    public String getName() {
        return "recipemapdump";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "susy.command.recipemapdump.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
            throws CommandException {
        // List<String> args2 = Stream.of(args).collect(Collectors.toList());
        // TODO make it accept a list of required items ( like "recipemaps", "crafting") and output it in
        // a file name that is a combination of the arguments, dump everything if no args passed

        // also this command takes seconds to run :C
        JsonObject root = new JsonObject();

        root.add("items", this.dumpItems());
        root.add("fluids", this.dumpFluids());
        root.add("oreDict", this.dumpOreDict());
        root.add("recipemaps", gtRecipeMaps());
        root.add("smelting", this.dumpSmeltingRecipes());
        root.add("crafting", this.dumpCraftingRecipes());
        root.add("gtMTEs", this.dumpMachines());

        writeJsonToRoot(root, "recipedump", sender);
    }

    public JsonElement dumpOreDict() {
        JsonObject allOresObj = new JsonObject();
        String[] ores = OreDictionary.getOreNames();
        for (String ore : ores) {
            JsonArray oreArray = new JsonArray();
            for (ItemStack stack : OreDictionary.getOres(ore)) {
                oreArray.add(stackToJson(stack));
            }
            allOresObj.add(ore, oreArray);
        }
        return allOresObj;
    }

    public JsonElement dumpFluids() {
        JsonArray allFluidsObj = new JsonArray();
        Map<String, Fluid> fluids = FluidRegistry.getRegisteredFluids();
        for (Fluid fluid : fluids.values()) {
            allFluidsObj.add(fullFluidInformation(fluid));
        }
        return allFluidsObj;
    }

    public JsonElement dumpItems() {
        JsonArray allItemsObj = new JsonArray();
        Collection<Item> items = ForgeRegistries.ITEMS.getValuesCollection();
        for (Item item : items) {
            if (item.getHasSubtypes()) {
                NonNullList<ItemStack> subItems = NonNullList.create();
                for (CreativeTabs tab : item.getCreativeTabs()) {
                    if (tab != null) {
                        item.getSubItems(tab, subItems);
                    }
                }
                for (ItemStack stack : subItems) {
                    allItemsObj.add(fullStackInformation(stack));
                }
            } else {
                allItemsObj.add(fullStackInformation(new ItemStack(item)));
            }
        }
        return allItemsObj;
    }

    public void writeJsonToRoot(JsonObject obj, String name, ICommandSender sender) {
        File root = FMLCommonHandler.instance().getSavesDirectory().getParentFile();
        File outFile = new File(root, name + ".json");
        try (FileWriter writer = new FileWriter(outFile)) {
            new Gson().toJson(obj, writer);
            if (sender != null) {
                sender.sendMessage(new TextComponentString("wrote to " + root.getAbsolutePath()));
            }
        } catch (IOException e) {
            SusyLog.logger.error("failed to write the json: " + e);
        }
    }

    public JsonObject gtRecipeMaps() {
        JsonObject allRecipeMapsObj = new JsonObject();
        for (RecipeMap<?> map : RecipeMap.getRecipeMaps()) {
            JsonObject recipemapObj = new JsonObject();

            recipemapObj.addProperty("translationKey", map.getTranslationKey());
            if (map.getSound() == null) {
                recipemapObj.add("sound", JsonNull.INSTANCE);
            } else {
                recipemapObj.addProperty("sound", map.getSound().getRegistryName().toString());
            }
            recipemapObj.addProperty("maxFluidInputs", map.getMaxFluidInputs());
            recipemapObj.addProperty("maxInputs", map.getMaxInputs());
            recipemapObj.addProperty("maxOutputs", map.getMaxOutputs());
            recipemapObj.addProperty("maxFluidOutputs", map.getMaxFluidOutputs());
            recipemapObj.addProperty("unlocalizedName", map.getUnlocalizedName());
            JsonArray recipes = new JsonArray();
            for (Recipe recipe : map.getRecipeList()) {
                JsonObject recipeobj = new JsonObject();
                // general recipe information
                recipeobj.addProperty("class", recipe.getClass().toString());
                recipeobj.addProperty("EUt", recipe.getEUt());
                recipeobj.addProperty("duration", recipe.getDuration());
                recipeobj.addProperty("isCTRecipe", recipe.getIsCTRecipe());
                recipeobj.addProperty("propertyCount", recipe.getPropertyCount());
                recipeobj.addProperty("unhiddenPropertyCount", recipe.getUnhiddenPropertyCount());
                JsonArray propertyArray = new JsonArray();
                // properties, not sure if anyone needs this but why not
                for (Entry<RecipeProperty<?>, Object> propEntry : recipe.getPropertyValues()) {
                    JsonObject propdesc = new JsonObject();
                    propdesc.addProperty("propertyKey", propEntry.getKey().getKey());
                    propdesc.addProperty("propertyClass", propEntry.getKey().getClass().toString());
                    propdesc.addProperty("propertyHash", propEntry.getKey().hashCode());
                    propdesc.addProperty("propertyValueClass", propEntry.getValue().getClass().toString());
                    propertyArray.add(propdesc);
                }
                recipeobj.add("properties", propertyArray);
                recipeobj.addProperty("categoryName", recipe.getRecipeCategory().getName());
                recipeobj.addProperty(
                        "categoryTranslationKey", recipe.getRecipeCategory().getTranslationKey());
                recipeobj.addProperty("categoryUniqueID", recipe.getRecipeCategory().getUniqueID());
                recipeobj.addProperty("categoryModID", recipe.getRecipeCategory().getModid());

                // items and fluids
                {
                    JsonArray itemInputs = new JsonArray();
                    if (recipe.getInputs() != null) {
                        for (GTRecipeInput recipeInput : recipe.getInputs()) {
                            JsonObject input = new JsonObject();
                            input.addProperty("class", recipeInput.getClass().toString());
                            input.addProperty("amount", recipeInput.getAmount());
                            input.addProperty("oreDict", recipeInput.getOreDict());
                            input.addProperty("sortingOrder", recipeInput.getSortingOrder());
                            input.addProperty("nonConsumable", recipeInput.isNonConsumable());
                            JsonArray inputstacks = new JsonArray();
                            if (recipeInput.getInputStacks() != null) {
                                for (ItemStack stackInput : recipeInput.getInputStacks()) {
                                    inputstacks.add(this.stackToJson(stackInput));
                                }
                            }
                            input.add("inputStacks", inputstacks);
                            input.add("inputFluidStack", this.fluidStackToJson(recipeInput.getInputFluidStack()));

                            itemInputs.add(input);
                        }
                    }

                    JsonArray fluidInputs = new JsonArray();
                    if (recipe.getFluidInputs() != null) {
                        for (GTRecipeInput recipeInput : recipe.getFluidInputs()) {
                            JsonObject input = new JsonObject();
                            input.addProperty("class", recipeInput.getClass().toString());
                            input.addProperty("amount", recipeInput.getAmount());
                            input.addProperty("oreDict", recipeInput.getOreDict());
                            input.addProperty("sortingOrder", recipeInput.getSortingOrder());
                            input.addProperty("nonConsumable", recipeInput.isNonConsumable());
                            JsonArray inputstacks = new JsonArray();
                            if (recipeInput.getInputStacks() != null) {
                                for (ItemStack stackInput : recipeInput.getInputStacks()) {
                                    inputstacks.add(this.stackToJson(stackInput));
                                }
                            }
                            input.add("inputStacks", inputstacks);
                            input.add("inputFluidStack", this.fluidStackToJson(recipeInput.getInputFluidStack()));

                            fluidInputs.add(input);
                        }
                    }
                    JsonArray fluidOutputs = new JsonArray();
                    if (recipe.getFluidOutputs() != null) {
                        recipe.getFluidOutputs().forEach(x -> fluidOutputs.add(this.fluidStackToJson(x)));
                    }
                    JsonArray itemOutputs = new JsonArray();
                    if (recipe.getOutputs() != null) {
                        recipe.getOutputs().forEach(x -> itemOutputs.add(this.stackToJson(x)));
                    }
                    if (recipe.getChancedOutputs() != null) {
                        JsonObject chancedOutputObj = new JsonObject();
                        JsonArray chancedOutputs = new JsonArray();
                        var chanced = recipe.getChancedOutputs();
                        chanced.getChancedEntries().forEach(x -> {
                            var stack = this.stackToJson(x.getIngredient());
                            stack.addProperty("chance", x.getChance());
                            chancedOutputs.add(stack);
                        });
                        chancedOutputObj.addProperty("logic", I18n.format(chanced.getChancedOutputLogic().getTranslationKey()));
                        recipeobj.add("chancedOutputs", chancedOutputs);
                    }
                    if (recipe.getChancedFluidOutputs() != null) {
                        JsonObject chancedOutputObj = new JsonObject();
                        JsonArray chancedOutputs = new JsonArray();
                        var chanced = recipe.getChancedFluidOutputs();
                        chanced.getChancedEntries().forEach(x -> {
                            JsonObject stack = this.fluidStackToJson(x.getIngredient());
                            stack.addProperty("chance", x.getChance());
                            chancedOutputs.add(stack);
                        });
                        chancedOutputObj.addProperty("logic", I18n.format(chanced.getChancedOutputLogic().getTranslationKey()));
                        recipeobj.add("chancedFluidOutputs", chancedOutputs);
                    }
                    recipeobj.add("inputsFluid", fluidInputs);
                    recipeobj.add("inputs", itemInputs);
                    recipeobj.add("outputs", itemOutputs);
                    recipeobj.add("fluidOutputs", fluidOutputs);
                }
                recipes.add(recipeobj);
            }
            recipemapObj.add("recipes", recipes);
            allRecipeMapsObj.add(map.getUnlocalizedName(), recipemapObj);
        }
        return allRecipeMapsObj;
    }

    public JsonObject stackToJson(ItemStack stack) {
        JsonObject stackObj = new JsonObject();
        if (stack == null || stack.getItem() == null) return stackObj;

        stackObj.addProperty("resource", stack.getItem().getRegistryName().toString());
        stackObj.addProperty("count", stack.getCount());
        stackObj.addProperty("metadata", stack.getMetadata());
        stackObj.addProperty("itemDamage", stack.getItemDamage());
        if (stack.getTagCompound() != null) {
            stackObj.add("nbt", CommandRecipemapDump.nbtToJson(stack.getTagCompound()));
        }
        return stackObj;
    }

    public JsonObject fullStackInformation(ItemStack stack) {
        JsonObject stackObj = new JsonObject();
        if (stack == null || stack.getItem() == null) return stackObj;

        stackObj.addProperty("displayName", stack.getDisplayName());
        stackObj.addProperty("translationKey", stack.getTranslationKey());
        stackObj.addProperty("resource", stack.getItem().getRegistryName().toString());
        stackObj.addProperty("maxDamage", stack.getMaxDamage());
        stackObj.addProperty("itemDamage", stack.getItemDamage());
        stackObj.addProperty("repairCost", stack.getRepairCost());
        stackObj.addProperty("hasSubtypes", stack.getHasSubtypes());
        stackObj.addProperty("maxStackSize", stack.getMaxStackSize());
        stackObj.addProperty("rarity", stack.getRarity().toString());
        stackObj.addProperty("itemClass", stack.getItem().getClass().toString());
        stackObj.addProperty("itemTranslationKey", stack.getItem().getTranslationKey());
        return stackObj;
    }

    public JsonElement fullFluidInformation(Fluid fluid) {
        JsonObject fluidObj = new JsonObject();
        if (fluid == null) return JsonNull.INSTANCE;
        fluidObj.addProperty("fluidName", fluid.getName());
        fluidObj.addProperty("unlocalizedName", fluid.getUnlocalizedName());
        fluidObj.addProperty("localizedName", fluid.getLocalizedName(new FluidStack(fluid, 1)));
        fluidObj.addProperty("fluidColor", fluid.getColor());

        fluidObj.addProperty("fluidDensity", fluid.getDensity());
        fluidObj.addProperty("fluidRarity", fluid.getRarity().toString());
        fluidObj.addProperty("fluidViscosity", fluid.getViscosity());
        fluidObj.addProperty("fluidLuminosity", fluid.getLuminosity());
        fluidObj.addProperty("fluidTemperature", fluid.getTemperature());
        return fluidObj;
    }

    public JsonObject fluidStackToJson(FluidStack fluidStack) {
        JsonObject stackObj = new JsonObject();
        if (fluidStack == null) return stackObj;
        if (fluidStack.getFluid() == null) return stackObj;
        stackObj.addProperty("type", "FluidStack");
        stackObj.addProperty("unlocalizedName", fluidStack.getUnlocalizedName());
        stackObj.addProperty("specificLocalizedName", fluidStack.getLocalizedName());
        stackObj.addProperty("amount", fluidStack.amount);

        return stackObj;
    }

    public JsonObject ingredientToJson(Ingredient ingredient) {
        JsonObject ingredientObj = new JsonObject();

        JsonArray possibleInputs = new JsonArray();
        for (var v : ingredient.getMatchingStacks()) {
            possibleInputs.add(this.stackToJson(v));
        }
        ingredientObj.addProperty("class", ingredient.getClass().getName());
        ingredientObj.add("validInputs", possibleInputs);
        if (ingredient instanceof GTFluidCraftingIngredient fluid) {
            ingredientObj.add("fluid", fluidStackToJson(fluid.getFluidStack()));
        }
        return ingredientObj;
    }

    public JsonElement dumpCraftingRecipes() {
        JsonArray root = new JsonArray();
        for (var cr : ForgeRegistries.RECIPES.getValuesCollection()) {
            JsonObject recipeobj = new JsonObject();
            var id = cr.getRegistryName();
            if (id == null) continue;
            recipeobj.addProperty("id", id.toString());
            recipeobj.addProperty("isDynamic", cr.isDynamic());
            recipeobj.addProperty("class", cr.getClass().toString());
            recipeobj.addProperty("group", cr.getGroup());
            recipeobj.addProperty("registryName", cr.getRegistryName().toString());
            if (cr instanceof IShapedRecipe shaped) {
                recipeobj.addProperty("type", "shaped");
                recipeobj.add("recipe", this.shapedToJson(shaped));
            } else if (cr instanceof ShapelessRecipes shapeless) {
                recipeobj.addProperty("type", "shapeless");
                recipeobj.add("recipe", this.shapelessToJson(shapeless));
                // } else if (cr instanceof GTShapedOreRecipe shapedore) {
                // recipeobj.addProperty("type", "gtShaped");
                //
            } else if (cr instanceof ShapelessOreRecipe oreRecipe) {
                recipeobj.addProperty("type", "shapelessOre");
                recipeobj.add("recipe", this.shapelessOreToJson(oreRecipe));
            } else {
                recipeobj.addProperty("type", "unknown");
                SusyLog.logger.warn("unknown type of {}", cr.getClass().getName());
                recipeobj.add("recipe", JsonNull.INSTANCE);
            }
            recipeobj.add("output", stackToJson(cr.getRecipeOutput()));
            root.add(recipeobj);
        }

        return root;
    }

    public JsonElement dumpSmeltingRecipes() {
        JsonArray smeltingArray = new JsonArray();
        Map<ItemStack, ItemStack> smeltMap = FurnaceRecipes.instance().getSmeltingList();

        for (ItemStack input : smeltMap.keySet()) {
            ItemStack output = smeltMap.get(input);
            if (output == null) continue; // just in case
            JsonObject smeltJson = new JsonObject();
            smeltJson.add("input", stackToJson(input));
            smeltJson.add("output", stackToJson(output));
            smeltingArray.add(smeltJson);
        }
        return smeltingArray;
    }

    public JsonElement dumpMachines() {
        var root = new JsonObject();
        for (ResourceLocation key : GregTechAPI.MTE_REGISTRY.getKeys()) {
            var machineObj = new JsonObject();
            MetaTileEntity machine = GregTechAPI.MTE_REGISTRY.getObject(key);
            machineObj.addProperty("class", machine.getClass().toString());
            machineObj.addProperty("metaName", machine.getMetaName());
            machineObj.addProperty("isController", machine instanceof MultiblockControllerBase);
            if (machine instanceof WorkableTieredMetaTileEntity tiered) {
                machineObj.addProperty("tier", tiered.getTier());
                machineObj.addProperty(
                        "recipemapName",
                        tiered.getRecipeMap().getUnlocalizedName()); // the recipe maps are dumped
                machineObj.addProperty("workable", tiered.getRecipeLogic().getName());
                machineObj.addProperty(
                        "workableParallelLogicType", tiered.getRecipeLogic().getParallelLogicType().toString());
            }
            root.add(key.toString(), machineObj);
        }

        return root;
    }

    private JsonElement shapedToJson(IShapedRecipe shaped) {
        int width = shaped.getRecipeWidth();
        int height = shaped.getRecipeHeight();
        JsonObject root = new JsonObject();
        List<Ingredient> ingredients = shaped.getIngredients();

        // map ingredient (by registry name) -> a single ASCII char
        Map<Ingredient, Character> keyMap = new LinkedHashMap<>();
        char nextChar = 'A';

        StringBuilder patternBuilder = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                var ing = ingredients.get(y * width + x);
                if (ing == null || ing.getMatchingStacks() == null || ing.getMatchingStacks().length == 0) {
                    patternBuilder.append(' '); // empty slot
                } else {

                    if (!keyMap.containsKey(ing)) {
                        keyMap.put(ing, nextChar);
                        nextChar++;
                    }
                    patternBuilder.append(keyMap.get(ing));
                }
            }
            patternBuilder.append("\n");
        }
        // the pattern should end up like "A A\n B \nC C" or something
        var keymap = new JsonObject();
        for (var e : keyMap.entrySet()) {
            keymap.add(String.valueOf(e.getValue()), ingredientToJson(e.getKey()));
            // results in something like keymap : { "A": { class:"..." validInputs: [ {stack1},{stack2}]}}
        }
        var shape = new JsonArray();
        for (String line : patternBuilder.toString().split("\n")) {
            if (line.length() != 0) {
                shape.add(line);
            }
        }
        root.add("keymap", keymap);
        root.add("shape", shape);
        // root.add("isClearing", shaped.getRecipeOutput());

        return root;
    }

    private JsonElement shapelessToJson(ShapelessRecipes shapeless) {
        JsonArray ingredients = new JsonArray();
        var root = new JsonObject();
        for (var ingredient : shapeless.getIngredients()) {
            if (ingredient != null && ingredient.getMatchingStacks() != null && ingredient.getMatchingStacks().length != 0) {
                ingredients.add(ingredientToJson(ingredient));
            }
        }
        root.add("ingredients", ingredients);
        return root;
    }

    private JsonElement shapelessOreToJson(ShapelessOreRecipe shapeless) {
        JsonArray ingredients = new JsonArray();
        var root = new JsonObject();
        for (var ingredient : shapeless.getIngredients()) {
            ingredients.add(ingredientToJson(ingredient));
        }
        root.add("ingredients", ingredients);
        return root;
    }

}
