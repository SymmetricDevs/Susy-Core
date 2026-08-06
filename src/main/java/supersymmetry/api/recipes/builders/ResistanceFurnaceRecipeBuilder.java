package supersymmetry.api.recipes.builders;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.EnumValidationResult;
import supersymmetry.api.SusyLog;
import supersymmetry.api.recipes.properties.ResistanceFurnaceTemperatureProperty;

public class ResistanceFurnaceRecipeBuilder extends RecipeBuilder<ResistanceFurnaceRecipeBuilder> {

    public ResistanceFurnaceRecipeBuilder() {}

    public ResistanceFurnaceRecipeBuilder(Recipe recipe, RecipeMap<ResistanceFurnaceRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public ResistanceFurnaceRecipeBuilder(ResistanceFurnaceRecipeBuilder recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public ResistanceFurnaceRecipeBuilder copy() {
        return new ResistanceFurnaceRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(@NotNull String key, Object value) {
        if (key.equals(ResistanceFurnaceTemperatureProperty.KEY)) {
            this.temperature(((Number) value).intValue());
            return true;
        }
        return super.applyProperty(key, value);
    }

    public ResistanceFurnaceRecipeBuilder temperature(int temperature) {
        if (temperature <= 0) {
            SusyLog.logger.error("Temperature cannot be less than or equal to 0", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        } else if (temperature > 1673) {
            SusyLog.logger.error("Temperature cannot be greater than 1673", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.applyProperty(ResistanceFurnaceTemperatureProperty.getInstance(), temperature);
        return this;
    }

    public int getTemperature() {
        return this.recipePropertyStorage == null ? 0 :
                this.recipePropertyStorage.getRecipePropertyValue(ResistanceFurnaceTemperatureProperty.getInstance(),
                        0);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(ResistanceFurnaceTemperatureProperty.getInstance().getKey(), getTemperature())
                .toString();
    }

    public void invalidateBuildAction() {
        this.invalidateOnBuildAction();
    }
}
