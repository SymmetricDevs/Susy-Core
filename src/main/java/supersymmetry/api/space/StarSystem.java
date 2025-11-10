package supersymmetry.api.space;

import org.jetbrains.annotations.Nullable;

public class StarSystem extends CelestialObject {

    public StarSystem(String translationKey, double mass, double posT, double posX, double posY, double posZ,
                      @Nullable CelestialObject parentBody) {
        super(translationKey, posT, posX, posY, posZ, mass, CelestialBodyType.STAR_SYSTEM, parentBody);
    }
}
