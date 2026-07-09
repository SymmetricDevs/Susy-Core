package supersymmetry.api.space;

import java.util.Collections;
import java.util.List;

import net.minecraft.util.math.Vec3d;

public class BodyRenderData {

    public final CelestialObject source;
    public final Vec3d direction;
    public final double distanceAU;
    public final double angularSizeDeg;
    public final double bodyRadiusAU;
    public final boolean isStar;
    public final boolean isGroundBody;
    public final List<StarLight> lights;
    public final float[] viewMatrix;
    public final float[] projectionMatrix;

    public BodyRenderData(CelestialObject source, Vec3d direction, double distanceAU,
                          double angularSizeDeg, double bodyRadiusAU,
                          boolean isStar, boolean isGroundBody, List<StarLight> lights,
                          float[] viewMatrix, float[] projectionMatrix) {
        this.source = source;
        this.direction = direction;
        this.distanceAU = distanceAU;
        this.angularSizeDeg = angularSizeDeg;
        this.bodyRadiusAU = bodyRadiusAU;
        this.isStar = isStar;
        this.isGroundBody = isGroundBody;
        this.lights = lights;
        this.viewMatrix = viewMatrix;
        this.projectionMatrix = projectionMatrix;
    }

    public BodyRenderData(CelestialObject source, Vec3d direction, double distanceAU,
                          double angularSizeDeg, double bodyRadiusAU,
                          boolean isStar, boolean isGroundBody) {
        this(source, direction, distanceAU, angularSizeDeg, bodyRadiusAU, isStar, isGroundBody,
                Collections.emptyList(), null, null);
    }
}
