package supersymmetry.api.recipes.builders;

import java.util.List;

import javax.annotation.Nonnull;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.PrimitiveProperty;
import gregtech.api.util.ValidationResult;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import supersymmetry.api.recipes.properties.DimensionProperty;

public class NoEnergyRecipeBuilder extends RecipeBuilder<NoEnergyRecipeBuilder> {

    public NoEnergyRecipeBuilder() {}

    @SuppressWarnings("unused")
    public NoEnergyRecipeBuilder(Recipe recipe, RecipeMap<NoEnergyRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public NoEnergyRecipeBuilder(RecipeBuilder<NoEnergyRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    public NoEnergyRecipeBuilder copy() {
        return new NoEnergyRecipeBuilder(this);
    }

    public ValidationResult<Recipe> build() {
        this.EUt(1);
        this.applyProperty(PrimitiveProperty.getInstance(), true);
        return super.build();
    }

    @Override
    public boolean applyProperty(@Nonnull String key, Object value) {
        if (key.equals(DimensionProperty.KEY)) {
            if (value instanceof Integer) {
                this.dimension((Integer) value);
            } else if (value instanceof List && !((List<?>) value).isEmpty() &&
                    ((List<?>) value).get(0) instanceof Integer) {
                        IntList dimensionIDs = getDimensionIDs();
                        if (dimensionIDs == IntLists.EMPTY_LIST) {
                            dimensionIDs = new IntArrayList();
                            this.applyProperty(DimensionProperty.getInstance(), dimensionIDs);
                        }
                        dimensionIDs.addAll((List<Integer>) value);
                    } else {
                        throw new IllegalArgumentException("Invalid Dimension Property Type!");
                    }
            return true;
        }
        return super.applyProperty(key, value);
    }

    public NoEnergyRecipeBuilder dimension(int dimensionID) {
        IntList dimensionIDs = getDimensionIDs();
        if (dimensionIDs == IntLists.EMPTY_LIST) {
            dimensionIDs = new IntArrayList();
            this.applyProperty(DimensionProperty.getInstance(), dimensionIDs);
        }
        dimensionIDs.add(dimensionID);
        return this;
    }

    public IntList getDimensionIDs() {
        return this.recipePropertyStorage == null ? IntLists.EMPTY_LIST :
                this.recipePropertyStorage.getRecipePropertyValue(DimensionProperty.getInstance(),
                        IntLists.EMPTY_LIST);
    }
}
