package supersymmetry.common.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager.BiomeEntry;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import supersymmetry.api.SusyLog;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;
import supersymmetry.common.world.sky.SkyRenderData;

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

        // Create sky renderer for the Moon
        SuSySkyRenderer moonSky = new SuSySkyRenderer();

        // Configure the Sun (follows celestial sphere - day/night cycle)
        SkyRenderData sun = new SkyRenderData.Builder(
                new ResourceLocation("susy", "textures/environment/sun.png"),
                30.0F)  // size
                        .positionType(SkyRenderData.PositionType.CELESTIAL_SPHERE)
                        .useLinearFiltering(false)
                        .build();

        // Configure Earth (stationary at zenith with phases)
        SkyRenderData earth = new SkyRenderData.Builder(
                new ResourceLocation("susy", "textures/environment/earth_phases.png"),
                40.0F)  // size - larger since it's closer
                        .positionType(SkyRenderData.PositionType.ZENITH)
                        .rotationX(90.0F)  // Point to zenith
                        .phases(4, 2, 29.53F)  // 8 phases, 1 row, 29.53 day cycle (synodic month)
                        .useLinearFiltering(false)
                        .brightness(0.8F)  // Slightly dimmer than sun
                        .mirrorTexture(true)  // flip the texture horizontally? for some reason it is flipped in game oh
                                              // well
                        .build();

        // Set the celestial objects
        moonSky.setCelestialObjects(sun, earth);

        new Planet(0, 800, "Moon").setBiomeList(
                new BiomeEntry(SuSyBiomes.LUNAR_HIGHLANDS, 100),
                new BiomeEntry(SuSyBiomes.LUNAR_MARIA, 100))
                .setStone(SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.SMOOTH)
                        .getState(SusyStoneVariantBlock.StoneType.ANORTHOSITE))
                .setSuSySkyRenderer(moonSky)
                .setGravity(0.166f)
                .setBiomeSize(7)
                .setDayLength(29.53f)
                .load();
    }
}
