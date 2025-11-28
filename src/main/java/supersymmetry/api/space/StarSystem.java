package supersymmetry.api.space;

import org.jetbrains.annotations.Nullable;

public class StarSystem extends CelestialObject {

    public StarSystem(double mass, double posT, double posX, double posY, double posZ,
                      @Nullable CelestialObject parentBody) {
        super(mass, posT, posX, posY, posZ, parentBody, CelestialBodyType.STAR_SYSTEM);
    }
}
