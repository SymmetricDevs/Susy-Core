package supersymmetry.client.shaders.space;

import net.minecraft.client.Minecraft;
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
    private float rotationPeriodTicks = 0f;

    public CubemapPlanetRenderer(Cubemap cubemap) {
        this.cubemap = cubemap;
    }

    public CubemapPlanetRenderer setRotationPeriod(float ticks) {
        this.rotationPeriodTicks = ticks;
        return this;
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

        long worldTime = 0;
        if (Minecraft.getMinecraft().world != null) {
            worldTime = Minecraft.getMinecraft().world.getWorldTime();
        }
        float spinAngle = rotationPeriodTicks > 0f ?
                (float) (worldTime % (long) rotationPeriodTicks) / rotationPeriodTicks * (float) Math.PI * 2f : 0f;
        float[] rot = buildCubemapRotation(dx, dy, dz, spinAngle);

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

    private static float[] buildCubemapRotation(float dx, float dy, float dz, float spinAngle) {
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

        if (spinAngle != 0f) {
            float cosA = (float) Math.cos(spinAngle);
            float sinA = (float) Math.sin(spinAngle);

            float nrx = rx * cosA + (dy * rz - dz * ry) * sinA + dx * (dx * rx + dy * ry + dz * rz) * (1f - cosA);
            float nry = ry * cosA + (dz * rx - dx * rz) * sinA + dy * (dx * rx + dy * ry + dz * rz) * (1f - cosA);
            float nrz = rz * cosA + (dx * ry - dy * rx) * sinA + dz * (dx * rx + dy * ry + dz * rz) * (1f - cosA);

            float nupx = upx * cosA + (dy * upz - dz * upy) * sinA +
                    dx * (dx * upx + dy * upy + dz * upz) * (1f - cosA);
            float nupy = upy * cosA + (dz * upx - dx * upz) * sinA +
                    dy * (dx * upx + dy * upy + dz * upz) * (1f - cosA);
            float nupz = upz * cosA + (dx * upy - dy * upx) * sinA +
                    dz * (dx * upx + dy * upy + dz * upz) * (1f - cosA);

            rx = nrx;
            ry = nry;
            rz = nrz;
            upx = nupx;
            upy = nupy;
            upz = nupz;
        }

        return new float[] {
                -rx, -ry, -rz, 0f,
                upx, upy, upz, 0f,
                dx, dy, dz, 0f,
                0f, 0f, 0f, 1f
        };
    }
}
