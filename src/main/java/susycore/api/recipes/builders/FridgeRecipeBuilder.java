package susycore.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.recipes.recipeproperties.TemperatureProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Nonnull;

public class FridgeRecipeBuilder extends RecipeBuilder<FridgeRecipeBuilder> {

    public FridgeRecipeBuilder() {
    }

    public FridgeRecipeBuilder(Recipe recipe, RecipeMap<FridgeRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public FridgeRecipeBuilder(FridgeRecipeBuilder recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public FridgeRecipeBuilder copy() {
        return new FridgeRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(@Nonnull String key, Object value) {
        if (key.equals(TemperatureProperty.KEY)) {
            this.blastFurnaceTemp(((Number) value).intValue());
            return true;
        }
        return super.applyProperty(key, value);
    }

    public FridgeRecipeBuilder blastFurnaceTemp(int blastFurnaceTemp) {
        if (blastFurnaceTemp <= 0) {
            GTLog.logger.error("Blast Furnace Temperature cannot be less than or equal to 0", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.applyProperty(TemperatureProperty.getInstance(), blastFurnaceTemp);
        return this;
    }

    public int getBlastFurnaceTemp() {
        return this.recipePropertyStorage == null ? 0 :
                this.recipePropertyStorage.getRecipePropertyValue(TemperatureProperty.getInstance(), 0);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(TemperatureProperty.getInstance().getKey(), getBlastFurnaceTemp())
                .toString();
    }

}
