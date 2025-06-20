package supersymmetry.loaders.recipes.handlers;

import com.cleanroommc.groovyscript.api.IIngredient;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.recipes.*;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.ValidationResult;
import gregtech.integration.groovy.GroovyScriptModule;
import gregtech.loaders.recipe.RecyclingRecipes;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recycling.MaterialRecyclable;
import supersymmetry.api.recycling.Recyclable;
import supersymmetry.api.recycling.toposort.TopologicalSort;
import supersymmetry.mixins.gregtech.OreDictUnifierAccessor;
import supersymmetry.mixins.gregtech.RecipeBuilderAccessor;
import supersymmetry.mixins.gregtech.RecyclingRecipesMixin;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"UnstableApiUsage", "JavadocReference"})
// TODO: Partial recursive recycling data reload
// TODO: Store part of the node geometry so that they can be restored when reloaded via GrS
public class RecyclingManager {

    private static final MutableValueGraph<Recyclable, Fraction> graphStorage = ValueGraphBuilder.directed()
            .allowsSelfLoops(false)
            .nodeOrder(ElementOrder.unordered())
            .expectedNodeCount(8192)
            .build();

    private static final MutableValueGraph<Recyclable, Fraction> backupGraphStorage = ValueGraphBuilder.directed()
            .allowsSelfLoops(false)
            .nodeOrder(ElementOrder.unordered())
            .expectedNodeCount(8192)
            .build();

    public static final Object2ObjectOpenHashMap<Recyclable, Recipe> arcRecipes = new Object2ObjectOpenHashMap<>();

    public static final Object2ObjectOpenHashMap<Recyclable, Recipe> maceratorRecipes = new Object2ObjectOpenHashMap<>();

    /// Copied lots of CEu codes here to parse a recipe properly
    ///
    /// @see RecyclingHandler#getRecyclingIngredients(int, Object...)
    public static void addRecycling(ItemStack output, int outputCount, @NotNull Object... recipe) {

        Char2IntOpenHashMap inputCountMap = new Char2IntOpenHashMap();
        Object2IntMap<Recyclable> ingredients = new Object2IntOpenHashMap<>();

        int itr = 0;
        while (recipe[itr] instanceof String s) {
            for (char c : s.toCharArray()) {
                if (ToolHelper.getToolFromSymbol(c) != null) continue; // skip tools
                int count = inputCountMap.getOrDefault(c, 0);
                inputCountMap.put(c, count + 1);
            }
            itr++;
        }

        char lastChar = ' ';
        for (int i = itr; i < recipe.length; i++) {
            Object ingredient = recipe[i];

            /// Track the current working ingredient symbol
            if (ingredient instanceof Character) {
                lastChar = (char) ingredient;
                continue;
            }

            if (lastChar == ' ') return;
            /// Should never happen if recipe is formatted correctly
            /// In the case that it isn't, this error should be handled
            /// by an earlier method call parsing the recipe.

            Recyclable ing = Recyclable.from(ingredient);

            if (ing.isEmpty()) continue;

            int ingCount = inputCountMap.get(lastChar) * ing.value(ingredient);

            ingredients.put(ing, ingredients.getOrDefault(ing, 0) + ingCount);
        }

        addRecyclingInternal(output, convertIntegerMap(ingredients, outputCount), false, false);
    }

    public static void addRecyclingGroovy(ItemStack output, int outputCount, List<IIngredient> inputs) {
        addRecycling(output, outputCount, inputs.stream()
                .filter(ing -> ing != null)
                .map(ing -> (Object) ing.getMatchingStacks()[0])
                .collect(Collectors.toList()));

        Object2IntMap<Recyclable> ingredients = new Object2IntOpenHashMap<>();

        for (IIngredient input : inputs) {

            Recyclable ing;
            int count;

            if (input == null) continue;

            // TODO: ugly code
            ItemStack[] inputStacks = input.getMatchingStacks();
            if (inputStacks == null || inputStacks.length == 0) continue;

            ItemStack inputStack = inputStacks[0];

            ing = Recyclable.from(inputStack);

            if (ing.isEmpty()) continue;

            count = input.getAmount() * ing.value(inputStack);

            ingredients.put(ing, ingredients.getOrDefault(ing, 0) + count);
        }
        addRecyclingInternal(output, convertIntegerMap(ingredients, outputCount), true, true);
    }

