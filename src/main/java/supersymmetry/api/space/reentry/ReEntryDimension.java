package supersymmetry.api.space.reentry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

import supersymmetry.api.SusyLog;
import supersymmetry.api.image.Cubemap;
import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.space.RenderableCelestialObject;

public class ReEntryDimension {

    public final int dimId;

    public final String name;

    public long orbitalPeriodTicks = 110_400L;

    public float reEntryFraction = 1.0f;

    public float transferAltitude = 5000f;

    public int targetDimensionId = 0;

    public float gravity = 0.0f;

    public float ambientLight = 0.02f;

    public ReEntryRenderer renderer = null;

    public RenderableCelestialObject renderableEarth;
    public RenderableCelestialObject renderableSun;
    public RenderableCelestialObject renderableMoon;

    public ReEntryDimension(int dimId, String name) {
        this.dimId = dimId;
        this.name = name;
    }

    public ReEntryDimension setOrbitalPeriod(long ticks) {
        this.orbitalPeriodTicks = ticks;
        return this;
    }

    public ReEntryDimension setTransferAltitude(float metres) {
        this.transferAltitude = metres;
        return this;
    }

    public ReEntryDimension setTargetDimension(int dimId) {
        this.targetDimensionId = dimId;
        return this;
    }

    public ReEntryDimension setGravity(float g) {
        this.gravity = g;
        return this;
    }

    public ReEntryDimension setAmbientLight(float light) {
        this.ambientLight = light;
        return this;
    }

    public ReEntryDimension setRenderer(ReEntryRenderer r) {
        this.renderer = r;
        return this;
    }

    public ReEntryDimension load() {
        ReEntryDimensions.REENTRY.put(dimId, this);

        if (!DimensionManager.isDimensionRegistered(dimId)) {
            DimensionManager.registerDimension(dimId, ReEntryDimensions.reEntryType);
            SusyLog.logger.info("[ReEntry] Registered re-entry dimension '" + name + "' at id " + dimId);
        }
        return this;
    }

    /**
     * Convenience builder that wires up the standard Earth re-entry scene.
     *
     * @param dimId       dimension id to register
     * @param targetDimId surface dimension the player transfers into
     */
    public static ReEntryDimension createEarthReEntry(int dimId, int targetDimId) {
        long leoTicks = 110_400L;

        Cubemap sunCubemap = new Cubemap(
                new ResourceLocation("susy", "textures/space/sun/cubemap.png"));
        RenderableCelestialObject sun = new RenderableCelestialObject(CelestialObjects.SUN, sunCubemap)
                .setAngularSize(20.0f)
                .setOrbitalPeriod(leoTicks)
                .setOrbitalInclination(23.5f);

        Cubemap moonCubemap = new Cubemap(
                new ResourceLocation("susy", "textures/space/moon/cubemap.png"));
        RenderableCelestialObject moon = new RenderableCelestialObject(CelestialObjects.MOON, moonCubemap)
                .setAngularSize(4.0f)
                .setOrbitalPeriod(708_734L)
                .setOrbitalInclination(5.14f)
                .setTidallyLocked(true)
                .setSunReference(sun);

        Cubemap earthCubemap = new Cubemap(
                new ResourceLocation("susy", "textures/space/earth/cubemap.png"));
        RenderableCelestialObject earth = new RenderableCelestialObject(CelestialObjects.EARTH, earthCubemap)
                .setAngularSize(180.0f)          // rendered size is overridden per-frame by the renderer
                .setFixedDirection(0, -1, 0);    // directly below

        ReEntryRenderer renderer = null;
        if (FMLLaunchHandler.side() == Side.CLIENT) {
            renderer = new ReEntryRenderer()
                    .setSunObject(sun)
                    .setEarthObject(earth, earthCubemap, leoTicks)
                    .setCelestialObjects(moon);
        }

        ReEntryDimension dim = new ReEntryDimension(dimId, "earth_reentry")
                .setOrbitalPeriod(leoTicks)
                .setTransferAltitude(5000f)
                .setTargetDimension(targetDimId)
                .setGravity(0.0f)
                .setAmbientLight(0.02f)
                .setRenderer(renderer);

        dim.renderableEarth = earth;
        dim.renderableSun = sun;
        dim.renderableMoon = moon;

        return dim;
    }
}
