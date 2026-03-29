package supersymmetry.common.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

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

public class SuSyDimensions {

    public static DimensionType planetType;
    public static DimensionType spaceType;

    public static List<Biome> BIOMES = new ArrayList<>();
    public static Map<Integer, PlanetoidHandler> PLANETS = new Int2ObjectArrayMap<>();

    /** Registry of all SpaceDimensions, keyed by dimension id. Populated via SpaceDimension#load(). */
    public static Map<Integer, SpaceDimension> SPACE = new Int2ObjectArrayMap<>();

    static long leoOrbitTicks = 110_400L;

    public static void init() {
        int id = -2;
        for (DimensionType type : DimensionType.values()) {
            if (type.getId() < id) id = type.getId();
        }
        id--;

        SusyLog.logger.info("Registering planet dimension type at id " + id);
        planetType = DimensionType.register("Supersymmetry Planet", "_susy", id, WorldProviderPlanet.class, false);

        SusyLog.logger.info("Registering space dimension type at id " + (id - 1));
        spaceType = DimensionType.register("susy_space", "_susyspace", id - 1, WorldProviderSpace.class, false);

        // -----------------------------------------------------------------
        // Shared celestial objects
        // -----------------------------------------------------------------

        Cubemap solCubemap = new Cubemap(
                new ResourceLocation("susy", "textures/space/sun/cubemap.png"));
        RenderableCelestialObject SUN = new RenderableCelestialObject(CelestialObjects.SUN, solCubemap)
                .setAngularSize(20.0f)
                .setOrbitalPeriod(leoOrbitTicks)
                .setOrbitalInclination(23.5f);

        Cubemap moonCubemap = new Cubemap(
                new ResourceLocation("susy", "textures/space/moon/cubemap.png"));
        long lunarDayTicks = 708_734L;
        RenderableCelestialObject renderableMoon = new RenderableCelestialObject(CelestialObjects.MOON, moonCubemap)
                .setAngularSize(4.0f)
                .setOrbitalPeriod(lunarDayTicks)
                .setOrbitalInclination(5.14f)
                .setTidallyLocked(true)
                .setSunReference(SUN);

        Cubemap earthCubemap = new Cubemap(
                new ResourceLocation("susy", "textures/space/earth/cubemap.png"));
        RenderableCelestialObject renderableEarth = new RenderableCelestialObject(CelestialObjects.EARTH, earthCubemap)
                .setAngularSize(180.0f)
                .setFixedDirection(0, -1, 0);

        // BEGIN MOON

        RenderableCelestialObject renderableEarthMoon = new RenderableCelestialObject(CelestialObjects.EARTH,
                earthCubemap)
                        .setAngularSize(15.0f)
                        .setFixedDirection(0, 1, 0); // straight up overhead

        SuSySkyRenderer moonSky = null;

        if (FMLLaunchHandler.side() == Side.CLIENT) {
            moonSky = new SuSySkyRenderer();

            moonSky.sunAngularRadius = 0.00935f;
            moonSky.sunColor = new float[] { 1.0f, 0.95f, 0.8f };
            moonSky.diskIntensity = 20.0f;
            moonSky.coronaScale = 6.0f;
            moonSky.limbDarkening = 0.85f;

            // Sun handled separately by renderSunShader().
            // Earth rendered as a shader-lit sphere in the sky.
            moonSky.setSunObject(SUN);
            moonSky.setCelestialObjects(renderableEarthMoon);

            // Pitch-black sky – no atmosphere on the Moon.
            SkyColorData moonColors = new SkyColorData.Builder()
                    .sunriseColor(0.0, 0.0, 0.0)
                    .noonColor(0.0, 0.0, 0.0)
                    .sunsetColor(0.0, 0.0, 0.0)
                    .midnightColor(0.0, 0.0, 0.0)
                    .noFog()
                    .build();
            moonSky.setSkyColorData(moonColors);
        }

        new PlanetoidHandler(CelestialObjects.MOON).setBiomeList(
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

        // END MOON

        // BEGIN LEO
        SuSySpaceRenderer leoRenderer = null;

        if (FMLLaunchHandler.side() == Side.CLIENT) {
            leoRenderer = new SuSySpaceRenderer();
            leoRenderer.setCelestialObjects(renderableMoon, renderableEarth);
            leoRenderer.setOrbitalBody(renderableEarth, earthCubemap, leoOrbitTicks);
            leoRenderer.setSunObject(SUN);
        }

        new SpaceDimension(802, "low_earth_orbit")
                .setOrbitTarget(renderableEarth)
                .setCelestialObjects(SUN, renderableMoon, renderableEarth)
                .setRenderer(leoRenderer)
                .setGravity(0.0f)
                .setAmbientLight(0.02f)
                .setVacuum(true)
                .setDayCycle(leoOrbitTicks, 1.53f, 0.0f)
                .load();

        if (!DimensionManager.isDimensionRegistered(802)) {
            DimensionManager.registerDimension(802, spaceType);
            SusyLog.logger.info("Registered Low Earth Orbit space dimension at id 802");
        }
        // END LEO
    }
}