    /// Copied lots of CEu codes here to convert [GTRecipeInput]s properly
    ///
    /// @see RecyclingHandler#getRecyclingIngredients(List, int)
    public static void addRecycling(ItemStack output, int outputCount, List<GTRecipeInput> inputs) {

        Object2IntMap<Recyclable> ingredients = new Object2IntOpenHashMap<>();

        for (GTRecipeInput input : inputs) {

            Recyclable ing;
            int count;

            if (input == null ||
                    input.isNonConsumable() ||
                    input instanceof GTRecipeFluidInput ||
                    input instanceof FluidCellInput) continue;

            // TODO: ugly code
            ItemStack[] inputStacks = input.getInputStacks();
            if (inputStacks == null || inputStacks.length == 0) continue;

            ItemStack inputStack = inputStacks[0];

            ing = Recyclable.from(inputStack);

            if (ing.isEmpty()) continue;

            count = input.getAmount() * ing.value(inputStack);

            ingredients.put(ing, ingredients.getOrDefault(ing, 0) + count);
        }
        addRecyclingInternal(output, convertIntegerMap(ingredients, outputCount), false, false);
    }

    private static Object2ObjectMap<Recyclable, Fraction> convertIntegerMap(Object2IntMap<Recyclable> ingredients, int outputCount) {
        Object2ObjectMap<Recyclable, Fraction> converted = new Object2ObjectOpenHashMap<>();
        for (Recyclable ing : ingredients.keySet()) {
            converted.put(ing, Fraction.getFraction(ingredients.getInt(ing), outputCount));
        }
        return converted;
    }

    private static void addRecyclingInternal(ItemStack output, Object2ObjectMap<Recyclable, Fraction> ingredients, boolean force, boolean scripted) {
        addRecyclingInternal(Recyclable.from(output), ingredients, force, scripted);
    }
    private static void addRecyclingInternal(Recyclable out, Object2ObjectMap<Recyclable, Fraction> ingredients, boolean force, boolean scripted) {
        if (graphStorage.nodes().contains(out)) { // There's already recycling data.
            Set<Recyclable> predecessors = new ObjectArraySet<>(graphStorage.predecessors(out)); // No CMEs today!
            if (force) { // The original recipe is going to get removed, so it's fine to directly replace the data.
                for (Recyclable ing : predecessors) {
                    removeRecyclingRelationInternal(ing, out, scripted);
                }
            } else { // We now need to take the minimum of the two; we don't know which recipe players will use!
                // First, find the intersection of the two sets of ingredients
                Set<Recyclable> intersection = new ObjectArraySet<>(predecessors);
                intersection.retainAll(ingredients.keySet());
                // For each edge from an ingredient in the intersection to the output,
                // we need to take the minimum of the two values.
                for (Recyclable ing : predecessors) {
                    if (intersection.contains(ing)) {
                        Fraction fraction = graphStorage.edgeValue(ing, out);
                        Fraction newFraction = ingredients.get(ing);
                        graphStorage.putEdgeValue(ing, out, fraction.compareTo(newFraction) < 0 ? fraction : newFraction);
                        if (scripted) {
                            backupGraphStorage.putEdgeValue(ing, out, fraction);
                        }
                    } else {
                        // We're deleting the edge.
                        removeRecyclingRelationInternal(ing, out, scripted);
                    }
                }
                return;
            }

        }
        ingredients.forEach((ing, count) -> graphStorage.putEdgeValue(
                ing, out, count
        ));
    }


        private static void removeRecyclingRelationInternal(Recyclable ing, Recyclable out, boolean scripted) {
        if (scripted) {
            backupGraphStorage.putEdgeValue(ing, out, graphStorage.edgeValue(ing, out));
        } else {
            graphStorage.removeEdge(ing, out);
        }
    }

    public static void registerRecycling(ItemStack output, Map<Object, Integer> ingredients, int outputCount, boolean force, boolean scripted) {
        // Convert map into Object2IntMap<Recyclable>
        Object2IntMap<Recyclable> recyclables = new Object2IntOpenHashMap<>();
        for (Map.Entry<Object, Integer> entry : ingredients.entrySet()) {
            recyclables.put(Recyclable.from(entry.getKey()), entry.getValue());
        }
        addRecyclingInternal(output, convertIntegerMap(recyclables, outputCount), force, scripted);
    }

    // Intended for GroovyScript users
    public static void registerRecycling(ItemStack output, Map<Object, Integer> ingredients, int outputCount) {
        registerRecycling(output, ingredients, outputCount, true, true);
    }

    public static void registerRecycling(ItemStack output, Map<Object, Integer> ingredients, int outputCount, boolean force) {
        registerRecycling(output, ingredients, outputCount, force, true);
    }

