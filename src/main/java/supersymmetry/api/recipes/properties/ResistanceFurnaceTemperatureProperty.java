package supersymmetry.api.recipes.properties;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.NotNull;

import gregtech.api.recipes.recipeproperties.RecipeProperty;

public class ResistanceFurnaceTemperatureProperty extends RecipeProperty<Integer> {

    public static final String KEY = "resistance_temperature";

    private static ResistanceFurnaceTemperatureProperty INSTANCE;

    private ResistanceFurnaceTemperatureProperty() {
        super(KEY, Integer.class);
    }

    @Override
    public void drawInfo(@NotNull Minecraft minecraft, int x, int y, int color, Object value) {}

    public static ResistanceFurnaceTemperatureProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ResistanceFurnaceTemperatureProperty();
        }
        return INSTANCE;
    }
}
