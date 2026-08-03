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

    private RenderableCelestialObject sunReference = null;
    private boolean tidallyLocked = false;

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

    public RenderableCelestialObject setSunReference(RenderableCelestialObject sun) {
        this.sunReference = sun;
        return this;
    }

    /**
     * Tidally locked: the same cubemap face always points toward the planet centre (origin).
     * The object still orbits normally - only the self-rotation is locked.
     * Use for the Moon.
     */
    public RenderableCelestialObject setTidallyLocked(boolean locked) {
        this.tidallyLocked = locked;
        return this;
    }

    public CelestialObject getCelestialObject() {
        return object;
    }

    public Cubemap getCubemap() {
        return cubemap;
    }

    public float getAngularSizeDeg() {
        return angularSizeDeg;
    }

    public boolean ensureLoaded() {
        return ensureLoadedInternal();
    }

    private boolean ensureLoadedInternal() {
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
        float dx, dy, dz;
        if (hasFixedDirection) {
            dx = fixedDx;
            dy = fixedDy;
            dz = fixedDz;
        } else {
            double angle = orbitalPeriodTicks > 0 ?
                    ((worldTime + phaseOffsetTicks) % orbitalPeriodTicks) /
                            (double) orbitalPeriodTicks * 2.0 * Math.PI :
                    0.0;
            float incRad = (float) Math.toRadians(orbitalInclinationDeg);
            dx = (float) Math.cos(angle);
            dy = (float) (Math.sin(angle) * Math.sin(incRad));
            dz = (float) (Math.sin(angle) * Math.cos(incRad));
        }

        float radius = (float) Math.tan(Math.toRadians(angularSizeDeg / 2.0));
        boolean hasTexture = ensureLoadedInternal();

        GlStateManager.pushMatrix();
        GL11.glTranslatef(dx, dy, dz);
        GL11.glScalef(radius, radius, radius);

        if (tidallyLocked) {
            // Tidal lock: rotate the sphere so its -Z face (the "near side") always
            // points toward the planet centre (origin). Since the object is at (dx,dy,dz),
            // the direction toward the planet is (-dx,-dy,-dz) normalised.
            float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 1e-6f) {
                float nx = -dx / len, ny = -dy / len, nz = -dz / len;
                // Rotate default -Z axis (0,0,-1) to point toward (nx,ny,nz)
                // Cross product: (0,0,-1) x (nx,ny,nz) = (0*nz-(-1)*ny, (-1)*nx-0*nz, 0*ny-0*nx)
                // = (ny, -nx, 0)
                float crossX = ny, crossY = -nx, crossZ = 0f;
                float sinA = (float) Math.sqrt(crossX * crossX + crossY * crossY);
                float cosA = -nz; // dot of (0,0,-1) with (nx,ny,nz)
                if (sinA > 1e-6f) {
                    float angleDeg = (float) Math.toDegrees(Math.atan2(sinA, cosA));
                    GL11.glRotatef(angleDeg, crossX / sinA, crossY / sinA, 0f);
                } else if (cosA < 0) {
                    // Already pointing away, rotate 180 around X
                    GL11.glRotatef(180f, 1f, 0f, 0f);
                }
            }
        } else if (sunReference != null) {
            // Rotate cubemap so +Z face points toward the sun (for lit-face alignment)
            float[] sunDir = sunReference.getWorldDirection(worldTime);
            float sx = sunDir[0] - dx;
            float sy = sunDir[1] - dy;
            float sz = sunDir[2] - dz;
            float len = (float) Math.sqrt(sx * sx + sy * sy + sz * sz);
            if (len > 1e-6f) {
                sx /= len;
                sy /= len;
                sz /= len;
            }
            float crossX = sy, crossY = -sx, crossZ = 0f;
            float sinA = (float) Math.sqrt(crossX * crossX + crossY * crossY);
            float cosA = sz;
            if (sinA > 1e-6f) {
                float angleDeg = (float) Math.toDegrees(Math.atan2(sinA, cosA));
                GL11.glRotatef(angleDeg, crossX / sinA, crossY / sinA, 0f);
            }
        }

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
