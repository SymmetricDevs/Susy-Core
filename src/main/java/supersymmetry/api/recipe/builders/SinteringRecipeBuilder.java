package supersymmetry.api.recipe.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipe.properties.CoilingCoilTemperatureProperty;
import supersymmetry.api.recipe.properties.SinterProperty;

public class SinteringRecipeBuilder extends RecipeBuilder<SinteringRecipeBuilder> {

    public SinteringRecipeBuilder() {

    }

    public SinteringRecipeBuilder(SinteringRecipeBuilder builder) {
        super(builder);
    }

    @SuppressWarnings("unused")
    public SinteringRecipeBuilder(Recipe recipe, RecipeMap<SinteringRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    @Override
    public SinteringRecipeBuilder copy() {
        return new SinteringRecipeBuilder(this);
    }

    public SinteringRecipeBuilder usePlasma(boolean usePlasma) {
        this.applyProperty(SinterProperty.getInstance(), usePlasma);
        return this;
    }

    @Override
    public boolean applyProperty(@NotNull String key, Object value) {
        if (key.equals(CoilingCoilTemperatureProperty.KEY)) {
            this.usePlasma((Boolean) value);
            return true;
        }
        return super.applyProperty(key, value);
    }

}
