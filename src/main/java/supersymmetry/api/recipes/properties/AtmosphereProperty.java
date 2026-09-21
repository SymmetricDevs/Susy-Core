package supersymmetry.api.recipes.properties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import gregtech.api.recipes.recipeproperties.RecipeProperty;

public class AtmosphereProperty extends RecipeProperty<Boolean> {

    public static final String KEY = "atmosphere";

    private static AtmosphereProperty INSTANCE;

    private AtmosphereProperty() {
        super(KEY, Boolean.class);
    }

    public static AtmosphereProperty getInstance() {
        if (INSTANCE == null)
            INSTANCE = new AtmosphereProperty();
        return INSTANCE;
    }

    @Override
    public int getInfoHeight(Object value) {
        Boolean casted = castValue(value);
        if (casted != null) {
            return super.getInfoHeight(value);
        }
        return 0;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        Boolean casted = castValue(value);
        if (casted != null) {
            if (casted) {
                minecraft.fontRenderer.drawString(I18n.format("susy.recipe.atmosphere"), x, y, color);
            } else {
                minecraft.fontRenderer.drawString(I18n.format("susy.recipe.vacuum"), x, y, color);
            }
        }
    }
}
