package supersymmetry.api.recipes.builders;

import java.util.Map;

import net.minecraft.item.ItemStack;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import supersymmetry.api.recipes.catalysts.CatalystGroup;
import supersymmetry.api.recipes.catalysts.CatalystInfo;
import supersymmetry.api.recipes.properties.CatalystProperty;
import supersymmetry.api.recipes.properties.CatalystPropertyValue;

public class CatalystRecipeBuilder extends RecipeBuilder<CatalystRecipeBuilder> {

    public CatalystRecipeBuilder() {}

    @SuppressWarnings("unused")
    public CatalystRecipeBuilder(Recipe recipe, RecipeMap<CatalystRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public CatalystRecipeBuilder(RecipeBuilder<CatalystRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    public CatalystRecipeBuilder copy() {
        return new CatalystRecipeBuilder(this);
    }

    public CatalystRecipeBuilder catalyst(CatalystGroup catalystGroup) {
        return catalyst(catalystGroup, CatalystInfo.NO_TIER, 1);
    }

    public CatalystRecipeBuilder catalyst(CatalystGroup catalystGroup, int tier) {
        return catalyst(catalystGroup, tier, 1);
    }

    public CatalystRecipeBuilder catalyst(CatalystGroup catalystGroup, int tier, int amount) {
        applyProperty(CatalystProperty.getInstance(), new CatalystPropertyValue(tier, catalystGroup));

        ItemStack[] inputStacks = catalystGroup.getCatalystInfos().streamEntries()
                .filter(entry -> entry.getValue().getTier() >= tier)
                .map(Map.Entry::getKey)
                .map(is -> {
                    is = is.copy();
                    is.setCount(amount);
                    return is;
                }).toArray(ItemStack[]::new);

        return this.notConsumable(GTRecipeItemInput.getOrCreate(inputStacks));
    }
}
