package supersymmetry.api.space;

import net.minecraft.util.math.Vec3d;

public interface CelestialFeature {

    void render(BodyRenderData context, Vec3d hostCenterRender, float hostRadiusRender,
                boolean fromSurface, Vec3d localUp);
}
