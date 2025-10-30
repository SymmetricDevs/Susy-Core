package supersymmetry.api.space;

import org.jetbrains.annotations.Nullable;

public class Star extends CelestialObject {

    private final StarType starType;

    public Star(String translationKey, double mass, double posT, double posX, double posY, double posZ, @Nullable CelestialObject parentBody,
                StarType starType) {
        super(translationKey, posT, posX, posY, posZ, mass, CelestialBodyType.STAR, parentBody);
        this.starType = starType;
    }

    public StarType getStarType() {
        return starType;
    }
}
