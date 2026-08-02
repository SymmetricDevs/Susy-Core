package supersymmetry.client.shaders.space;

import net.minecraft.util.math.Vec3d;

import supersymmetry.api.image.Cubemap;
import supersymmetry.api.space.BodyRenderData;
import supersymmetry.api.space.BodyRenderer;
import supersymmetry.api.space.StarLight;
import supersymmetry.client.shaders.ShaderManager;
import supersymmetry.client.shaders.space.planet.PlanetSurfaceRenderer;

public class CubemapPlanetRenderer implements BodyRenderer {

    private final Cubemap cubemap;
    private final PlanetSurfaceRenderer planetSurface = new PlanetSurfaceRenderer();
    private boolean loadAttempted = false;

    public CubemapPlanetRenderer(Cubemap cubemap) {
        this.cubemap = cubemap;
    }

    @Override
    public void render(BodyRenderData data) {
        if (!ShaderManager.shadersAllowed()) return;

        ensureLoaded();

        boolean allValid = true;
        for (int i = 0; i < 6; i++) {
            if (cubemap.getFaceTexId(i) < 0) allValid = false;
        }
        if (!allValid) return;

        float[] viewMat = data.viewMatrix;
        float[] projMat = data.projectionMatrix;
        if (viewMat == null || projMat == null) return;

        Vec3d dir = data.direction;
        float dx = (float) dir.x;
        float dy = (float) dir.y;
        float dz = (float) dir.z;

        float angRad = (float) Math.toRadians(data.angularSizeDeg / 2.0);
        float scale = 100f * (float) Math.tan(angRad);

        float[] planetPos = new float[] { dx * 100f, dy * 100f, dz * 100f };

        float spinAngle = (float) data.source.getRotationAngle(data.worldTime);
        float[] rot = buildCubemapRotation(dx, dy, dz, cubemap.getRotationAxis(), spinAngle);

        float[] sunDir;
        if (!data.lights.isEmpty()) {
            StarLight light = data.lights.get(0);
            sunDir = new float[] {
                    (float) light.direction.x,
                    (float) light.direction.y,
                    (float) light.direction.z
            };
        } else {
            sunDir = new float[] { 0f, 1f, 0f };
        }

        int[] faces = new int[6];
        for (int i = 0; i < 6; i++) faces[i] = cubemap.getFaceTexId(i);

        planetSurface.render(viewMat, projMat, sunDir, planetPos, scale, rot, faces);
    }

    private void ensureLoaded() {
        if (loadAttempted) return;
        loadAttempted = true;
        try {
            cubemap.loadAll();
        } catch (Exception e) {}
    }

    private static float[] buildCubemapRotation(float dx, float dy, float dz, Vec3d spinAxis, float spinAngle) {
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-6f) return new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
        dx /= len;
        dy /= len;
        dz /= len;

        float ux = 0f, uy = 1f, uz = 0f;
        if (Math.abs(dy) > 0.99f) {
            ux = 0f;
            uy = 0f;
            uz = -1f;
        }

        float rx = dy * uz - dz * uy;
        float ry = dz * ux - dx * uz;
        float rz = dx * uy - dy * ux;
        float rlen = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
        if (rlen < 1e-6f) return new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
        rx /= rlen;
        ry /= rlen;
        rz /= rlen;

        float upx = ry * dz - rz * dy;
        float upy = rz * dx - rx * dz;
        float upz = rx * dy - ry * dx;

        float sx = upx;
        float sy = upy;
        float sz = upz;
        if (spinAxis != null) {
            float slen = (float) spinAxis.length();
            if (slen > 1e-6f) {
                sx = (float) (spinAxis.x / slen);
                sy = (float) (spinAxis.y / slen);
                sz = (float) (spinAxis.z / slen);
            }
        }

        float cosA = (float) Math.cos(spinAngle);
        float sinA = (float) Math.sin(spinAngle);
        float t = 1f - cosA;

        float r0x = cosA + sx * sx * t;
        float r0y = sy * sx * t - sz * sinA;
        float r0z = sz * sx * t + sy * sinA;
        float r1x = sx * sy * t + sz * sinA;
        float r1y = cosA + sy * sy * t;
        float r1z = sz * sy * t - sx * sinA;
        float r2x = sx * sz * t - sy * sinA;
        float r2y = sy * sz * t + sx * sinA;
        float r2z = cosA + sz * sz * t;

        float m0x = r0x * -rx + r0y * upx + r0z * dx;
        float m0y = r0x * -ry + r0y * upy + r0z * dy;
        float m0z = r0x * -rz + r0y * upz + r0z * dz;

        float m1x = r1x * -rx + r1y * upx + r1z * dx;
        float m1y = r1x * -ry + r1y * upy + r1z * dy;
        float m1z = r1x * -rz + r1y * upz + r1z * dz;

        float m2x = r2x * -rx + r2y * upx + r2z * dx;
        float m2y = r2x * -ry + r2y * upy + r2z * dy;
        float m2z = r2x * -rz + r2y * upz + r2z * dz;

        return new float[] {
                m0x, m0y, m0z, 0f,
                m1x, m1y, m1z, 0f,
                m2x, m2y, m2z, 0f,
                0f, 0f, 0f, 1f
        };
    }
}
