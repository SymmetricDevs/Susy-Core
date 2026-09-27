package supersymmetry.api.space;

import net.minecraft.util.math.Vec3d;

public class StarLight {

    public final Vec3d direction;
    public final Vec3d color;
    public final float intensity;

    public StarLight(Vec3d direction, Vec3d color, float intensity) {
        this.direction = direction;
        this.color = color;
        this.intensity = intensity;
    }
}