    public static void registerOre(ItemStack itemStack, ItemMaterialInfo materialInfo) {
        Recyclable out = Recyclable.from(itemStack);
        materialInfo.getMaterials().forEach(ms -> graphStorage.putEdgeValue(
                Recyclable.from(ms), out, Fraction.getFraction((int) ms.amount, 1)
        ));
    }

    private static void registerOreInternal(ItemStack itemStack, ItemMaterialInfo materialInfo) {
        if (itemStack.isEmpty()) return;
        OreDictUnifierAccessor.getUnificationInfo().put(new ItemAndMetadata(itemStack), materialInfo);
    }

    public static void init() {
        GroovyScriptModule.loadMetaItemBracketHandler();
        MinecraftForge.EVENT_BUS.post(new PreRecyclingEvent());

        /// Register all unification data
        TopologicalSort.topologicalSort(graphStorage, null).forEach(RecyclingManager::registerRecyclingData);
    }

    private static void registerRecyclingData(Recyclable output) {
        if (output instanceof MaterialRecyclable) return; /// Skip materials

        ItemStack outputStack = output.asStack();
        OrePrefix prefix = OreDictUnifier.getPrefix(outputStack);
        if (prefix != null && prefix.getMaterialAmount(null) > 0) {
            return; /// MetaPrefixItems are handled by ceu itself
        }

        Object2ObjectOpenHashMap<Material, Fraction> mStacks = new Object2ObjectOpenHashMap<>();

        graphStorage.predecessors(output).forEach(
                /// Theoretically no default value is needed. However, it has to be here is for CrL compat
                /// since [MutableValueGraph#edgeValue] returns [Optional] in newer guava versions
                ing -> ing.addToMStack(mStacks, graphStorage.edgeValueOrDefault(ing, output, Fraction.ZERO))
        );

        if (mStacks.isEmpty()) return;

        registerOreInternal(outputStack,
                new ItemMaterialInfo(mStacks.entrySet().stream()
                        .map(e -> new MaterialStack(e.getKey(), e.getValue().intValue()))
                        .sorted(Comparator.comparingLong(m -> -m.amount))
                        .collect(Collectors.toList())
                ));
    }

    /// Copied and modified from CEu
    ///
    /// @see RecyclingHandler#addItemStackToMaterialStacks(ItemStack, Object2LongMap, int)
    public static void addItemStackToMaterialStacks(@NotNull ItemStack itemStack,
                                                    @NotNull Object2ObjectMap<Material, Fraction> receiver,
                                                    @NotNull Fraction amount) {
        // First try to get ItemMaterialInfo
        ItemMaterialInfo info = OreDictUnifier.getMaterialInfo(itemStack);
        if (info != null) {
            for (MaterialStack ms : info.getMaterials()) {
                if (!(ms.material instanceof MarkerMaterial)) {
                    addMaterialStack(receiver, amount, ms);
                }
            }
            return;
        }

        // Then try to get a single Material (UnificationEntry needs this, for example)
        MaterialStack materialStack = OreDictUnifier.getMaterial(itemStack);
        if (materialStack != null && !(materialStack.material instanceof MarkerMaterial)) {
            addMaterialStack(receiver, amount, materialStack);
        }

        // Gather any secondary materials if this item has an OrePrefix
        OrePrefix prefix = OreDictUnifier.getPrefix(itemStack);
        if (prefix != null && !prefix.secondaryMaterials.isEmpty()) {
            for (MaterialStack ms : prefix.secondaryMaterials) {
                addMaterialStack(receiver, amount, ms);
            }
        }
    }

    /// Copied and modified from CEu
    /// Adds a MaterialStack to a map of `<Material, Quantity>`
    ///
    /// @param receiver the map to add to
    /// @param amount   the number of items in the stack
    /// @param ms       the stack to add
    /// @see RecyclingHandler#addMaterialStack(Object2LongMap, int, MaterialStack)
    private static void addMaterialStack(@NotNull Object2ObjectMap<Material, Fraction> receiver,
                                         @NotNull Fraction amount, @NotNull MaterialStack ms) {
        Fraction oldAmount = receiver.getOrDefault(ms.material, Fraction.ZERO);
        receiver.put(ms.material, oldAmount.add(amount.multiplyBy(Fraction.getFraction((int) ms.amount, 1))));
    }

