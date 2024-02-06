package supersymmetry.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.recipeproperties.PrimitiveProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.ValidationResult;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.SusyLog;
import supersymmetry.api.recipes.properties.EvaporationEnergyProperty;

public class EvaporationPoolRecipeBuilder  extends RecipeBuilder<EvaporationPoolRecipeBuilder> {


    public EvaporationPoolRecipeBuilder() {

    }

    public EvaporationPoolRecipeBuilder(EvaporationPoolRecipeBuilder other) {
        super(other);
    }

    @Override
    public EvaporationPoolRecipeBuilder copy() {
        return new EvaporationPoolRecipeBuilder(this);
    }

    public EvaporationPoolRecipeBuilder evaporationEnergy(int kJ) {
        if(kJ <= 0) {
            SusyLog.logger.error("Evaporation Pool required energy cannot be less then or equal to one!"
                    , new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }
        this.applyProperty(EvaporationEnergyProperty.getInstance(), kJ);
        return this;
    }

    @Override
    public boolean applyProperty(@NotNull String key, Object value) {
        if (key.equals(EvaporationEnergyProperty.KEY)) {
            this.evaporationEnergy((int) value);
            return true;
        }
        return super.applyProperty(key, value);
    }

    @Override
    public ValidationResult<Recipe> build() {
        if (this.recipePropertyStorage == null || !this.recipePropertyStorage.hasRecipeProperty(EvaporationEnergyProperty.getInstance())) {
            this.evaporationEnergy(100);
        }
        this.EUt(-1);
        this.applyProperty(PrimitiveProperty.getInstance(), true);
        return super.build();
    }
}
