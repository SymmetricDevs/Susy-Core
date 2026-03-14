package supersymmetry.api.space.dimension;

import java.nio.FloatBuffer;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.IRenderHandler;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import supersymmetry.api.SusyLog;
import supersymmetry.api.image.Cubemap;
import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.space.QuadSphere;
import supersymmetry.api.space.RenderableCelestialObject;
import supersymmetry.client.shaders.ShaderManager;
import supersymmetry.client.shaders.space.atmosphere.AtmosphereRenderer;
import supersymmetry.client.shaders.util.ShaderUtils;

public class SuSySpaceRenderer extends IRenderHandler {

    private RenderableCelestialObject[] objects = new RenderableCelestialObject[0];
    private RenderableCelestialObject mainPlanet = null;
    private RenderableCelestialObject sunObject = null;
    private Cubemap mainCubemap = null;
    private long mainPlanetoidOrbitalPeriodTicks = 110_400L;
    private final AtmosphereRenderer atmosphereRenderer = new AtmosphereRenderer();

    // ── geometry ─────────────────────────────────────────────────────────────
    private final QuadSphere mesh = new QuadSphere(64);

    private int quadVbo = -1; // lazily created, reused every frame

    public float sunAngularRadius = 0.00935f;
    public float[] sunColor = { 1.0f, 0.95f, 0.8f };
    public float diskIntensity = 20.0f;
    public float coronaScale = 6.0f;
    public float limbDarkening = 0.85f;

    private boolean loggedOnce = false;
    private float gameTime = 0f;

    private final FloatBuffer matBuf = BufferUtils.createFloatBuffer(16);
    // Captured once per frame BEFORE any GL matrix manipulation
    private float[] capturedView = new float[16];
    private float[] capturedProj = new float[16];

    public SuSySpaceRenderer setCelestialObjects(RenderableCelestialObject... objs) {
        this.objects = (objs != null) ? objs : new RenderableCelestialObject[0];
        return this;
    }

    public SuSySpaceRenderer setSunObject(RenderableCelestialObject sun) {
        this.sunObject = sun;
        return this;
    }

