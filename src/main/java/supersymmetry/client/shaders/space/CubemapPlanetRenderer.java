package supersymmetry.client.shaders.space;

import net.minecraft.util.math.Vec3d;

import supersymmetry.api.image.Cubemap;
import supersymmetry.api.space.BodyRenderData;
import supersymmetry.api.space.BodyRenderer;
import supersymmetry.api.space.StarLight;
import supersymmetry.api.util.Quaternion;
import supersymmetry.client.shaders.ShaderManager;
import supersymmetry.client.shaders.space.planet.PlanetSurfaceRenderer;

// godless class
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
        Vec3d spinAxis = data.spinAxis;
        float[] rot = buildCubemapRotation(dx, dy, dz, spinAxis, spinAngle);

        float[] sunDir;
        float[] sunColor;
        if (!data.lights.isEmpty()) {
            StarLight light = data.lights.get(0);
            sunDir = new float[] {
                    (float) light.direction.x,
                    (float) light.direction.y,
                    (float) light.direction.z
            };
            sunColor = new float[] {
                    (float) light.color.x,
                    (float) light.color.y,
                    (float) light.color.z
            };
        } else {
            sunDir = new float[] { 0f, 1f, 0f };
            sunColor = new float[] { 1f, 1f, 1f };
        }

        int[] faces = new int[6];
        for (int i = 0; i < 6; i++) faces[i] = cubemap.getFaceTexId(i);

        planetSurface.render(viewMat, projMat, sunDir, sunColor, planetPos, scale, rot, faces);
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

                float e2x = dx - sx * (sx * dx + sy * dy + sz * dz);
                float e2y = dy - sy * (sx * dx + sy * dy + sz * dz);
                float e2z = dz - sz * (sx * dx + sy * dy + sz * dz);
                float e2len = (float) Math.sqrt(e2x * e2x + e2y * e2y + e2z * e2z);
                if (e2len > 1e-6f) {
                    float sPerp = (float) Math.sqrt(sx * sx + sz * sz);
                    if (sPerp > 1e-6f) {
                        float qx = -sx * sy / sPerp;
                        float qy = sPerp;
                        float qz = -sz * sy / sPerp;
                        float vx = sy * qz - sz * qy;
                        float vy = sz * qx - sx * qz;
                        float vz = sx * qy - sy * qx;
                        float b0x = (sx * qx - sz * vx) / sPerp;
                        float b0y = (sx * qy - sz * vy) / sPerp;
                        float b0z = (sx * qz - sz * vz) / sPerp;
                        float b2x = (sz * qx + sx * vx) / sPerp;
                        float b2y = (sz * qy + sx * vy) / sPerp;
                        float b2z = (sz * qz + sx * vz) / sPerp;
                        rx = -b0x;
                        ry = -b0y;
                        rz = -b0z;
                        upx = sx;
                        upy = sy;
                        upz = sz;
                        dx = b2x;
                        dy = b2y;
                        dz = b2z;
                    } else {
                        float b2x = e2x / e2len;
                        float b2y = e2y / e2len;
                        float b2z = e2z / e2len;
                        rx = sy * b2z - sz * b2y;
                        ry = sz * b2x - sx * b2z;
                        rz = sx * b2y - sy * b2x;
                        upx = sx;
                        upy = sy;
                        upz = sz;
                        dx = b2x;
                        dy = b2y;
                        dz = b2z;
                    }
                }
            }
        }

        double[] quatMat = Quaternion.fromAxisAngle(new Vec3d(sx, sy, sz), spinAngle).toMatrix3x3Flat();
        float r0x = (float) quatMat[0];
        float r0y = (float) quatMat[1];
        float r0z = (float) quatMat[2];
        float r1x = (float) quatMat[3];
        float r1y = (float) quatMat[4];
        float r1z = (float) quatMat[5];
        float r2x = (float) quatMat[6];
        float r2y = (float) quatMat[7];
        float r2z = (float) quatMat[8];

        float m0x = -rx * r0x + upx * r1x + dx * r2x;
        float m0y = -ry * r0x + upy * r1x + dy * r2x;
        float m0z = -rz * r0x + upz * r1x + dz * r2x;

        float m1x = -rx * r0y + upx * r1y + dx * r2y;
        float m1y = -ry * r0y + upy * r1y + dy * r2y;
        float m1z = -rz * r0y + upz * r1y + dz * r2y;

        float m2x = -rx * r0z + upx * r1z + dx * r2z;
        float m2y = -ry * r0z + upy * r1z + dy * r2z;
        float m2z = -rz * r0z + upz * r1z + dz * r2z;

        return new float[] {
                m0x, m0y, -m0z, 0f,
                m1x, m1y, -m1z, 0f,
                m2x, m2y, -m2z, 0f,
                0f, 0f, 0f, 1f
        };
    }
}
