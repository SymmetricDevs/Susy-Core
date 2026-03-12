package supersymmetry.common.world;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
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
import supersymmetry.common.world.sky.SkyRenderData;

// thismightbenoah & martin are the same people
public class SuSyDimensions {

    public static DimensionType planetType;
    public static DimensionType spaceType;

    public static List<Biome> BIOMES = new ArrayList<>();
    public static Map<Integer, PlanetoidHandler> PLANETS = new Int2ObjectArrayMap<>();

    /** Registry of all SpaceDimensions, keyed by dimension id. Populated via SpaceDimension#load(). */
    public static Map<Integer, SpaceDimension> SPACE = new Int2ObjectArrayMap<>();

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

        Cubemap solCubemap = new Cubemap(
                new ResourceLocation("susy", "textures/space/sun/px.png"),
                new ResourceLocation("susy", "textures/space/sun/py.png"),
                new ResourceLocation("susy", "textures/space/sun/pz.png"),
                new ResourceLocation("susy", "textures/space/sun/nx.png"),
                new ResourceLocation("susy", "textures/space/sun/ny.png"),
                new ResourceLocation("susy", "textures/space/sun/nz.png"));
        RenderableCelestialObject SUN = new RenderableCelestialObject(CelestialObjects.SUN, solCubemap)
                .setAngularSize(20.0f)
                .setOrbitalPeriod(24000L)
                .setOrbitalInclination(23.5f);

        Cubemap moonCubemap = new Cubemap(
                new ResourceLocation("susy", "textures/space/moon/cubemap.png"));
        long lunarDayTicks = 708734L;
        RenderableCelestialObject renderableMoon = new RenderableCelestialObject(CelestialObjects.MOON, moonCubemap)
                .setAngularSize(20.0f)
                .setOrbitalPeriod(lunarDayTicks)
                .setOrbitalInclination(5.14f);

        Cubemap earthCubemap = new Cubemap(new ResourceLocation("susy", "textures/space/earth/cubemap.png"));
        RenderableCelestialObject renderableEarth = new RenderableCelestialObject(CelestialObjects.EARTH, earthCubemap)
                .setAngularSize(180.0f)
                .setFixedDirection(0, -1, 0);

        SuSySkyRenderer moonSky = new SuSySkyRenderer();

        float nodalPeriodInLunarDays = 11.73f;
        long nodalPeriodTicks = (long) (nodalPeriodInLunarDays * lunarDayTicks);

        SkyRenderData sun = new SkyRenderData.Builder(
                new ResourceLocation("susy", "textures/environment/sun.png"), 10.6F)
                        .positionType(SkyRenderData.PositionType.CELESTIAL_SPHERE)
                        .useLinearFiltering(false)
                        .baseInclination(5.14f)
                        .inclination(5.14f, nodalPeriodTicks)
                        .build();

        SkyRenderData earth = new SkyRenderData.Builder(
                new ResourceLocation("susy", "textures/environment/earth_phases.png"), 40.0F)
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

        SuSySpaceRenderer leoRenderer = null;

        long leoOrbitTicks = 110_400L;
        // After creating leoRenderer and earthCubemap:
        if (FMLLaunchHandler.side() == Side.CLIENT) {
            leoRenderer = new SuSySpaceRenderer();
            leoRenderer.setCelestialObjects(renderableEarth, renderableMoon, SUN);
            leoRenderer.setOrbitalBody(renderableEarth, earthCubemap, leoOrbitTicks);
        }

        new SpaceDimension(802, "low_earth_orbit")
                .setOrbitTarget(renderableEarth)
                .setCelestialObjects(renderableEarth, renderableMoon, SUN)
                .setRenderer(leoRenderer)  // can be null on server, which is fine
                .setGravity(0.0f)
                .setAmbientLight(0.02f)
                .setVacuum(true)
                .setDayCycle(leoOrbitTicks, 1.53f, 0.0f)
                .load();

        // Register dim 802 with Forge's DimensionManager so it can be entered
        if (!DimensionManager.isDimensionRegistered(802)) {
            DimensionManager.registerDimension(802, spaceType);
            SusyLog.logger.info("Registered Low Earth Orbit space dimension at id 802");
        }
    }
}
