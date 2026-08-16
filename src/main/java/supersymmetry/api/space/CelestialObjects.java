package supersymmetry.api.space;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

import supersymmetry.api.image.Cubemap;
import supersymmetry.client.shaders.space.CubemapPlanetRenderer;
import supersymmetry.client.shaders.space.SunGlowRenderer;

public class CelestialObjects {

    // Galaxies
    public static Galaxy MILKY_WAY;

    // Star clusters

    // Star systems
    public static StarSystem SOLAR_SYSTEM;

    // Stars
    public static Star SUN;

    // Planetoids
    public static Planetoid EARTH;
    public static Planetoid MOON;
    public static Planetoid MARS;
    public static Planetoid SATURN;

    // Cubemaps
    public static Cubemap SUN_CUBEMAP;
    public static Cubemap EARTH_CUBEMAP;
    public static Cubemap MOON_CUBEMAP;

    public static CelestialRenderer RENDERER;

    public static void init() {
        MILKY_WAY = new Galaxy("milky_way", 1., 0., 0., 0., 0., null, GalaxyType.SPIRAL);

        SOLAR_SYSTEM = new StarSystem("solar_system", 1., 0., 0., 0., 0., MILKY_WAY);

        SUN = new Star("sun", 1., 0., 0., 0., 0., SOLAR_SYSTEM, StarType.G);
        SUN.setRadius(109.2);

        EARTH = new Planetoid("earth", 1., 0., 0., 0., 0., SUN, PlanetType.TERRESTRIAL)
                .setDimension(0);

        MOON = new Planetoid("moon", 0.0123, 0., 1., 0., 0., EARTH, PlanetType.TERRESTRIAL)
                .setDimension(800);
        MOON.setRadius(0.2724);

        SUN_CUBEMAP = new Cubemap(
                new ResourceLocation("susy", "textures/space/sun/cubemap.png"));
        EARTH_CUBEMAP = new Cubemap(
                new ResourceLocation("susy", "textures/space/earth/cubemap.png"));
        MOON_CUBEMAP = new Cubemap(
                new ResourceLocation("susy", "textures/space/moon/cubemap.png"));

        CelestialOrbitRegistry.register(EARTH, new Orbit(
                1.0, 0.0167086, 0.0, 0.0,
                Math.toRadians(288.1), Math.toRadians(357.5), 0L, 8766000L));

        CelestialOrbitRegistry.register(MOON, new Orbit(
                0.00257, 0.0549, Math.toRadians(5.145), Math.toRadians(125.08),
                Math.toRadians(318.06), Math.toRadians(38.34), 0L, 655720L));

        MOON.setRotationAxis(CelestialOrbitRegistry.get(MOON).computeOrbitalNormal())
                .setRotationPeriodTicks(655720L);
        Vec3d moonNormal = CelestialOrbitRegistry.get(MOON).computeOrbitalNormal();
        double obliquity = Math.toRadians(23.44);
        double tiltAzimuth = Math.atan2(moonNormal.z, moonNormal.x) + Math.PI;
        EARTH.setRotationAxis(new Vec3d(
                Math.sin(obliquity) * Math.cos(tiltAzimuth),
                Math.cos(obliquity),
                Math.sin(obliquity) * Math.sin(tiltAzimuth)))
                .setRotationPeriodTicks(23934L);

        if (FMLLaunchHandler.side() == Side.CLIENT) {
            RENDERER = new CelestialRenderer();
            RENDERER.registerRenderer(SUN, new SunGlowRenderer());
            RENDERER.registerRenderer(EARTH, new CubemapPlanetRenderer(EARTH_CUBEMAP));
            RENDERER.registerRenderer(MOON, new CubemapPlanetRenderer(MOON_CUBEMAP));

            // EARTH.addFeature(new RingFeatureRenderer(1.5, 3.5, 0.1, new Vec3d(0.85, 0.75, 0.6)));
            // EARTH.addFeature(new RingFeatureRenderer(4.0, 6.0, 0.1, new Vec3d(0.7, 0.6, 0.5)));
        }
    }
}
