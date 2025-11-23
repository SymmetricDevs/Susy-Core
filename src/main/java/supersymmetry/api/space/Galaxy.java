package supersymmetry.api.space;

import org.jetbrains.annotations.Nullable;

public class Galaxy extends CelestialObject {

    private final GalaxyType galaxyType;

    public Galaxy(String translationKey, double mass, double posT, double posX, double posY, double posZ,
                  @Nullable CelestialObject parentBody,
                  GalaxyType galaxyType) {
        super(translationKey, posT, posX, posY, posZ, mass, CelestialBodyType.GALAXY, parentBody);
        this.galaxyType = galaxyType;
    }

    public GalaxyType getGalaxyType() {
        return galaxyType;
    }
}
