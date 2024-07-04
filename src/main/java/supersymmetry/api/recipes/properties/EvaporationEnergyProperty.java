package supersymmetry.api.recipes.properties;

import gregtech.api.recipes.recipeproperties.RecipeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class EvaporationEnergyProperty extends RecipeProperty<Integer> {
    public static final String KEY = "evaporation_energy";

    private static EvaporationEnergyProperty INSTANCE;

    private EvaporationEnergyProperty() {
        super(KEY, Integer.class);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.evaporation",
                castValue(value)), x, y, color);
    }

    public static EvaporationEnergyProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EvaporationEnergyProperty();
        }
        return INSTANCE;
    }

}
