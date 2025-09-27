package supersymmetry.api.recipes.properties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import gregtech.api.recipes.recipeproperties.RecipeProperty;

public class EvaporationEnergyProperty extends RecipeProperty<Integer> {

    public static final String KEY = "evaporation_energy";

    private static EvaporationEnergyProperty INSTANCE;

    private EvaporationEnergyProperty() {
        super(KEY, Integer.class);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("susy.recipe.evaporation",
                castValue(value)), x, y, color);
    }

    public static EvaporationEnergyProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EvaporationEnergyProperty();
        }
        return INSTANCE;
    }
}
