package supersymmetry.api.recipes.properties;

import gregtech.api.recipes.recipeproperties.RecipeProperty;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BiomeProperty extends RecipeProperty<BiomeProperty.BiomePropertyList> {

    public static final String KEY = "biome";

    private static BiomeProperty INSTANCE;

    private BiomeProperty() {
        super(KEY, BiomePropertyList.class);
    }

    public static BiomeProperty getInstance() {
        if (INSTANCE == null)
            INSTANCE = new BiomeProperty();
        return INSTANCE;
    }

    private static String getBiomesForRecipe(List<Biome> value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.size(); i++) {
            builder.append(value.get(i).biomeName);
            if (i != value.size() - 1)
                builder.append(", ");
        }
        String str = builder.toString();

        if (str.length() >= 26) {
            str = str.substring(0, 23) + "..";
        }
        return str;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        BiomePropertyList list = castValue(value);

        if (list.whiteListBiomes.size() > 0)
            minecraft.fontRenderer.drawString(I18n.format("susy.recipe.biomes",
                    getBiomesForRecipe(castValue(value).whiteListBiomes)), x, y, color);
        if (list.blackListBiomes.size() > 0)
            minecraft.fontRenderer.drawString(I18n.format("susy.recipe.biomes_blocked",
                    getBiomesForRecipe(castValue(value).blackListBiomes)), x, y, color);
    }

    public static class BiomePropertyList {

        public static BiomePropertyList EMPTY_LIST = new BiomePropertyList();

        public final List<Biome> whiteListBiomes = new ObjectArrayList<>();
        public final List<Biome> blackListBiomes = new ObjectArrayList<>();

        public void add(Biome biome, boolean toBlacklist) {
            if (toBlacklist) {
                blackListBiomes.add(biome);
                whiteListBiomes.remove(biome);
            } else {
                whiteListBiomes.add(biome);
                blackListBiomes.remove(biome);
            }
        }

        public void merge(@NotNull BiomeProperty.BiomePropertyList list) {
            this.whiteListBiomes.addAll(list.whiteListBiomes);
            this.blackListBiomes.addAll(list.blackListBiomes);
        }

        public boolean checkBiome(Biome biome) {
            boolean valid = true;
            if (this.blackListBiomes.size() > 0) valid = !this.blackListBiomes.contains(biome);
            if (this.whiteListBiomes.size() > 0) valid = this.whiteListBiomes.contains(biome);
            return valid;
        }
    }
}
