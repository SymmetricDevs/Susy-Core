package supersymmetry.api.recipes.properties;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import org.jetbrains.annotations.NotNull;

import gregtech.api.recipes.recipeproperties.RecipeProperty;

public class InductionCrucibleMaterialProperty extends RecipeProperty<String> {

    public static final String KEY = "induction_crucible_material";

    private static InductionCrucibleMaterialProperty INSTANCE;

    private InductionCrucibleMaterialProperty() {
        super(KEY, String.class);
    }

    public static InductionCrucibleMaterialProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InductionCrucibleMaterialProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(@NotNull Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("susy.recipe.induction_crucible_material", castValue(value)), x, y, color);
    }
}
