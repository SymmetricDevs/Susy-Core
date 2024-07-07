package supersymmetry.api.recipes;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.category.GTRecipeCategory;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RecipeMapGroup<R extends RecipeBuilder<R>> extends RecipeMap<R> {

    private final RecipeMap<?>[] recipeMaps;

    public RecipeMapGroup(@NotNull String unlocalizedName, int maxInputs, int maxOutputs, int maxFluidInputs, int maxFluidOutputs, @NotNull R defaultRecipeBuilder, boolean isHidden, @NotNull RecipeMap<?>[] recipeMaps) {
        super(unlocalizedName, maxInputs, maxOutputs, maxFluidInputs, maxFluidOutputs, defaultRecipeBuilder, isHidden);
        this.recipeMaps = recipeMaps;
    }

    @Nonnull
    public static RecipeMapGroup<SimpleRecipeBuilder> create(@NotNull String unlocalizedName, @NotNull RecipeMap<?>[] recipeMaps) {
        AtomicInteger maxInputs = new AtomicInteger(), maxOutputs = new AtomicInteger(),
                maxFluidInputs = new AtomicInteger(), maxFluidOutputs = new AtomicInteger();
        Arrays.stream(recipeMaps).forEach(recipeMap -> {
            maxInputs.set(Math.max(maxInputs.get(), recipeMap.getMaxInputs()));
            maxOutputs.set(Math.max(maxOutputs.get(), recipeMap.getMaxOutputs()));
            maxFluidInputs.set(Math.max(maxFluidInputs.get(), recipeMap.getMaxFluidInputs()));
            maxFluidOutputs.set(Math.max(maxFluidOutputs.get(), recipeMap.getMaxFluidOutputs()));
        });
        return new RecipeMapGroup<>(unlocalizedName, maxInputs.get(), maxOutputs.get(), maxFluidInputs.get(), maxFluidOutputs.get(), new SimpleRecipeBuilder(), true, recipeMaps);
    }

    @Nullable
    @Override
    public Recipe findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, boolean exactVoltage) {
        AtomicReference<Recipe> recipe = new AtomicReference<>();
        Arrays.stream(recipeMaps)
                .parallel()
                .map(recipeMap -> recipeMap.findRecipe(voltage, inputs, fluidInputs, exactVoltage))
                .filter(java.util.Objects::nonNull)
                .findFirst().ifPresent(recipe::set);
        return recipe.get();
    }

    @Nonnull
    @Override
    public Map<GTRecipeCategory, List<Recipe>> getRecipesByCategory() {
        Map<GTRecipeCategory, List<Recipe>> res = new Object2ObjectOpenHashMap<>();
        for (RecipeMap<?> recipeMap : recipeMaps) {
            recipeMap.getRecipesByCategory().forEach((category, recipes) -> {
                List<Recipe> list = res.getOrDefault(category, new java.util.ArrayList<>());
                list.addAll(recipes);
                res.put(category, list);
            });
        }
        return Collections.unmodifiableMap(res);
    }
}
