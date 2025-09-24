package supersymmetry.api.recipes.properties;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import gregtech.api.gui.resources.TextureArea;
import gregtech.api.recipes.recipeproperties.RecipeProperty;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BiomeProperty extends RecipeProperty<BiomeProperty.BiomePropertyList> {

    public static final String KEY = "biome";
    private static final Position POSITION = new Position(80, 45);
    private static final Size SIZE = new Size(16, 16);
    private static final TextureArea ICON = TextureArea.fullImage("textures/gui/widget/information.png");

    private static BiomeProperty INSTANCE;

    private BiomeProperty() {
        super(KEY, BiomePropertyList.class);
    }

    public static BiomeProperty getInstance() {
        if (INSTANCE == null)
            INSTANCE = new BiomeProperty();
        return INSTANCE;
    }

    private static String getBiomesForRecipe(BiomePropertyList biomePropertyList, boolean limited) {
        boolean isWhiteList = biomePropertyList.whiteListBiomes.size() > 0;
        List<Biome> list = isWhiteList ? biomePropertyList.whiteListBiomes : biomePropertyList.blackListBiomes;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append(list.get(i).biomeName);
            if (i != list.size() - 1)
                builder.append(", ");
        }
        String str = I18n.format(isWhiteList ? "susy.recipe.biomes" : "susy.recipe.biomes_blocked", builder.toString());

        if (limited && str.length() >= 35) {
            str = str.substring(0, 32) + "..";
        }

        return str;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getTooltipStrings(List<String> tooltip, int mouseX, int mouseY, Object value) {
        super.getTooltipStrings(tooltip, mouseX, mouseY, value);

        BiomePropertyList list = castValue(value);

        if (mouseX < POSITION.getX() || mouseX > POSITION.getX() + SIZE.getWidth() ||
                mouseY < POSITION.getY() || mouseY > POSITION.getY() + SIZE.getHeight())
            return;

        tooltip.add(getBiomesForRecipe(list, false));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        BiomePropertyList list = castValue(value);
        minecraft.fontRenderer.drawString(getBiomesForRecipe(list, true), x, y, color);

        GlStateManager.enableLighting();
        GlStateManager.enableLight(1);
        ICON.draw(POSITION.getX(), POSITION.getY(), SIZE.getWidth(), SIZE.getHeight());
        GlStateManager.disableLight(1);
        GlStateManager.disableLighting();
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
