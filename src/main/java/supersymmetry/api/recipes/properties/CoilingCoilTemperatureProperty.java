package supersymmetry.api.recipes.properties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import org.jetbrains.annotations.NotNull;

import gregtech.api.recipes.recipeproperties.RecipeProperty;

public class CoilingCoilTemperatureProperty extends RecipeProperty<Integer> {

    public static final String KEY = "cooling_temperature";

    private static CoilingCoilTemperatureProperty INSTANCE;

    private CoilingCoilTemperatureProperty() {
        super(KEY, Integer.class);
    }

    public static CoilingCoilTemperatureProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CoilingCoilTemperatureProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(@NotNull Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("susy.recipe.temperature", castValue(value)), x, y, color);
    }
}
