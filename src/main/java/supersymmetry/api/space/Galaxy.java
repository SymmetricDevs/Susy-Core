package supersymmetry.api.space;

import org.jetbrains.annotations.Nullable;

public class Galaxy extends CelestialObject {

    private GalaxyType galaxyType;

    public Galaxy(double mass, double posT, double posX, double posY, double posZ, @Nullable CelestialObject parentBody, GalaxyType galaxyType) {
        super(mass, posT, posX, posY, posZ, parentBody, CelestialBodyType.GALAXY);
        this.galaxyType = galaxyType;
    }

    public GalaxyType getGalaxyType() {
        return galaxyType;
    }

}
