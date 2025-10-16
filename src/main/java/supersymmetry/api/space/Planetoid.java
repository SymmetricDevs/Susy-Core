package supersymmetry.api.space;

import org.jetbrains.annotations.Nullable;

public class Planetoid extends CelestialObject {

    private PlanetType planetType;

    public Planetoid(double mass, double posT, double posX, double posY, double posZ,
                     @Nullable CelestialObject parentBody, PlanetType planetType) {
        super(mass, posT, posX, posY, posZ, parentBody, CelestialBodyType.PLANETOID);
        this.planetType = planetType;
    }

    public PlanetType getPlanetType() {
        return planetType;
    }

    public void setPlanetType(PlanetType planetType) {
        this.planetType = planetType;
    }
}
