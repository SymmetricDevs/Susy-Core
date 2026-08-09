package supersymmetry.api.recipes.properties;

import gregtech.api.recipes.recipeproperties.RecipeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class SolarFurnaceMinPowerProperty extends RecipeProperty<Integer> {

    public static final String KEY = "solar_furnace_min_power";

    private static SolarFurnaceMinPowerProperty INSTANCE;

    private SolarFurnaceMinPowerProperty() {
        super(KEY, Integer.class);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("susy.recipe.solar_furnace_min_power", castValue(value)), x, y,
                color);
    }

    public static SolarFurnaceMinPowerProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SolarFurnaceMinPowerProperty();
        }
        return INSTANCE;
    }
}
