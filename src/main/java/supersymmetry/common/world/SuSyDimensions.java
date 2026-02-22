package supersymmetry.common.world;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import supersymmetry.api.SusyLog;
import supersymmetry.api.image.Cubemap;
import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.space.RenderableCelestialObject;
import supersymmetry.api.space.dimension.SpaceDimension;
import supersymmetry.api.space.dimension.SuSySpaceRenderer;
import supersymmetry.api.space.dimension.WorldProviderSpace;
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

    public static Map<Integer, SpaceDimension> SPACE = new Int2ObjectArrayMap<>();

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

        SusyLog.logger.info("Registering space dimension type at id " + (id - 1));
        spaceType = DimensionType.register("susy_space", "_susyspace", id - 1, WorldProviderSpace.class, false);

        SuSySkyRenderer moonSky = new SuSySkyRenderer();

        long lunarDayTicks = 708734L; // 29.5306 * 24000

        float nodalPeriodInLunarDays = 11.73f;
        long nodalPeriodTicks = (long) (nodalPeriodInLunarDays * lunarDayTicks);
        float orbitalInclination = 5.14f;

        Cubemap solCubemap = new Cubemap(
                new ResourceLocation("susy", "textures/space/sun/sun/px.png"),
                new ResourceLocation("susy", "textures/space/sun/sun/py.png"),
                new ResourceLocation("susy", "textures/space/sun/sun/pz.png"),
                new ResourceLocation("susy", "textures/space/sun/sun/nx.png"),
                new ResourceLocation("susy", "textures/space/sun/sun/ny.png"),
                new ResourceLocation("susy", "textures/space/sun/sun/nz.png"));
        RenderableCelestialObject SUN = new RenderableCelestialObject(CelestialObjects.SUN, solCubemap);

        Cubemap moonCubemap = new Cubemap(
                new ResourceLocation("susy", "textures/space/moon/px.png"),
                new ResourceLocation("susy", "textures/space/moon/py.png"),
                new ResourceLocation("susy", "textures/space/moon/pz.png"),
                new ResourceLocation("susy", "textures/space/moon/nx.png"),
                new ResourceLocation("susy", "textures/space/moon/ny.png"),
                new ResourceLocation("susy", "textures/space/moon/nz.png"));
        Cubemap earthCubemap = new Cubemap(new ResourceLocation("susy", "textures/space/earth/cubemap.png"));

        RenderableCelestialObject renderableMoon = new RenderableCelestialObject(CelestialObjects.MOON, moonCubemap);
        RenderableCelestialObject renderableEarth = new RenderableCelestialObject(CelestialObjects.EARTH, earthCubemap);

        SkyRenderData sun = new SkyRenderData.Builder(
                new ResourceLocation("susy", "textures/environment/sun.png"),
                10.0F)
                        .positionType(SkyRenderData.PositionType.CELESTIAL_SPHERE)
                        .useLinearFiltering(false)
                        .baseInclination(5.14f)
                        .inclination(5.14f, nodalPeriodTicks)
                        .build();

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

        moonSky.setCelestialObjects(sun, earth);

        SkyColorData moonColors = new SkyColorData.Builder()
                .sunriseColor(0.0, 0.0, 0.0)
                .noonColor(0.0, 0.0, 0.0)
                .sunsetColor(0.0, 0.0, 0.0)
                .midnightColor(0.0, 0.0, 0.0)
                .noFog()
                .build();

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

        SuSySpaceRenderer leoRenderer = new SuSySpaceRenderer();
        leoRenderer.setCelestialObjects(renderableEarth, renderableMoon, SUN);

        // 92 minutes * 60 seconds * 20 ticks/s
        long leoOrbitTicks = 110_400L;

        new SpaceDimension(802, "low_earth_orbit")
                .setOrbitTarget(renderableEarth)
                .setCelestialObjects(renderableEarth, renderableMoon, SUN)
                .setRenderer(leoRenderer)
                .setGravity(0.0f)
                .setAmbientLight(0.02f)
                .setVacuum(true)
                .setDayCycle(leoOrbitTicks, 1.53f, 0.0f)
                .load();

        SusyLog.logger.info("Registered Low Earth Orbit space dimension at id 802");
    }
}
