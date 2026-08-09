package supersymmetry.api.recipes.builders;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.recipeproperties.PrimitiveProperty;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.ValidationResult;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.SusyLog;
import supersymmetry.api.recipes.properties.DimensionProperty;
import supersymmetry.api.recipes.properties.EvaporationEnergyProperty;
import supersymmetry.api.recipes.properties.SolarFurnaceMinPowerProperty;
import supersymmetry.api.util.SuSyUtility;

import java.util.List;

public class SolarFurnaceRecipeBuilder extends RecipeBuilder<SolarFurnaceRecipeBuilder> {

    int eutStorage = -1;

    public SolarFurnaceRecipeBuilder() {}

    public SolarFurnaceRecipeBuilder(SolarFurnaceRecipeBuilder other) {
        super(other);
        this.eutStorage = other.eutStorage;
    }

    @Override
    public SolarFurnaceRecipeBuilder copy() {
        return new SolarFurnaceRecipeBuilder(this);
    }

    @SuppressWarnings("UnusedReturnValue")
    public SolarFurnaceRecipeBuilder power(int power) {
        if (power <= 0) {
            SusyLog.logger.error("Solar Furnace required energy cannot be less than or equal to one.",
                    new IllegalArgumentException());
            recipeStatus = EnumValidationResult.INVALID;
        }

        eutStorage = power;

        this.applyProperty(SolarFurnaceMinPowerProperty.getInstance(), power);
        return this;
    }

    @Override
    public boolean applyProperty(@NotNull String key, Object value) {
        if (key.equals(SolarFurnaceMinPowerProperty.KEY)) {
            this.power((int) value);
            return true;
        }
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

    @Override
    public SolarFurnaceRecipeBuilder EUt(int EUt) {
        eutStorage = EUt;
        return super.EUt(EUt);
    }

    @Override
    public ValidationResult<Recipe> build() {
        if (this.recipePropertyStorage == null ||
                !this.recipePropertyStorage.hasRecipeProperty(SolarFurnaceMinPowerProperty.getInstance())) {
                this.power(eutStorage);
        }

        this.EUt(-1);
        this.applyProperty(PrimitiveProperty.getInstance(), true);
        return super.build();
    }

    public SolarFurnaceRecipeBuilder dimension(int dimensionID) {
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
