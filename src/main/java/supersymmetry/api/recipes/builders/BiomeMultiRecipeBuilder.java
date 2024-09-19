package supersymmetry.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.world.biome.Biome;
import supersymmetry.api.recipes.properties.BiomeMultiProperty;
import supersymmetry.api.recipes.properties.BiomeMultiPropertyList;

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
        return (BiomeMultiRecipeBuilder) this;
    }

    public BiomeMultiPropertyList getCompleteBiomes() {
        return this.recipePropertyStorage == null ? BiomeMultiPropertyList.EMPTY_LIST :
                this.recipePropertyStorage.getRecipePropertyValue(BiomeMultiProperty.getInstance(),
                        BiomeMultiPropertyList.EMPTY_LIST);
    }

    public IntList getBiomes() {
        return this.recipePropertyStorage == null ? IntLists.EMPTY_LIST :
                this.recipePropertyStorage.getRecipePropertyValue(BiomeMultiProperty.getInstance(),
                        BiomeMultiPropertyList.EMPTY_LIST).whiteListBiomes;
    }

    public IntList getBlockedBiomes() {
        return this.recipePropertyStorage == null ? IntLists.EMPTY_LIST :
                this.recipePropertyStorage.getRecipePropertyValue(BiomeMultiProperty.getInstance(),
                        BiomeMultiPropertyList.EMPTY_LIST).blackListBiomes;
    }



}
