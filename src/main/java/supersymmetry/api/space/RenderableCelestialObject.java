package supersymmetry.api.space;

import net.minecraft.client.renderer.GlStateManager;

import org.lwjgl.opengl.GL11;

import supersymmetry.api.SusyLog;
import supersymmetry.api.image.Cubemap;

public class RenderableCelestialObject {

    private final CelestialObject object;
    private final Cubemap cubemap;

    private float angularSizeDeg = 20.0f;
    private long orbitalPeriodTicks = 0L;
    private float orbitalInclinationDeg = 0.0f;
    private long phaseOffsetTicks = 0L;

    private boolean hasFixedDirection = false;
    private float fixedDx, fixedDy, fixedDz;

    private boolean loadAttempted = false;

    public RenderableCelestialObject(CelestialObject object, Cubemap cubemap) {
        this.object = object;
        this.cubemap = cubemap;
    }

    public RenderableCelestialObject setAngularSize(float degrees) {
        this.angularSizeDeg = degrees;
        return this;
    }

    public RenderableCelestialObject setOrbitalPeriod(long ticks) {
        this.orbitalPeriodTicks = ticks;
        return this;
    }

    public RenderableCelestialObject setOrbitalInclination(float degrees) {
        this.orbitalInclinationDeg = degrees;
        return this;
    }

    public RenderableCelestialObject setPhaseOffset(long ticks) {
        this.phaseOffsetTicks = ticks;
        return this;
    }

    public RenderableCelestialObject setFixedDirection(float dx, float dy, float dz) {
        this.hasFixedDirection = true;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        this.fixedDx = dx / len;
        this.fixedDy = dy / len;
        this.fixedDz = dz / len;
        return this;
    }

    public CelestialObject getCelestialObject() {
        return object;
    }

    private boolean ensureLoaded() {
        if (loadAttempted) return cubemap.isLoaded();
        loadAttempted = true;
        try {
            cubemap.loadAll();
            SusyLog.logger.info("[Space] Cubemap loaded for " + object.getTranslationKey());
            return true;
        } catch (Exception e) {
            SusyLog.logger.error("[Space] Cubemap FAILED for " + object.getTranslationKey(), e);
            return false;
        }
    }

    public void renderAtPosition(long worldTime, QuadSphere mesh) {
        float dx, dy, dz;
        if (hasFixedDirection) {
            dx = fixedDx;
            dy = fixedDy;
            dz = fixedDz;
        } else {
            double angle = orbitalPeriodTicks > 0 ? ((worldTime + phaseOffsetTicks) % orbitalPeriodTicks) /
                    (double) orbitalPeriodTicks * 2.0 * Math.PI : 0.0;
            float incRad = (float) Math.toRadians(orbitalInclinationDeg);
            dx = (float) Math.cos(angle);
            dy = (float) (Math.sin(angle) * Math.sin(incRad));
            dz = (float) (Math.sin(angle) * Math.cos(incRad));
        }

        float radius = (float) Math.tan(Math.toRadians(angularSizeDeg / 2.0));
        boolean hasTexture = ensureLoaded();

        GlStateManager.pushMatrix();
        GL11.glTranslatef(dx, dy, dz);
        GL11.glScalef(radius, radius, radius);

        if (hasTexture) {
            GlStateManager.enableTexture2D();
            GL11.glColor4f(1f, 1f, 1f, 1f);

            // QuadSphere builds 6 faces in order: +X,-X,+Y,-Y,+Z,-Z
            // Each face has subdivisions² quads.
            // We bind the matching cubemap face texture for each group.
            int subdivs = mesh.subdivisions;
            int quadsPerFace = subdivs * subdivs;
            java.util.List<int[]> allQuads = mesh.getQuads();
            java.util.List<QuadSphere.Vertex> verts = mesh.getVertices();

            for (int face = 0; face < 6; face++) {
                int faceTexId = cubemap.getFaceTexId(face);
                if (faceTexId == -1) continue;

                GlStateManager.bindTexture(faceTexId);

                GL11.glBegin(GL11.GL_QUADS);
                int start = face * quadsPerFace;
                int end = start + quadsPerFace;
                for (int qi = start; qi < end; qi++) {
                    int[] quad = allQuads.get(qi);
                    for (int idx : quad) {
                        QuadSphere.Vertex v = verts.get(idx);
                        // Map the vertex position on the unit sphere to a UV
                        // within this face's [0,1]² space using the face's
                        // two tangent axes.
                        float[] uv = faceUV(face, v.nx, v.ny, v.nz);
                        GL11.glTexCoord2f(uv[0], uv[1]);
                        GL11.glVertex3f(v.x, v.y, v.z);
                    }
                }
                GL11.glEnd();
            }

        } else {
            // Magenta fallback
            GlStateManager.disableTexture2D();
            GL11.glColor4f(1f, 0f, 1f, 1f);
            GL11.glBegin(GL11.GL_QUADS);
            for (int[] quad : mesh.getQuads()) {
                for (int idx : quad) {
                    QuadSphere.Vertex v = mesh.getVertices().get(idx);
                    GL11.glVertex3f(v.x, v.y, v.z);
                }
            }
            GL11.glEnd();
            GL11.glColor4f(1f, 1f, 1f, 1f);
            GlStateManager.enableTexture2D();
        }

        GlStateManager.popMatrix();
    }

    /**
     * Perspective-correct UV derived from QuadSphere face axes.
     * u = dot(n, axisA) / dot(n, faceNormal)
     * v = dot(n, axisB) / dot(n, faceNormal)
     * All verified correct with corner tests.
     */
    private static float[] faceUV(int face, float nx, float ny, float nz) {
        float u, v;
        switch (face) {
            case 0: // +X axisA=-Z axisB=-Y
                u = -nz / nx;
                v = -ny / nx;
                break;
            case 1: // -X axisA=+Z axisB=-Y
                u = nz / -nx;
                v = -ny / -nx;
                break;
            case 2: // +Y axisA=+X axisB=+Z
                u = nx / ny;
                v = nz / ny;
                break;
            case 3: // -Y axisA=+X axisB=-Z
                u = nx / -ny;
                v = -nz / -ny;
                break;
            case 4: // +Z axisA=+X axisB=-Y
                u = nx / nz;
                v = -ny / nz;
                break;
            case 5: // -Z axisA=-X axisB=-Y
                u = -nx / -nz;
                v = -ny / -nz;
                break;
            default:
                u = v = 0;
        }
        return new float[] { u * 0.5f + 0.5f, v * 0.5f + 0.5f };
    }
}
