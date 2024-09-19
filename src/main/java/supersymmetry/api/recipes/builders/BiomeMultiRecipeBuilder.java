package supersymmetry.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.recipes.properties.BiomeMultiProperty;
import supersymmetry.api.recipes.properties.BiomeMultiPropertyList;

import java.util.ArrayList;

public class BiomeMultiRecipeBuilder extends RecipeBuilder<BiomeMultiRecipeBuilder>{
    public BiomeMultiRecipeBuilder() {
    }

    @SuppressWarnings("unused")
    public BiomeMultiRecipeBuilder(Recipe recipe, RecipeMap<BiomeMultiRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public BiomeMultiRecipeBuilder(RecipeBuilder<BiomeMultiRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    public BiomeMultiRecipeBuilder copy() {
        return new BiomeMultiRecipeBuilder(this);
    }

    public BiomeMultiRecipeBuilder biome(String biome) {
        return biome(biome, false);
    }

    public BiomeMultiRecipeBuilder biome(String biome, boolean toBlackList) {
        BiomeMultiPropertyList biomes = getCompleteBiomes();
        if (biomes == BiomeMultiPropertyList.EMPTY_LIST) {
            biomes = new BiomeMultiPropertyList();
            this.applyProperty(BiomeMultiProperty.getInstance(), biomes);
        }
        biomes.add(biome, toBlackList);
        return this;
    }

    public BiomeMultiPropertyList getCompleteBiomes() {
        return this.recipePropertyStorage == null
                ? BiomeMultiPropertyList.EMPTY_LIST
                : this.recipePropertyStorage.getRecipePropertyValue(BiomeMultiProperty.getInstance(), BiomeMultiPropertyList.EMPTY_LIST);
    }

    public ArrayList<String> getBiomes() {
        return this.recipePropertyStorage == null
                ? EMPTY_LIST
                : this.recipePropertyStorage.getRecipePropertyValue(BiomeMultiProperty.getInstance(), BiomeMultiPropertyList.EMPTY_LIST).whiteListBiomes;
    }

    public ArrayList<String> getBlockedBiomes() {
        return this.recipePropertyStorage == null
                ? EMPTY_LIST
                : this.recipePropertyStorage.getRecipePropertyValue(BiomeMultiProperty.getInstance(), BiomeMultiPropertyList.EMPTY_LIST).blackListBiomes;
    }
}
