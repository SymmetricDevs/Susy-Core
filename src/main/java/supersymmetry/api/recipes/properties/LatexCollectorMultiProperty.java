package supersymmetry.api.recipes.properties;

import gregtech.api.recipes.recipeproperties.RecipeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.NotNull;

public class LatexCollectorMultiProperty extends RecipeProperty<LatexCollectorMultiPropertyValues> {
    public static final String KEY = "blocks";

    private static LatexCollectorMultiProperty INSTANCE;
    private LatexCollectorMultiProperty() {
        super(KEY, LatexCollectorMultiPropertyValues.class);
    }

    public static LatexCollectorMultiProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LatexCollectorMultiProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(@NotNull Minecraft minecraft, int x, int y, int color, Object value) {
        LatexCollectorMultiPropertyValues propertyValue = castValue(value);
        String localisedBlockGroupMembers = I18n.format("gregtech.block_group_members." + propertyValue.getBlockGroupName() + ".name");
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.blocks", localisedBlockGroupMembers), x, y, color);
    }

}