    public SuSySpaceRenderer setOrbitalBody(RenderableCelestialObject mainPlanetoid, Cubemap cubemap,
                                            long orbitalPeriodTicks) {
        this.mainPlanet = mainPlanetoid;
        this.mainCubemap = cubemap;
        this.mainPlanetoidOrbitalPeriodTicks = orbitalPeriodTicks;
        return this;
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        if (!loggedOnce) {
            SusyLog.logger.info("[Space] SuSySpaceRenderer.render() called, objects=" + objects.length);
            loggedOnce = true;
        }

        ShaderManager.ensureInitialised();

        gameTime += partialTicks / 20f; // ticks → seconds for shader uniforms

        long worldTime = world.getWorldTime();

        capturedView = getMatrix(GL11.GL_MODELVIEW_MATRIX);
        capturedProj = getMatrix(GL11.GL_PROJECTION_MATRIX);

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

        if (sunObject != null && ShaderManager.shadersAllowed()) {
            renderSunShader(worldTime);
        } else if (sunObject != null) {
            GlStateManager.pushMatrix();
            GL11.glScalef(100.0f, 100.0f, 100.0f);
            sunObject.renderAtPosition(worldTime, mesh);
            GlStateManager.popMatrix();
        }

        if (mainPlanet != null && mainCubemap != null) {
            rendermainPlanetoidHemisphere(worldTime);
        }

        GlStateManager.pushMatrix();
        GL11.glScalef(100.0f, 100.0f, 100.0f);

        for (RenderableCelestialObject obj : objects) {
            if (obj.getCelestialObject() == CelestialObjects.EARTH) continue;
            if (obj == sunObject) continue;
            obj.renderAtPosition(worldTime, mesh);
        }

        GlStateManager.popMatrix();

        if (mainPlanet != null) {
            float scale = 2500.0f;
            float planetY = -scale * 1.02f;
            float[] sd = (sunObject != null) ? sunObject.getWorldDirection(worldTime) : new float[] { 0f, 1f, 0f };

            atmosphereRenderer.render(capturedView, capturedProj, sd, planetY, scale);
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

    private void renderSunShader(long worldTime) {
        if (!ShaderManager.shadersAllowed()) return;

        float[] sd = sunObject.getWorldDirection(worldTime);

        if (!loggedOnce) {
            SusyLog.logger.info("[Sun] sunDir=({},{},{}) angularRadius={} diskIntensity={} coronaScale={}",
                    sd[0], sd[1], sd[2], sunAngularRadius, diskIntensity, coronaScale);
            SusyLog.logger.info("[Sun] view[0]={} view[5]={} view[10]={} view[15]={}",
                    capturedView[0], capturedView[5], capturedView[10], capturedView[15]);
            SusyLog.logger.info("[Sun] proj[0]={} proj[5]={} proj[10]={} proj[15]={}",
                    capturedProj[0], capturedProj[5], capturedProj[10], capturedProj[15]);
            float len = (float) Math.sqrt(sd[0] * sd[0] + sd[1] * sd[1] + sd[2] * sd[2]);
            SusyLog.logger.info("[Sun] sunDir length={}", len);
        }

        GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);

        int progId = ShaderManager.getRawProgram("sun.vert", "sun.frag");
        if (!loggedOnce) {
            SusyLog.logger.info("[Sun] progId={} sunDir=({},{},{}) len={} angR={} intensity={}",
                    progId, sd[0], sd[1], sd[2],
                    Math.sqrt(sd[0] * sd[0] + sd[1] * sd[1] + sd[2] * sd[2]),
                    sunAngularRadius, diskIntensity);
            SusyLog.logger.info("[Sun] view diag=({},{},{},{}) proj diag=({},{},{},{})",
                    capturedView[0], capturedView[5], capturedView[10], capturedView[15],
                    capturedProj[0], capturedProj[5], capturedProj[10], capturedProj[15]);
        }
        if (progId <= 0) {
            if (!loggedOnce) SusyLog.logger.error("[Sun] progId <= 0, shader link failed");
            return;
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glColorMask(true, true, true, true);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glViewport(0, 0,
                Minecraft.getMinecraft().displayWidth,
                Minecraft.getMinecraft().displayHeight);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);

        GL20.glUseProgram(progId);
        setUniform3f(progId, "u_sunDir", sd[0], sd[1], sd[2]);
        setUniform1f(progId, "u_angularRadius", sunAngularRadius);
        setUniform3f(progId, "u_sunColor", sunColor[0], sunColor[1], sunColor[2]);
        setUniform1f(progId, "u_diskIntensity", diskIntensity);
        setUniform1f(progId, "u_coronaScale", coronaScale);
        setUniform1f(progId, "u_time", gameTime);
        setUniform1f(progId, "u_limbDarkening", limbDarkening);
        setUniformMat4(progId, "u_invView", ShaderUtils.invertMat4(capturedView));
        setUniformMat4(progId, "u_invProjection", ShaderUtils.invertMat4(capturedProj));
        drawFullScreenQuad();
        GL20.glUseProgram(0);
        GL11.glPopAttrib();
    }

    private void rendermainPlanetoidHemisphere(long worldTime) {
        if (!mainCubemap.isLoaded()) {
            try {
                mainCubemap.loadAll();
            } catch (Exception e) {
                SusyLog.logger.error("[Space] Failed to load mainPlanetoid cubemap", e);
                return;
            }
        }

        double orbitAngle = (worldTime % mainPlanetoidOrbitalPeriodTicks) /
                (double) mainPlanetoidOrbitalPeriodTicks * 2.0 * Math.PI;

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
        GL11.glCullFace(GL11.GL_FRONT);

        GlStateManager.pushMatrix();
        GL11.glScalef(2500.0f, 2500.0f, 2500.0f);
        GL11.glTranslatef(0f, -1.02f, 0f);
        GL11.glRotatef(90.0f, 1f, 0f, 0f);
        GL11.glRotatef((float) Math.toDegrees(orbitAngle), 0f, 1f, 0f);

        GlStateManager.enableTexture2D();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        for (int face = 0; face < 6; face++) {
            int texId = mainCubemap.getFaceTexId(face);
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

        GlStateManager.popMatrix();
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glPopAttrib();
    }

    private void drawFullScreenQuad() {
        if (quadVbo == -1) {
            FloatBuffer verts = BufferUtils.createFloatBuffer(8);
            verts.put(new float[] { -1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f }).flip();
            quadVbo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, quadVbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verts, GL15.GL_STATIC_DRAW);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        }

        GL30.glBindVertexArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, quadVbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 8, 0);

        GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);

        GL20.glDisableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    /** Reads the current GL matrix into a float[16] array. */
    private float[] getMatrix(int glEnum) {
        matBuf.clear();
        GL11.glGetFloat(glEnum, matBuf);
        float[] m = new float[16];
        matBuf.get(m);
        return m;
    }

    private static void setUniform1f(int prog, String name, float v) {
        int loc = GL20.glGetUniformLocation(prog, name);
        if (loc >= 0) GL20.glUniform1f(loc, v);
    }

    private static void setUniform3f(int prog, String name, float x, float y, float z) {
        int loc = GL20.glGetUniformLocation(prog, name);
        if (loc >= 0) GL20.glUniform3f(loc, x, y, z);
    }

    private static void setUniformMat4(int prog, String name, float[] m) {
        int loc = GL20.glGetUniformLocation(prog, name);
        if (loc < 0) return;
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        buf.put(m).flip();
        GL20.glUniformMatrix4(loc, false, buf);
    }
}
