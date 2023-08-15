package supersymmetry.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import org.apache.commons.lang3.builder.ToStringBuilder;
import supersymmetry.api.recipes.properties.DroneDimensionProperty;

import javax.annotation.Nonnull;
import java.util.List;

public class DronePadRecipeBuilder extends RecipeBuilder<DronePadRecipeBuilder> {


    public DronePadRecipeBuilder() {
    }

    public DronePadRecipeBuilder(Recipe recipe, RecipeMap<DronePadRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public DronePadRecipeBuilder(RecipeBuilder<DronePadRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public DronePadRecipeBuilder copy() {
        return new DronePadRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(@Nonnull String key, Object value) {
        if (key.equals(DroneDimensionProperty.KEY)) {
            if (value instanceof Integer) {
                this.dimension((Integer) value);
            } else if (value instanceof List && !((List<?>) value).isEmpty() && ((List<?>) value).get(0) instanceof Integer) {
                IntList dimensionIDs = getDimensionIDs();
                if (dimensionIDs == IntLists.EMPTY_LIST) {
                    dimensionIDs = new IntArrayList();
                    this.applyProperty(DroneDimensionProperty.getInstance(), dimensionIDs);
                }
                dimensionIDs.addAll((List<Integer>) value);
            } else {
                throw new IllegalArgumentException("Invalid Dimension Property Type! (Drone)");
            }
            return true;
        }
        return super.applyProperty(key, value);
    }

    public DronePadRecipeBuilder dimension(int dimensionID) {
        IntList dimensionIDs = getDimensionIDs();
        if (dimensionIDs == IntLists.EMPTY_LIST) {
            dimensionIDs = new IntArrayList();
            this.applyProperty(DroneDimensionProperty.getInstance(), dimensionIDs);
        }
        dimensionIDs.add(dimensionID);
        return this;
    }

    @Override
    public DronePadRecipeBuilder duration(int duration) {
        return super.duration(Math.max(duration, 400));
    }

    public IntList getDimensionIDs() {
        return this.recipePropertyStorage == null ? IntLists.EMPTY_LIST :
                this.recipePropertyStorage.getRecipePropertyValue(DroneDimensionProperty.getInstance(),
                        IntLists.EMPTY_LIST);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append(DroneDimensionProperty.getInstance().getKey(), getDimensionIDs().toString())
                .toString();
    }

}
