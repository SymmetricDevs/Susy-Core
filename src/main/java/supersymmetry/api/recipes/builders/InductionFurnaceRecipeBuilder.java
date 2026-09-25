package supersymmetry.api.recipes.builders;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import supersymmetry.api.recipes.properties.InductionCrucibleMaterialProperty;

public class InductionFurnaceRecipeBuilder extends RecipeBuilder<InductionFurnaceRecipeBuilder> {

    public InductionFurnaceRecipeBuilder() {}

    @SuppressWarnings("unused")
    public InductionFurnaceRecipeBuilder(Recipe recipe, RecipeMap<InductionFurnaceRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public InductionFurnaceRecipeBuilder(InductionFurnaceRecipeBuilder recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public InductionFurnaceRecipeBuilder copy() {
        return new InductionFurnaceRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(@NotNull String key, Object value) {
        if (key.equals(InductionCrucibleMaterialProperty.KEY)) {
            this.material(((String) value));
            return true;
        }
        return super.applyProperty(key, value);
    }

    public InductionFurnaceRecipeBuilder material(String material) {
        this.applyProperty(InductionCrucibleMaterialProperty.getInstance(), material);
        return this;
    }

    public String getMaterial() {
        return this.recipePropertyStorage == null ? "" :
                this.recipePropertyStorage.getRecipePropertyValue(InductionCrucibleMaterialProperty.getInstance(),
                        "");
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(InductionCrucibleMaterialProperty.getInstance().getKey(), getMaterial())
                .toString();
    }
}
