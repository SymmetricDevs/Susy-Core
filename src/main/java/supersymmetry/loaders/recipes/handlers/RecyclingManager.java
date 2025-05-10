package supersymmetry.loaders.recipes.handlers;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.recipes.FluidCellInput;
import gregtech.api.recipes.RecyclingHandler;
import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.common.metatileentities.MetaTileEntities;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recycling.MaterialRecyclable;
import supersymmetry.api.recycling.Recyclable;
import supersymmetry.api.recycling.toposort.TopologicalSort;
import supersymmetry.mixins.gregtech.OreDictUnifierAccessor;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"UnstableApiUsage", "JavadocReference"})
// TODO: GrS Ingredient compat
// TODO: An event system for registering recycling info before / after the unification data
// TODO: Partial recursive recycling data reload
public class RecyclingManager {

    private static final MutableValueGraph<Recyclable, Fraction> graphStorage = ValueGraphBuilder.directed()
            .allowsSelfLoops(false)
            .nodeOrder(ElementOrder.unordered())
            .expectedNodeCount(8192)
            .build();

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

            /// Should never happen if recipe is formatted correctly
            /// In the case that it isn't, this error should be handled
            /// by an earlier method call parsing the recipe.
            if (lastChar == ' ') return;

            Recyclable ing = Recyclable.from(ingredient);

            if (ing.isEmpty()) continue;

            int ingCount = inputCountMap.get(lastChar) * ing.value(ingredient);

            ingredients.put(ing, ingredients.getOrDefault(ing, 0) + ingCount);
        }

        addRecyclingInternal(output, ingredients, outputCount);
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
        addRecyclingInternal(output, ingredients, outputCount);
    }

    private static void addRecyclingInternal(ItemStack output, Object2IntMap<Recyclable> ingredients, int outputCount) {
        Recyclable out = Recyclable.from(output);
        if (output.isItemEqual(MetaTileEntities.WORKBENCH.getStackForm())) {
            System.out.println("haha you're caught");
        }
        ingredients.forEach((ing, count) -> graphStorage.putEdgeValue(
                ing, out, Fraction.getFraction(count, outputCount)
        ));
    }

    public static void registerOre(ItemStack itemStack, ItemMaterialInfo materialInfo) {
        Recyclable out = Recyclable.from(itemStack);
        materialInfo.getMaterials().forEach(ms -> graphStorage.putEdgeValue(
                Recyclable.from(ms), out, Fraction.getFraction((int) ms.amount, 1)
        ));
    }

    private static void registerOreInternal(ItemStack itemStack, ItemMaterialInfo materialInfo) {
        if (itemStack.isItemEqual(new ItemStack(Blocks.CHEST))) {
            System.out.println("haha you're caught");
        }
        if (itemStack.isEmpty()) return;
        OreDictUnifierAccessor.getUnificationInfo().put(new ItemAndMetadata(itemStack), materialInfo);
    }

    public static void init() {

        /// For testing
//        addRecycling(new ItemStack(Items.SADDLE), 1, "XXX", "XYX",
//                "XXX", 'X', new ItemStack(Items.CAKE), 'Y',
//                new UnificationEntry(OrePrefix.plate, Materials.Olivine));
//
//        addRecycling(new ItemStack(Items.CAKE), 1, "XXX", "XYX",
//                "XXX", 'X', new UnificationEntry(OrePrefix.foil, Materials.Zinc), 'Y',
//                new UnificationEntry(OrePrefix.plate, Materials.Olivine));

        /// Register all unification data
        TopologicalSort.topologicalSort(graphStorage, null).forEach(output -> {

            if (output instanceof MaterialRecyclable) return; /// Skip materials

            ItemStack outputStack = output.asStack();
            OrePrefix prefix = OreDictUnifier.getPrefix(outputStack);
            if (prefix != null && prefix.getMaterialAmount(null) > 0)
                return; /// MetaPrefixItems are handled by ceu itself

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
        });
    }

    /// Copied and modified from VEu
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
}
