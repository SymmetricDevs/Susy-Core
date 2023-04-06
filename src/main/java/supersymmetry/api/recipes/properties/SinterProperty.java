package supersymmetry.api.recipes.properties;

import gregtech.api.recipes.recipeproperties.RecipeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.NotNull;

public class SinterProperty extends RecipeProperty<Boolean> {

    public static final String KEY = "plasma_enabled";
    private static SinterProperty INSTANCE;

    private SinterProperty() {
        super(KEY, Boolean.class);
    }

    @Override
    public void drawInfo(@NotNull Minecraft minecraft, int x, int y, int color, Object value) {
        if (castValue(value)) {
            minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.plasma_requirement", castValue(value)), x, y, color);
        }
    }

    public static SinterProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SinterProperty();
        }
        return INSTANCE;
    }
}
