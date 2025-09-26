package supersymmetry.api.recipes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.category.GTRecipeCategory;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class RecipeMapGroup<R extends RecipeBuilder<R>> extends RecipeMap<R> {

    protected final RecipeMap<?>[] recipeMaps;

    protected RecipeMapGroup(@NotNull String unlocalizedName, int maxInputs, int maxOutputs, int maxFluidInputs,
                             int maxFluidOutputs, @NotNull R defaultRecipeBuilder, boolean isHidden,
                             @NotNull RecipeMap<?>[] recipeMaps) {
        super(unlocalizedName, maxInputs, maxOutputs, maxFluidInputs, maxFluidOutputs, defaultRecipeBuilder, isHidden);
        this.recipeMaps = recipeMaps;
    }

    @Nonnull
    public static RecipeMapGroup<SimpleRecipeBuilder> create(@NotNull String unlocalizedName,
                                                             @NotNull RecipeMap<?>... recipeMaps) {
        int maxInputs = 0, maxOutputs = 0, maxFluidInputs = 0, maxFluidOutputs = 0;
        for (RecipeMap<?> recipeMap : recipeMaps) {
            maxInputs = Math.max(maxInputs, recipeMap.getMaxInputs());
            maxOutputs = Math.max(maxOutputs, recipeMap.getMaxOutputs());
            maxFluidInputs = Math.max(maxFluidInputs, recipeMap.getMaxFluidInputs());
            maxFluidOutputs = Math.max(maxFluidOutputs, recipeMap.getMaxFluidOutputs());
        }
        return new RecipeMapGroup<>(unlocalizedName, maxInputs, maxOutputs, maxFluidInputs, maxFluidOutputs,
                new SimpleRecipeBuilder(), true, recipeMaps);
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
            res.putAll(recipeMap.getRecipesByCategory());
        }
        return Collections.unmodifiableMap(res);
    }
}
