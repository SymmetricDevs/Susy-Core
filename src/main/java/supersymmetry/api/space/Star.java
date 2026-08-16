package supersymmetry.api.space;

import net.minecraft.util.math.Vec3d;

import org.jetbrains.annotations.Nullable;

public class Star extends CelestialObject {

    private final StarType starType;
    private Vec3d color;

    public Star(String translationKey, double mass, double posT, double posX, double posY, double posZ,
                @Nullable CelestialObject parentBody, StarType starType) {
        super(translationKey, posT, posX, posY, posZ, mass, CelestialBodyType.STAR, parentBody);
        this.starType = starType;
    }

    public StarType getStarType() {
        return starType;
    }

    public Vec3d getColor() {
        if (color == null) {
            color = defaultColor(starType);
        }
        return color;
    }

    public void setColor(Vec3d color) {
        this.color = color;
    }

    public static Vec3d defaultColor(StarType type) {
        switch (type) {
            case O:
                return new Vec3d(0.61, 0.69, 1.0);
            case B:
                return new Vec3d(0.67, 0.77, 1.0);
            case A:
                return new Vec3d(0.84, 0.88, 1.0);
            case F:
                return new Vec3d(0.98, 0.95, 0.86);
            case G:
                return new Vec3d(1.0, 0.96, 0.91);
            case K:
                return new Vec3d(1.0, 0.82, 0.63);
            case M:
                return new Vec3d(1.0, 0.60, 0.40);
            default:
                return new Vec3d(1.0, 1.0, 1.0);
        }
    }
}
