package supersymmetry.api.recipes.builders;

import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.builder.ToStringBuilder;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import supersymmetry.api.recipes.properties.DimensionProperty;

public class DimensionRecipeBuilder extends RecipeBuilder<DimensionRecipeBuilder> {

    private int minimumDuration = 0;

    public DimensionRecipeBuilder() {}

    public DimensionRecipeBuilder(Recipe recipe, RecipeMap<DimensionRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public DimensionRecipeBuilder(RecipeBuilder<DimensionRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public DimensionRecipeBuilder copy() {
        return new DimensionRecipeBuilder(this);
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

    public DimensionRecipeBuilder dimension(int dimensionID) {
        IntList dimensionIDs = getDimensionIDs();
        if (dimensionIDs == IntLists.EMPTY_LIST) {
            dimensionIDs = new IntArrayList();
            this.applyProperty(DimensionProperty.getInstance(), dimensionIDs);
        }
        dimensionIDs.add(dimensionID);
        return this;
    }

    @Override
    public DimensionRecipeBuilder duration(int duration) {
        return super.duration(Math.max(duration, this.minimumDuration));
    }

    public DimensionRecipeBuilder minimumDuration(int minimumDuration) {
        this.minimumDuration = minimumDuration;
        return this;
    }

    public IntList getDimensionIDs() {
        return this.recipePropertyStorage == null ? IntLists.EMPTY_LIST :
                this.recipePropertyStorage.getRecipePropertyValue(DimensionProperty.getInstance(),
                        IntLists.EMPTY_LIST);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(DimensionProperty.getInstance().getKey(), getDimensionIDs().toString())
                .toString();
    }
}
