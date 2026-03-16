package supersymmetry.api.space.dimension;

import static supersymmetry.client.shaders.util.ShaderUtils.invertMat4;

import java.nio.FloatBuffer;

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
import supersymmetry.client.shaders.space.planet.PlanetSurfaceRenderer;
import supersymmetry.client.shaders.util.ShaderUtils;

public class SuSySpaceRenderer extends IRenderHandler {

    private RenderableCelestialObject[] objects = new RenderableCelestialObject[0];
    private RenderableCelestialObject mainPlanet = null;
    private RenderableCelestialObject sunObject = null;
    private Cubemap mainCubemap = null;
    private long mainPlanetoidOrbitalPeriodTicks = 110_400L;

    private final AtmosphereRenderer atmosphereRenderer = new AtmosphereRenderer();
    private final PlanetSurfaceRenderer planetSurfaceRenderer = new PlanetSurfaceRenderer();

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
    private float[] capturedView = new float[16];
    private float[] capturedProj = new float[16];

    private float[] currentSunDir = new float[] { 0f, 1f, 0f };

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

        gameTime += partialTicks / 20f;
        long worldTime = world.getWorldTime();

        currentSunDir = (sunObject != null) ? sunObject.getWorldDirection(worldTime) : new float[] { 0f, 1f, 0f };

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
            renderMainPlanetoidHemisphere(worldTime);
        }

        GlStateManager.pushMatrix();
        GL11.glScalef(100.0f, 100.0f, 100.0f);
        for (RenderableCelestialObject obj : objects) {
            if (obj.getCelestialObject() == CelestialObjects.EARTH) continue;
            if (obj.getCelestialObject() == CelestialObjects.SUN) continue;
            if (obj == sunObject) continue;

            if (obj.getCelestialObject() == CelestialObjects.MOON && ShaderManager.shadersAllowed()) {
                // Moon uses PlanetSurfaceRenderer with terminator
                if (obj.ensureLoaded()) {
                    float[] moonDir = obj.getWorldDirection(worldTime);
                    float moonScale = 100.0f * (float) Math.tan(Math.toRadians(4.0 / 2.0)); // angularSize/2

                    // Moon rotation: tidally locked - same face always toward Earth (origin)
                    // The -Z face points toward Earth (origin from moon position)
                    float[] moonRot = buildTidalLockRotation(moonDir);

                    int[] moonFaces = new int[6];

                    for (int i = 0; i < 6; i++) moonFaces[i] = obj.getCubemap().getFaceTexId(i);
                    float[] sd = currentSunDir;
                    planetSurfaceRenderer.render(
                            capturedView, capturedProj, sd,
                            new float[] { moonDir[0] * 100f, moonDir[1] * 100f, moonDir[2] * 100f },
                            moonScale, moonRot, moonFaces);
                }
            } else {
                obj.renderAtPosition(worldTime, mesh);
            }
        }
        GlStateManager.popMatrix();

        if (mainPlanet != null && ShaderManager.shadersAllowed()) {
            float scale = 2500.0f;
            float planetY = -scale * 1.02f;
            float[] sd = currentSunDir;
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

        float[] sd = currentSunDir;

        GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);

        int progId = ShaderManager.getRawProgram("sun.vert", "sun.frag");
        if (progId <= 0) return;

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
        ShaderUtils.setUniform3f(progId, "u_sunDir", sd[0], sd[1], sd[2]);
        ShaderUtils.setUniform1f(progId, "u_angularRadius", sunAngularRadius);
        ShaderUtils.setUniform3f(progId, "u_sunColor", sunColor[0], sunColor[1], sunColor[2]);
        ShaderUtils.setUniform1f(progId, "u_diskIntensity", diskIntensity);
        ShaderUtils.setUniform1f(progId, "u_coronaScale", coronaScale);
        ShaderUtils.setUniform1f(progId, "u_time", gameTime);
        ShaderUtils.setUniform1f(progId, "u_limbDarkening", limbDarkening);
        ShaderUtils.setUniformMat4(progId, "u_invView", invertMat4(capturedView));
        ShaderUtils.setUniformMat4(progId, "u_invProjection", invertMat4(capturedProj));
        float[] sunScreenPos = ShaderUtils.projectDirToNDC(sd, capturedView, capturedProj);
        ShaderUtils.setUniform2f(progId, "u_sunScreenPos", sunScreenPos[0], sunScreenPos[1]);

        drawFullScreenQuad();
        GL20.glUseProgram(0);
        GL11.glPopAttrib();
    }

    private void renderMainPlanetoidHemisphere(long worldTime) {
        if (!mainCubemap.isLoaded()) {
            try {
                mainCubemap.loadAll();
            } catch (Exception e) {
                SusyLog.logger.error("[Space] Failed to load Earth cubemap", e);
                return;
            }
        }

        double orbitAngle = (worldTime % mainPlanetoidOrbitalPeriodTicks) /
                (double) mainPlanetoidOrbitalPeriodTicks * 2.0 * Math.PI;

        // Matches old GL calls: glRotatef(90,1,0,0) then glRotatef(orbitDeg,0,1,0)
        // Ry(orbit) * Rx(90) in column-major:
        // col0=(co,0,-so,0) col1=(so,0,co,0) col2=(0,-1,0,0) col3=(0,0,0,1)
        float co = (float) Math.cos(orbitAngle);
        float so = (float) Math.sin(orbitAngle);
        float[] rot = {
                co, 0f, -so, 0f,
                so, 0f, co, 0f,
                0f, -1f, 0f, 0f,
                0f, 0f, 0f, 1f
        };

        int[] faceTexIds = new int[6];
        for (int i = 0; i < 6; i++) faceTexIds[i] = mainCubemap.getFaceTexId(i);

        float scale = 2500.0f;
        float planetY = -scale * 1.02f;
        float[] sd = (sunObject != null) ? sunObject.getWorldDirection(worldTime) : new float[] { 0f, 1f, 0f };

        // Earth has atmosphere - disable terminator in surface shader,
        // the atmosphere pass handles night-side darkening
        float savedSunR = planetSurfaceRenderer.sunAngularRadius;
        planetSurfaceRenderer.sunAngularRadius = 0.0f;

        planetSurfaceRenderer.render(
                capturedView, capturedProj, sd,
                new float[] { 0f, planetY, 0f },
                scale, rot, faceTexIds);

        planetSurfaceRenderer.sunAngularRadius = savedSunR;
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

    private float[] getMatrix(int glEnum) {
        matBuf.clear();
        GL11.glGetFloat(glEnum, matBuf);
        float[] m = new float[16];
        matBuf.get(m);
        return m;
    }

    private static float[] buildTidalLockRotation(float[] moonDir) {
        // We want the cubemap's -Z face to point toward -moonDir (back toward Earth).
        // Rotate (0,0,-1) to point toward (-moonDir[0], -moonDir[1], -moonDir[2]).
        float tx = -moonDir[0], ty = -moonDir[1], tz = -moonDir[2];
        float len = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
        if (len < 1e-6f) return new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
        tx /= len;
        ty /= len;
        tz /= len;

        // Rodrigues rotation: (0,0,-1) → (tx,ty,tz)
        // axis = (0,0,-1) x (tx,ty,tz) = (-ty*(-1)-tz*0, tz*0-(-1)*tx-... )
        // cross((0,0,-1),(tx,ty,tz)) = (0*tz-(-1)*ty, (-1)*tx-0*tz, 0*ty-0*tx) = (ty, -tx, 0)
        float ax = ty, ay = -tx, az = 0f;
        float sinA = (float) Math.sqrt(ax * ax + ay * ay);
        float cosA = -tz; // dot((0,0,-1),(tx,ty,tz))

        if (sinA < 1e-6f) {
            if (cosA > 0) return new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
            else return new float[] { 1, 0, 0, 0, 0, -1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1 };
        }

        // Rodrigues: R = I*cos + (1-cos)*nnT + sin*[n]x where n = axis/|axis|
        float nx = ax / sinA, ny = ay / sinA;
        float c = cosA, s = sinA, mc = 1f - c;
        return new float[] {
                c + nx * nx * mc, ny * nx * mc + 0 * s, 0 * nx * mc - ny * s, 0f,  // col 0
                nx * ny * mc - 0 * s, c + ny * ny * mc, 0 * ny * mc + nx * s, 0f,  // col 1
                0 * nx * mc + ny * s, 0 * ny * mc - nx * s, c, 0f,  // col 2
                0f, 0f, 0f, 1f   // col 3
        };
    }
}