    /// Steps for resetting:
    /// ┌─ 1. remove related recipes from [arcRecipes], [maceratorRecipes], and related recipe maps.
    /// │  2. remove edges connecting old ingredients to the output from [graphStorage].
    /// │  3. connect new edges according to the new ingredients.
    /// │  4. calculate & override old unification data in [OreDictUnifier].
    /// │  5. registering new recycling recipes and put them in [arcRecipes] & [maceratorRecipes].
    /// └─ 6. call the same method recursively for all downstream ingredients.
    ///
    /// For refreshing, only steps 1, 4, 5, and 6 are performed, since the geometry of the node is not changed.
    private static void resetRecyclingRecursivelyInternal(Recyclable out, boolean refreshOnly) {
        removeByRecyclable(out);    /// Step 1

        if (!refreshOnly) {
            Object2ObjectMap<Recyclable, Fraction> ingredients = new Object2ObjectOpenHashMap<>();
            for (Recyclable ing : graphStorage.predecessors(out)) {
                ingredients.put(ing, graphStorage.edgeValueOrDefault(ing, out, Fraction.ZERO));
            }
            addRecyclingInternal(out, ingredients, true, true); /// Step 2 & Step 3
        }

        registerRecyclingData(out); /// Step 4

        ItemMaterialInfo info = OreDictUnifier.getMaterialInfo(out.asStack());
        if (info == null) return;
        RecyclingRecipes.registerRecyclingRecipes(out.asStack(), info.getMaterials(), false, null);   /// Step 5

        graphStorage.successors(out).forEach(ing -> resetRecyclingRecursivelyInternal(ing, true));  /// Step 6
    }

    /// Only removes recipes from [RecipeMaps#MACERATOR_RECIPES] and [RecipeMaps#ARC_FURNACE_RECIPES]
    ///
    /// @implNote as long as [RecipeMap#removeRecipe(Recipe)] is called in a groovy script it will be considered as a GrS recipe
    public static void removeByRecyclable(Recyclable recyclable) {
        Recipe recipe = arcRecipes.remove(recyclable);
        if (recipe != null) {
            RecipeMaps.ARC_FURNACE_RECIPES.removeRecipe(recipe);
        }

        recipe = maceratorRecipes.remove(recyclable);
        if (recipe != null) {
            RecipeMaps.MACERATOR_RECIPES.removeRecipe(recipe);
        }
    }

    /// Mixin Hook, with lots of code copied from CEu
    ///
    /// @see RecyclingRecipesMixin
    /// @see RecipeBuilder#buildAndRegister()
    public static void buildAndRegister(RecipeBuilder<?> recipeBuilder, ItemStack output) {
        var accessor = (RecipeBuilderAccessor) recipeBuilder;

        var buildAction = accessor.getOnBuildAction();
        if (buildAction != null) {
            buildAction.accept(recipeBuilder);
        }

        ValidationResult<Recipe> validationResult = recipeBuilder.build();
        RecipeMap<?> recipeMap = accessor.getRecipeMap();
        recipeMap.addRecipe(validationResult);

        /// Extra code to register recycling recipes
        if (validationResult.getType() == EnumValidationResult.VALID) {
            Recipe recipe = validationResult.getResult();
            Recyclable recyclable = Recyclable.from(output);

            if (recipeMap == RecipeMaps.ARC_FURNACE_RECIPES) {
                arcRecipes.put(recyclable, recipe);
            } else if (recipeMap == RecipeMaps.MACERATOR_RECIPES) {
                maceratorRecipes.put(recyclable, recipe);
            }
        }
    }

    public static void reload() {
        // For each node in backup, reset the node in the primary graph.
        for (Recyclable out : backupGraphStorage.nodes()) {
            Set<Recyclable> currentPredecessors = new ObjectArraySet<>(graphStorage.predecessors(out)); // No CMEs today!
            for (Recyclable ing : currentPredecessors) {
                graphStorage.removeEdge(ing, out);
            }
            Set<Recyclable> backupPredecessors = new ObjectArraySet<>(backupGraphStorage.predecessors(out)); // No CMEs today!
            for (Recyclable ing : backupPredecessors) {
                graphStorage.putEdgeValue(ing, out, backupGraphStorage.edgeValue(ing, out));
            }
        }

        backupGraphStorage.nodes().clear();
        backupGraphStorage.edges().clear();
        MinecraftForge.EVENT_BUS.post(new PreRecyclingEvent());

        // Now, the backup graph should be filled up again with the same unused unscripted data, while the real graph storage has scripted but reloaded data.
        for (Recyclable out : backupGraphStorage.nodes()) {
            resetRecyclingRecursivelyInternal(out, false);
        }
    }

    public static class PreRecyclingEvent extends Event {
    }
}
