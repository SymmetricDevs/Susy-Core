package supersymmetry.api.recipes.properties;

import gregtech.api.GTValues;
import gregtech.api.recipes.recipeproperties.RecipeProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.catalysts.CatalystInfo;

public class CatalystProperty extends RecipeProperty<CatalystPropertyValue> {

    public static final String KEY = "catalyst";

    private static CatalystProperty INSTANCE;

    private CatalystProperty() {
        super(KEY, CatalystPropertyValue.class);
    }

    public static CatalystProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CatalystProperty();
        }
        return INSTANCE;
    }

    @Override
    public void drawInfo(@NotNull Minecraft minecraft, int x, int y, int color, Object value) {
        CatalystPropertyValue propertyValue = castValue(value);
        int tier = propertyValue.getTier();
        String localisedCatalystGroupName = I18n.format("gregtech.catalyst_group." + propertyValue.getCatalystGroup().getName() + ".name");
        if(tier == CatalystInfo.NO_TIER) {
            minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.catalyst", localisedCatalystGroupName, ""), x, y, color);
        } else {
            minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.catalyst", GTValues.VN[tier], localisedCatalystGroupName), x, y, color);
        }
    }
}
