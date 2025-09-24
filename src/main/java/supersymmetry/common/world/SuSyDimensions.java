package supersymmetry.common.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager.BiomeEntry;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import supersymmetry.api.SusyLog;
import supersymmetry.client.renderer.sky.SkyRendererMoon;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;

public class SuSyDimensions {

    public static DimensionType planetType;
    public static DimensionType spaceType;

    public static List<Biome> BIOMES = new ArrayList<>();
    public static Map<Integer, Planet> PLANETS = new Int2ObjectArrayMap<>();

    public static void init() {
        // Registers dimension type. Uses a negative ID so that fire blocks have less logic.
        int id = -2;

        for (DimensionType type : DimensionType.values()) {
            if (type.getId() < id) {
                id = type.getId();
            }
        }
        id--;

        SusyLog.logger.info("Registering planet dimension type at id " + id);
        planetType = DimensionType.register("susy_planet", "_susy", id, WorldProviderPlanet.class, false);

        // Actually registers dimension layout.

        new Planet(0, 800, "Moon").setBiomeList(
                new BiomeEntry(SuSyBiomes.LUNAR_HIGHLANDS, 80),
                new BiomeEntry(SuSyBiomes.LUNAR_MARIA, 20))
                .setStone(SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.SMOOTH)
                        .getState(SusyStoneVariantBlock.StoneType.ANORTHOSITE))
                .setSkyRenderer(new SkyRendererMoon())
                .setGravity(0.166f).setDragMultiplier(1f).setBiomeSize(7).load();

        new Planet(0, 700, "Water Test World").setBiomeList(
                new BiomeEntry(Biomes.OCEAN, 1)).load();
    }
}
