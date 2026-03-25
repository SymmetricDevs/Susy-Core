package supersymmetry.api.recipes.properties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import gregtech.api.recipes.recipeproperties.RecipeProperty;

public class MixerSettlerCellsProperty extends RecipeProperty<Integer> {

    public static final String KEY = "mixer_settler_cells";

    private static MixerSettlerCellsProperty INSTANCE;

    private MixerSettlerCellsProperty() {
        super(KEY, Integer.class);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("susy.recipe.mixer_settler_cells",
                castValue(value)), x, y, color);
    }

    public static MixerSettlerCellsProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MixerSettlerCellsProperty();
        }
        return INSTANCE;
    }
}
