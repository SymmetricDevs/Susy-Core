package supersymmetry.api.space;

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

    public static void init() {
        MILKY_WAY = new Galaxy(1., 0., 0., 0., 0., null, GalaxyType.SPIRAL);

        SOLAR_SYSTEM = new StarSystem(1., 0., 0., 0., 0., MILKY_WAY);

        SUN = new Star(1., 0., 0., 0., 0., SOLAR_SYSTEM, StarType.G);

        EARTH = new Planetoid(1., 0., 0., 0., 0., SUN, PlanetType.TERRESTRIAL)
                .setDimension(0);
        MOON = new Planetoid(0.0123, 0., 1., 0., 0., EARTH, PlanetType.TERRESTRIAL)
                .setDimension(800);
    }
}
