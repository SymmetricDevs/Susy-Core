package supersymmetry.api.recipes.builders;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.EnumValidationResult;
import supersymmetry.api.SusyLog;
import supersymmetry.api.recipes.properties.CoilingCoilTemperatureProperty;

public class CoilingCoilRecipeBuilder extends RecipeBuilder<CoilingCoilRecipeBuilder> {

    public CoilingCoilRecipeBuilder() {}

    @SuppressWarnings("unused")
    public CoilingCoilRecipeBuilder(Recipe recipe, RecipeMap<CoilingCoilRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public CoilingCoilRecipeBuilder(CoilingCoilRecipeBuilder builder) {
        super(builder);
    }

    @Override
    public CoilingCoilRecipeBuilder copy() {
        return new CoilingCoilRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(@NotNull String key, Object value) {
        if (key.equals(CoilingCoilTemperatureProperty.KEY)) {
            this.temperature(((Number) value).intValue());
            return true;
        }
        return super.applyProperty(key, value);
    }

    public CoilingCoilRecipeBuilder temperature(int temperature) {
        if (temperature <= 0) {
            SusyLog.logger.error("Temperature cannot be less than or equal to 0", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.applyProperty(CoilingCoilTemperatureProperty.getInstance(), temperature);
        return this;
    }

    public int getTemperature() {
        return this.recipePropertyStorage == null ? 0 :
                this.recipePropertyStorage.getRecipePropertyValue(CoilingCoilTemperatureProperty.getInstance(), 0);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(CoilingCoilTemperatureProperty.getInstance().getKey(), getTemperature())
                .toString();
    }
}
