package supersymmetry.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import supersymmetry.api.recipes.catalysts.CatalystGroup;
import supersymmetry.api.recipes.ingredients.SuSyRecipeCatalystInput;
import supersymmetry.api.recipes.properties.CatalystProperty;

public class CatalystRecipeBuilder extends RecipeBuilder<CatalystRecipeBuilder> {

    public CatalystRecipeBuilder() {

    }

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
        return catalyst(catalystGroup,0, 1);
    }

    public CatalystRecipeBuilder catalyst(CatalystGroup catalystGroup, int tier) {
        return catalyst(catalystGroup, tier, 1);
    }

    public CatalystRecipeBuilder catalyst(CatalystGroup catalystGroup, int tier, int amount) {
        applyProperty(CatalystProperty.getInstance(), tier);
        return this.notConsumable((SuSyRecipeCatalystInput.getOrCreate(catalystGroup, tier, amount)));
    }

}
