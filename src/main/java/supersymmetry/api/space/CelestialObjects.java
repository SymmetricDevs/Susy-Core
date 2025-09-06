package supersymmetry.api.space;

public class CelestialObjects {

    public Galaxy MILKY_WAY;

    public void init() {
        MILKY_WAY = new Galaxy(1., 0., 0., 0., 0., null, GalaxyType.SPIRAL);
    }

}
