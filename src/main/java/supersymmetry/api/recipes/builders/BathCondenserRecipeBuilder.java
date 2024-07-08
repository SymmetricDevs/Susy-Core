package supersymmetry.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.recipeproperties.PrimitiveProperty;
import gregtech.api.util.ValidationResult;
import supersymmetry.api.recipes.properties.CryogenicEnvironmentProperty;

public class BathCondenserRecipeBuilder extends CryogenicRecipeBuilder<BathCondenserRecipeBuilder> {

    public BathCondenserRecipeBuilder() {
        super();
    }

    public BathCondenserRecipeBuilder(BathCondenserRecipeBuilder recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public BathCondenserRecipeBuilder copy() {
        return new BathCondenserRecipeBuilder(this);
    }

    @Override
    public ValidationResult<Recipe> build() {
        // see PrimitiveRecipeBuilder#build
        this.EUt(1); // secretly force to 1 to allow recipe matching to work properly

        applyProperty(PrimitiveProperty.getInstance(), true);

        // always require cryogenic environment
        if (!recipePropertyStorage.hasRecipeProperty(CryogenicEnvironmentProperty.getInstance())) {
            applyProperty(CryogenicEnvironmentProperty.getInstance(), true);
        }
        return super.build();
    }
}
