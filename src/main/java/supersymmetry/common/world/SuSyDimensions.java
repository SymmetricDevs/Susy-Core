package supersymmetry.common.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import supersymmetry.api.SusyLog;
import supersymmetry.common.blocks.BlockRegolith;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;
import supersymmetry.common.world.biome.SuSyBiomeEntry;
import supersymmetry.common.world.sky.SkyColorData;
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

        SuSySkyRenderer moonSky = new SuSySkyRenderer();

        // Lunar eclipse mechanics:
        // - Moon's orbital period: 27.3 days (synodic: 29.5 days)
        // - Eclipse season occurs roughly every 6 months (173 days)
        // - The nodal period (when orbit crosses ecliptic plane) is ~346 days
        // - Eclipses occur 4-7 times per Earth year

        // For Moon perspective: Earth eclipses sun when sun passes behind Earth at zenith
        // This can only happen when the orbital planes align

        long lunarDayTicks = 708734L; // 29.5306 * 24000

        float nodalPeriodInLunarDays = 11.73f; // Realistic eclipse cycle
        long nodalPeriodTicks = (long) (nodalPeriodInLunarDays * lunarDayTicks); // Use lunar day length!
        float orbitalInclination = 5.14f;

        // Sun moves on celestial sphere with orbital inclination
        // The inclination() method creates a sine wave that determines eclipse seasons
        SkyRenderData sun = new SkyRenderData.Builder(
                new ResourceLocation("susy", "textures/environment/sun.png"),
                10.0F)
                        .positionType(SkyRenderData.PositionType.CELESTIAL_SPHERE)
                        .useLinearFiltering(false)
                        .baseInclination(5.14f)           // Sun's path is tilted 5.14° by default
                        .inclination(5.14f, nodalPeriodTicks) // Oscillates ±5.14° around base
                        .build();

        // Earth stays at zenith and rotates to show phases
        SkyRenderData earth = new SkyRenderData.Builder(
                new ResourceLocation("susy", "textures/environment/earth_phases.png"),
                40.0F)
                        .positionType(SkyRenderData.PositionType.ZENITH)
                        .rotationX(90.0F)
                        .phases(4, 2, 29.53F)
                        .useLinearFiltering(false)
                        .brightness(0.8F)
                        .mirrorTexture(true)
                        .build();

        // Set the celestial objects
        moonSky.setCelestialObjects(sun, earth);

        // Create custom sky colors for the Moon (black sky with no atmosphere)
        SkyColorData moonColors = new SkyColorData.Builder()
                .sunriseColor(0.0, 0.0, 0.0)       // Pure black
                .noonColor(0.0, 0.0, 0.0)          // Pure black
                .sunsetColor(0.0, 0.0, 0.0)        // Pure black
                .midnightColor(0.0, 0.0, 0.0)      // Pure black
                .noFog()                            // No atmospheric fog
                .build();

        // Set the sky colors
        moonSky.setSkyColorData(moonColors);

        new Planet(0, 800, "Moon").setBiomeList(
                new SuSyBiomeEntry(SuSyBiomes.LUNAR_HIGHLANDS, 100)
                        .setCraterBlock(SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.HIGHLAND)),
                new SuSyBiomeEntry(SuSyBiomes.LUNAR_MARIA, 100)
                        .setCraterBlock(SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.LOWLAND)))
                .setStone(SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.SMOOTH)
                        .getState(SusyStoneVariantBlock.StoneType.ANORTHOSITE))
                .setSuSySkyRenderer(moonSky)
                .setGravity(0.166f)
                .setBiomeSize(7)
                .setTicksPerDay(lunarDayTicks)
                .setDayLength(29.53f)
                .setTimeOffset(0.0f)
                .load();
    }
}
