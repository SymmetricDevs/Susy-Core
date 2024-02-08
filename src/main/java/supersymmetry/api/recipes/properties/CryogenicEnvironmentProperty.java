package supersymmetry.api.recipes.properties;

import gregtech.api.recipes.recipeproperties.RecipeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class CryogenicEnvironmentProperty extends RecipeProperty<Boolean> {

    public static final String KEY = "cryogenic_environment";

    private static CryogenicEnvironmentProperty INSTANCE;

    public static CryogenicEnvironmentProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CryogenicEnvironmentProperty();
        }
        return INSTANCE;
    }

    public CryogenicEnvironmentProperty() {
        super(KEY, Boolean.class);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        Boolean casted = castValue(value);
        if (casted != null && casted) {
            minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.cryogenic_environment"), x, y, color);
        }
    }

    @Override
    public int getInfoHeight(Object value) {
        Boolean casted = castValue(value);
        if (casted != null && casted) {
            return super.getInfoHeight(value);
        }
        return 0;
    }
}
