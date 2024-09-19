package supersymmetry.api.recipes.properties;

import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.worldgen.config.WorldGenRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BiomeMultiProperty extends RecipeProperty<BiomeMultiPropertyList> {
    public static final String KEY = "biome";

    private static BiomeMultiProperty INSTANCE;
    private BiomeMultiProperty() {
        super(KEY, BiomeMultiPropertyList.class);
    }

    public static BiomeMultiProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BiomeMultiProperty();
        }
        return INSTANCE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        BiomeMultiPropertyList list = castValue(value);

        if (!list.whiteListBiomes.isEmpty())
            minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.biomes",
                    getBiomesForRecipe(castValue(value).whiteListBiomes)), x, y, color);
        if (!list.blackListBiomes.isEmpty())
            minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.biomes_blocked",
                    getBiomesForRecipe(castValue(value).blackListBiomes)), x, y, color);
    }

    private static String getBiomesForRecipe(ArrayList<String> biomeNames) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < biomeNames.size(); i++) {
            builder.append(biomeNames.get(i));
            if (i != biomeNames.size() - 1)
                builder.append(", ");
        }
        String str = builder.toString();

        if (str.length() >= 13) {
            str = str.substring(0, 10) + "..";
        }
        return str;
    }

}
