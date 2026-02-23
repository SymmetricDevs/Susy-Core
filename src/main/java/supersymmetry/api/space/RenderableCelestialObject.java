package supersymmetry.api.space;

import net.minecraft.client.renderer.GlStateManager;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import supersymmetry.api.SusyLog;
import supersymmetry.api.image.Cubemap;

public class RenderableCelestialObject {

    private final CelestialObject object;
    private final Cubemap cubemap;

    private float angularSizeDeg = 20.0f;
    private long orbitalPeriodTicks = 0L;
    private float orbitalInclinationDeg = 0.0f;
    private long phaseOffsetTicks = 0L;

    private int textureId = -1;
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

    public CelestialObject getCelestialObject() {
        return object;
    }

    private int getTextureId() {
        if (!loadAttempted) {
            loadAttempted = true;
            try {
                textureId = cubemap.load();
                SusyLog.logger.info("[Space] Cubemap loaded for " + object.getTranslationKey() + " texId=" + textureId);
            } catch (Exception e) {
                SusyLog.logger.error("[Space] Cubemap FAILED for " + object.getTranslationKey(), e);
            }
        }
        return textureId;
    }

    public void renderAtPosition(long worldTime, QuadSphere mesh) {
        // Compute orbital position
        double angle = 0.0;
        if (orbitalPeriodTicks > 0) {
            angle = ((worldTime + phaseOffsetTicks) % orbitalPeriodTicks) / (double) orbitalPeriodTicks * 2.0 * Math.PI;
        }
        float incRad = (float) Math.toRadians(orbitalInclinationDeg);
        float dx = (float) Math.cos(angle);
        float dy = (float) (Math.sin(angle) * Math.sin(incRad));
        float dz = (float) (Math.sin(angle) * Math.cos(incRad));

        // Apparent radius at unit distance from angular size
        float radius = (float) Math.tan(Math.toRadians(angularSizeDeg / 2.0));

        int tex = getTextureId();

        GlStateManager.pushMatrix();

        // Move to object's position on the celestial sphere
        GL11.glTranslatef(dx, dy, dz);
        // Scale to apparent size
        GL11.glScalef(radius, radius, radius);

        if (tex != -1) {
            // Bind cubemap and render the QuadSphere.
            // Each vertex position on the unit sphere IS the cubemap lookup vector.
            GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, tex);
            GL11.glEnable(GL13.GL_TEXTURE_CUBE_MAP);
            GlStateManager.disableTexture2D();

            GL11.glBegin(GL11.GL_QUADS);
            for (int[] quad : mesh.getQuads()) {
                for (int idx : quad) {
                    QuadSphere.Vertex v = mesh.getVertices().get(idx);
                    // Vertex position on unit sphere = cubemap direction vector
                    GL11.glTexCoord3f(v.x, v.y, v.z);
                    GL11.glVertex3f(v.x, v.y, v.z);
                }
            }
            GL11.glEnd();

            GL11.glDisable(GL13.GL_TEXTURE_CUBE_MAP);
            GlStateManager.enableTexture2D();
        } else {
            // Magenta fallback — confirms rendering works without textures
            GlStateManager.disableTexture2D();
            GL11.glColor4f(1.0f, 0.0f, 1.0f, 1.0f);
            GL11.glBegin(GL11.GL_QUADS);
            for (int[] quad : mesh.getQuads()) {
                for (int idx : quad) {
                    QuadSphere.Vertex v = mesh.getVertices().get(idx);
                    GL11.glVertex3f(v.x, v.y, v.z);
                }
            }
            GL11.glEnd();
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableTexture2D();
        }

        GlStateManager.popMatrix();
    }
}
