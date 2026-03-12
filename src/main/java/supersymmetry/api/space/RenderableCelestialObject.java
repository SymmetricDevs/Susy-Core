package supersymmetry.api.space;

import java.util.List;

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

    /**
     * Returns the normalised world-space direction toward this object at the given world time.
     * Reused by both renderAtPosition and the depth-sort in SuSySpaceRenderer.
     */
    public float[] getWorldDirection(long worldTime) {
        if (hasFixedDirection) {
            return new float[] { fixedDx, fixedDy, fixedDz };
        }
        double angle = orbitalPeriodTicks > 0 ?
                ((worldTime + phaseOffsetTicks) % orbitalPeriodTicks) / (double) orbitalPeriodTicks * 2.0 * Math.PI :
                0.0;
        float incRad = (float) Math.toRadians(orbitalInclinationDeg);
        return new float[] {
                (float) Math.cos(angle),
                (float) (Math.sin(angle) * Math.sin(incRad)),
                (float) (Math.sin(angle) * Math.cos(incRad))
        };
    }

    public void renderAtPosition(long worldTime, QuadSphere mesh) {
        float[] dir = getWorldDirection(worldTime);
        float dx = dir[0], dy = dir[1], dz = dir[2];

        float radius = (float) Math.tan(Math.toRadians(angularSizeDeg / 2.0));
        boolean hasTexture = ensureLoaded();

        GlStateManager.pushMatrix();
        GL11.glTranslatef(dx, dy, dz);
        GL11.glScalef(radius, radius, radius);

        if (hasTexture) {
            GlStateManager.enableTexture2D();
            GL11.glColor4f(1f, 1f, 1f, 1f);

            List<int[]> allQuads = mesh.getQuads();
            List<float[][]> allUVs = mesh.getQuadUVs();
            List<List<Integer>> faceQuadIndices = mesh.getFaceQuadIndices();
            List<QuadSphere.Vertex> verts = mesh.getVertices();

            for (int face = 0; face < 6; face++) {
                int faceTexId = cubemap.getFaceTexId(face);
                if (faceTexId == -1) continue;

                GlStateManager.bindTexture(faceTexId);
                GL11.glBegin(GL11.GL_QUADS);

                for (int qi : faceQuadIndices.get(face)) {
                    int[] quad = allQuads.get(qi);
                    float[][] uvs = allUVs.get(qi);

                    for (int c = 0; c < 4; c++) {
                        QuadSphere.Vertex v = verts.get(quad[c]);
                        GL11.glTexCoord2f(uvs[c][0], uvs[c][1]);
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
}
