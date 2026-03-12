package supersymmetry.api.space.dimension;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.IRenderHandler;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import supersymmetry.api.SusyLog;
import supersymmetry.api.image.Cubemap;
import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.space.QuadSphere;
import supersymmetry.api.space.RenderableCelestialObject;

public class SuSySpaceRenderer extends IRenderHandler {

    private RenderableCelestialObject[] objects = new RenderableCelestialObject[0];
    private RenderableCelestialObject earthObject = null;
    private Cubemap earthCubemap = null;
    private long earthOrbitalPeriodTicks = 110_400L;

    private final QuadSphere mesh = new QuadSphere(32);
    private int terminatorTexId = -1;
    private boolean loggedOnce = false;

    public SuSySpaceRenderer setCelestialObjects(RenderableCelestialObject... objs) {
        this.objects = (objs != null) ? objs : new RenderableCelestialObject[0];
        return this;
    }

    public SuSySpaceRenderer setOrbitalBody(RenderableCelestialObject earth, Cubemap cubemap, long orbitalPeriodTicks) {
        this.earthObject = earth;
        this.earthCubemap = cubemap;
        this.earthOrbitalPeriodTicks = orbitalPeriodTicks;
        return this;
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        if (!loggedOnce) {
            SusyLog.logger.info("[Space] SuSySpaceRenderer.render() called, objects=" + objects.length);
            loggedOnce = true;
        }

        long worldTime = world.getWorldTime();

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GlStateManager.disableFog();
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_FRONT);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // Read camera forward BEFORE any matrix push so the scale doesn't corrupt it.
        // For a pure rotation matrix, inverse = transpose, so forward = column 2 = [2, 6, 10].
        FloatBuffer mvBuf = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, mvBuf);
        float camFwdX = mvBuf.get(2);
        float camFwdY = mvBuf.get(6);
        float camFwdZ = mvBuf.get(10);

        GlStateManager.pushMatrix();
        GL11.glScalef(100.0f, 100.0f, 100.0f);

        // Depth-sort: objects furthest from camera direction draw first so
        // closer objects (e.g. Moon) correctly overdraw farther ones (e.g. Sun).
        List<RenderableCelestialObject> sorted = new ArrayList<>();
        for (RenderableCelestialObject obj : objects) {
            if (obj.getCelestialObject() != CelestialObjects.EARTH) sorted.add(obj);
        }
        sorted.sort((a, b) -> {
            float[] da = a.getWorldDirection(worldTime);
            float[] db = b.getWorldDirection(worldTime);
            float dotA = da[0] * camFwdX + da[1] * camFwdY + da[2] * camFwdZ;
            float dotB = db[0] * camFwdX + db[1] * camFwdY + db[2] * camFwdZ;
            return Float.compare(dotA, dotB); // most-negative dot = furthest behind camera = draw first
        });
        for (RenderableCelestialObject obj : sorted) {
            obj.renderAtPosition(worldTime, mesh);
        }

        GlStateManager.popMatrix(); // closes glScalef(100)

        // Earth hemisphere — drawn after sky objects so it composites on top
        if (earthObject != null && earthCubemap != null) {
            renderEarthHemisphere(worldTime);
        }

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.enableFog();
        GL11.glPopAttrib();
    }

    private void renderEarthHemisphere(long worldTime) {
        if (!earthCubemap.isLoaded()) {
            try {
                earthCubemap.loadAll();
            } catch (Exception e) {
                SusyLog.logger.error("[Space] Failed to load Earth cubemap", e);
                return;
            }
        }

        double orbitAngle = (worldTime % earthOrbitalPeriodTicks) /
                (double) earthOrbitalPeriodTicks * 2.0 * Math.PI;
        float orbitFaceDeg = (float) Math.toDegrees(orbitAngle);

        // Earth's own axial spin — one full rotation per 24000 ticks.
        // This only rotates the texture; vertex normals rotate with it, so
        // the terminator dot product is invariant to earthSelfRotation.
        float earthSelfRotation = (float) (worldTime % 24000L) / 24000.0f * 360.0f;

        // World-space sun direction (orbits on the XZ plane)
        double sunAngle = (worldTime % 24000L) / 24000.0 * 2.0 * Math.PI;
        float sunX = (float) Math.cos(sunAngle);
        float sunZ = (float) Math.sin(sunAngle);

        // Bring sun direction into Earth-local space by undoing the orbitFaceDeg
        // Y-rotation. earthSelfRotation cancels out (normals rotate with mesh).
        float orbitRad = (float) Math.toRadians(-orbitFaceDeg);
        float cosOrbit = (float) Math.cos(orbitRad);
        float sinOrbit = (float) Math.sin(orbitRad);
        float localSunX = sunX * cosOrbit - sunZ * sinOrbit;
        float localSunZ = sunX * sinOrbit + sunZ * cosOrbit;

        List<QuadSphere.Vertex> verts = mesh.getVertices();
        List<int[]> quads = mesh.getQuads();
        List<float[][]> uvs = mesh.getQuadUVs();
        List<List<Integer>> faceQuads = mesh.getFaceQuadIndices();

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GlStateManager.disableFog();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK); // player is above/outside the hemisphere, cull back faces

        GlStateManager.pushMatrix();
        GL11.glScalef(2500.0f, 2500.0f, 2500.0f);
        GL11.glTranslatef(0f, -1.02f, 0f);
        GL11.glRotatef(90.0f, 1f, 0f, 0f);           // tilt: poles sideways, orbit over equator
        GL11.glRotatef(orbitFaceDeg, 0f, 1f, 0f);    // which longitude is beneath the player
        GL11.glRotatef(earthSelfRotation, 0f, 0f, 1f); // Earth's own axial spin

        // ---- Pass 1: texture ----
        GlStateManager.enableTexture2D();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        for (int face = 0; face < 6; face++) {
            int texId = earthCubemap.getFaceTexId(face);
            if (texId == -1) continue;
            GlStateManager.bindTexture(texId);
            GL11.glBegin(GL11.GL_QUADS);
            for (int qi : faceQuads.get(face)) {
                int[] quad = quads.get(qi);
                float[][] quadUV = uvs.get(qi);
                for (int c = 0; c < 4; c++) {
                    QuadSphere.Vertex v = verts.get(quad[c]);
                    GL11.glTexCoord2f(quadUV[c][0], quadUV[c][1]);
                    GL11.glVertex3f(v.x, v.y, v.z);
                }
            }
            GL11.glEnd();
        }

        // ---- Pass 2: terminator shadow overlay ----
        // localSunX/Z are in Earth-local space so the dot against mesh normals is correct.
        if (terminatorTexId == -1) terminatorTexId = buildTerminatorTexture();

        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(terminatorTexId);
        GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        GL11.glBegin(GL11.GL_QUADS);
        for (int face = 0; face < 6; face++) {
            for (int qi : faceQuads.get(face)) {
                int[] quad = quads.get(qi);
                for (int c = 0; c < 4; c++) {
                    QuadSphere.Vertex v = verts.get(quad[c]);
                    float dot = v.nx * localSunX + v.nz * localSunZ;
                    // remap dot [-0.1, 0.1] -> U [0, 1]
                    float u = 1.0f - (dot + 0.1f) / 0.2f;
                    u = Math.max(0f, Math.min(1f, u));
                    GL11.glTexCoord2f(u, 0.5f);
                    GL11.glVertex3f(v.x, v.y, v.z);
                }
            }
        }
        GL11.glEnd();

        GlStateManager.popMatrix();
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glPopAttrib();
    }

    private int buildTerminatorTexture() {
        int width = 512;
        byte[] data = new byte[width * 4];
        for (int i = 0; i < width; i++) {
            float t = i / (float) (width - 1);
            float dot = (t * 2f - 1f) * 0.1f;
            float light = smoothstep(-0.02f, 0.02f, dot);
            byte b = (byte) (light * 255);
            data[i * 4] = b;
            data[i * 4 + 1] = b;
            data[i * 4 + 2] = b;
            data[i * 4 + 3] = (byte) 0xFF;
        }
        int id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        java.nio.ByteBuffer buf = BufferUtils.createByteBuffer(data.length);
        buf.put(data).flip();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, 1, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        return id;
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = Math.max(0f, Math.min(1f, (x - edge0) / (edge1 - edge0)));
        return t * t * (3f - 2f * t);
    }
}
