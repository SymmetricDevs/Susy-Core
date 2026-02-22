package supersymmetry.api.space;

import net.minecraft.client.renderer.GlStateManager;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import supersymmetry.api.image.Cubemap;

public class RenderableCelestialObject {

    private final CelestialObject object;
    private final Cubemap cubemap;

    /**
     * Apparent angular diameter in degrees as seen from the observer dimension.
     * Earth from Moon surface ≈ 1.9°, Sun from Earth ≈ 0.53°, Moon from Earth ≈ 0.52°.
     */
    private float angularSizeDeg = 1.0f;

    /**
     * Orbital period in world ticks. Used to animate the object's position
     * around the observer over time. 0 = fixed/stationary (e.g. the body being orbited).
     */
    private long orbitalPeriodTicks = 0L;

    /**
     * Orbital inclination in degrees relative to the observer's equatorial plane.
     * 0 = equatorial orbit, 90 = polar orbit.
     */
    private float orbitalInclinationDeg = 0.0f;

    /**
     * Phase offset in ticks — shifts where in the orbit the object starts.
     */
    private long phaseOffsetTicks = 0L;

    private int textureId = -1;

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

    public int getTextureId() {
        if (textureId == -1) {
            try {
                textureId = cubemap.load();
            } catch (Exception e) {
                e.printStackTrace();
                textureId = -1;
            }
        }
        return textureId;
    }

    /**
     * Renders this object at the correct position on the celestial sphere for the given world time.
     *
     * <p>
     * Each object is placed at a direction determined by its orbital angle,
     * then rendered as a billboard quad at its apparent angular size.
     * </p>
     *
     * @param worldTime current world time in ticks
     * @param mesh      shared quad sphere mesh (used only when renderAsSphere is desired)
     */
    public void renderAtPosition(long worldTime, QuadSphere mesh) {
        int tex = getTextureId();
        if (tex == -1) return;

        // Compute orbital angle [0, 2π)
        double orbitalAngleRad;
        if (orbitalPeriodTicks <= 0) {
            orbitalAngleRad = 0.0; // stationary — the body being orbited, render at zenith/nadir
        } else {
            long adjustedTime = (worldTime + phaseOffsetTicks) % orbitalPeriodTicks;
            orbitalAngleRad = (adjustedTime / (double) orbitalPeriodTicks) * 2.0 * Math.PI;
        }

        // Direction vector on the celestial sphere
        // Orbit in the XZ plane, then tilt by inclination around X axis
        float incRad = (float) Math.toRadians(orbitalInclinationDeg);
        float dx = (float) Math.cos(orbitalAngleRad);
        float dy = (float) (Math.sin(orbitalAngleRad) * Math.sin(incRad));
        float dz = (float) (Math.sin(orbitalAngleRad) * Math.cos(incRad));

        // Half-size of the billboard in "sky units" from angular size
        // tan(θ/2) gives the half-size at unit distance
        float halfSize = (float) Math.tan(Math.toRadians(angularSizeDeg / 2.0));

        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, tex);
        GlStateManager.pushMatrix();

        // Translate to the object's direction on the unit sphere
        GL11.glTranslatef(dx, dy, dz);

        // Build a billboard: we need two axes perpendicular to the view direction (dx,dy,dz)
        // Use world up (0,1,0) to derive right, then recompute up
        float[] right = normalize(cross(new float[] { dx, dy, dz }, new float[] { 0, 1, 0 }));
        if (right[0] == 0 && right[1] == 0 && right[2] == 0) {
            // Object is at zenith/nadir — use X as right
            right = new float[] { 1, 0, 0 };
        }
        float[] up = normalize(cross(right, new float[] { dx, dy, dz }));

        float rx = right[0] * halfSize, ry = right[1] * halfSize, rz = right[2] * halfSize;
        float ux = up[0] * halfSize, uy = up[1] * halfSize, uz = up[2] * halfSize;

        // Render a single quad facing the observer
        GL11.glBegin(GL11.GL_QUADS);
        // Map cubemap normals: use the direction to the object as the cubemap lookup direction
        // All four corners share the same base normal; small angular extent means negligible error
        GL11.glNormal3f(-dx, -dy, -dz);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex3f(-rx - ux, -ry - uy, -rz - uz);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex3f(rx - ux, ry - uy, rz - uz);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex3f(rx + ux, ry + uy, rz + uz);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex3f(-rx + ux, -ry + uy, -rz + uz);
        GL11.glEnd();

        GlStateManager.popMatrix();
    }

    /**
     * Legacy full-sphere render (kept for compatibility / background starfield use).
     */
    public void render(double scale, QuadSphere mesh) {
        int tex = getTextureId();
        if (tex == -1) return;

        GL11.glPushMatrix();
        GL11.glScaled(scale, scale, scale);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, tex);

        GL11.glBegin(GL11.GL_QUADS);
        for (int[] quad : mesh.getQuads()) {
            for (int idx : quad) {
                QuadSphere.Vertex v = mesh.getVertices().get(idx);
                GL11.glNormal3f(v.nx, v.ny, v.nz);
                GL11.glVertex3f(v.x, v.y, v.z);
            }
        }
        GL11.glEnd();

        GL11.glPopMatrix();
    }

    // ------------------------------------------------------------------
    // Math helpers
    // ------------------------------------------------------------------

    private static float[] cross(float[] a, float[] b) {
        return new float[] {
                a[1] * b[2] - a[2] * b[1],
                a[2] * b[0] - a[0] * b[2],
                a[0] * b[1] - a[1] * b[0]
        };
    }

    private static float[] normalize(float[] v) {
        float len = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (len < 1e-6f) return new float[] { 0, 0, 0 };
        return new float[] { v[0] / len, v[1] / len, v[2] / len };
    }
}
