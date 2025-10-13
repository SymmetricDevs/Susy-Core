package supersymmetry.api.space;

import org.jetbrains.annotations.Nullable;

public class Star extends CelestialObject {

    private final StarType starType;

    public Star(double mass, double posT, double posX, double posY, double posZ, @Nullable CelestialObject parentBody,
                StarType starType) {
        super(mass, posT, posX, posY, posZ, parentBody, CelestialBodyType.STAR);
        this.starType = starType;
    }

    public StarType getStarType() {
        return starType;
    }
}
