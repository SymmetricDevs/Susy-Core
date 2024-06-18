package supersymmetry.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.recipeproperties.PrimitiveProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.ValidationResult;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.SusyLog;
import supersymmetry.api.recipes.properties.EvaporationEnergyProperty;
import supersymmetry.api.util.SuSyUtility;

public class EvaporationPoolRecipeBuilder  extends RecipeBuilder<EvaporationPoolRecipeBuilder> {
    int eutStorage = -1; //according to mtbo what is done with eut will change at some point, so I am just grabbing it when the method is called instead of trusting its later availability

    public EvaporationPoolRecipeBuilder() {

    }

    public EvaporationPoolRecipeBuilder(EvaporationPoolRecipeBuilder other) {
        super(other);
    }

    @Override
    public EvaporationPoolRecipeBuilder copy() {
        return new EvaporationPoolRecipeBuilder(this);
    }

    public EvaporationPoolRecipeBuilder Jt(int Jt) {
        if(Jt <= 0) {
            SusyLog.logger.error("Evaporation Pool required energy cannot be less then or equal to one."
                    , new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }

        eutStorage = Jt;

        this.applyProperty(EvaporationEnergyProperty.getInstance(), Jt);
        return this;
    }

    @Override
    public boolean applyProperty(@NotNull String key, Object value) {
        if (key.equals(EvaporationEnergyProperty.KEY)) {
            this.Jt((int) value);
            return true;
        }

        return super.applyProperty(key, value);
    }

    //store provided EUt for later calculations for the sake of supporting old recipes
    @Override
    public EvaporationPoolRecipeBuilder EUt(int EUt) {
        eutStorage = EUt;
        return super.EUt(EUt);
    }

    @Override
    public ValidationResult<Recipe> build() {
        if (this.recipePropertyStorage == null || !this.recipePropertyStorage.hasRecipeProperty(EvaporationEnergyProperty.getInstance())) {
            if (eutStorage <= 0) {
                this.Jt(40800 * 55 * getFluidInputs().get(0).getAmount() / (getDuration() == 0 ? 200 : getDuration())); //use latent heat of vaporization for water w/ 55mol/L in case of recipes with no energy specified
            } else {
                //calculate joules needed per tick from EUt -> J/t and use eutStorage as variable, as it will no longer be needed
                this.Jt(eutStorage * SuSyUtility.JOULES_PER_EU);
            }
        }

        this.EUt(-1);
        this.applyProperty(PrimitiveProperty.getInstance(), true);
        return super.build();
    }
}
