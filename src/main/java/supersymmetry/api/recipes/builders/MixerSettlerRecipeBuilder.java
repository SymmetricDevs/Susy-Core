package supersymmetry.api.recipes.builders;

import org.jetbrains.annotations.NotNull;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.ValidationResult;
import supersymmetry.api.SusyLog;
import supersymmetry.api.recipes.properties.EvaporationEnergyProperty;
import supersymmetry.api.recipes.properties.MixerSettlerCellsProperty;

public class MixerSettlerRecipeBuilder extends RecipeBuilder<MixerSettlerRecipeBuilder> {

    public MixerSettlerRecipeBuilder() {}

    public MixerSettlerRecipeBuilder(MixerSettlerRecipeBuilder other) {
        super(other);
    }

    @Override
    public MixerSettlerRecipeBuilder copy() {
        return new MixerSettlerRecipeBuilder(this);
    }

    @SuppressWarnings("UnusedReturnValue")
    public MixerSettlerRecipeBuilder requiredCells(int cells) {
        if (cells <= 0) {
            SusyLog.logger.error("Required mixer settler cell count cannot be less than or equal to one.",
                    new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }

        if (cells % 2 != 0) {
            SusyLog.logger.error("Required mixer settler cell count must be even.", new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.applyProperty(MixerSettlerCellsProperty.getInstance(), cells);
        return this;
    }

    @Override
    public boolean applyProperty(@NotNull String key, Object value) {
        if (key.equals(EvaporationEnergyProperty.KEY)) {
            this.requiredCells((int) value);
            return true;
        }

        return super.applyProperty(key, value);
    }

    @Override
    public ValidationResult<Recipe> build() {
        if (this.recipePropertyStorage == null ||
                !this.recipePropertyStorage.hasRecipeProperty(MixerSettlerCellsProperty.getInstance())) {
            this.requiredCells(2);
        }
        return super.build();
    }
}
