package supersymmetry.api.recipes.properties;

import gregtech.api.worldgen.config.WorldGenRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;

import gregtech.api.recipes.recipeproperties.RecipeProperty;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.resources.I18n;

import java.util.Map;

public class DroneDimensionProperty extends RecipeProperty<IntList> {

    public static final String KEY = "dimension";

    private static DroneDimensionProperty INSTANCE;

    private DroneDimensionProperty() {
        super(KEY, IntList.class);
    }

    public static DroneDimensionProperty getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DroneDimensionProperty();
        return INSTANCE;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("susy.recipe.dimensions",
                getDimensionsForRecipe(castValue(value))), x, y, color);
    }

    private static String getDimensionsForRecipe(IntList value) {
        Map<Integer, String> dimNames = WorldGenRegistry.getNamedDimensions();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.size(); i++) {
            builder.append(dimNames.getOrDefault(value.getInt(i), String.valueOf(value.getInt(i))));
            if (i != value.size() - 1)
                builder.append(", ");
        }
        String str = builder.toString();

        if (str.length() >= 13) {
            str = str.substring(0, 10) + "..";
        }
        return str;
    }

}
