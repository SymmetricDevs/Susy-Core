package supersymmetry.api.space.reentry;

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
import supersymmetry.api.space.QuadSphere;
import supersymmetry.api.space.RenderableCelestialObject;
import supersymmetry.client.shaders.ShaderManager;
import supersymmetry.client.shaders.space.atmosphere.AtmosphereRenderer;
import supersymmetry.client.shaders.space.planet.PlanetSurfaceRenderer;
import supersymmetry.client.shaders.util.ShaderUtils;

/**
 * Sky renderer for the atmospheric re-entry corridor dimension.
 *
 * Scene phases (driven by ReEntryPhase supplied from the server via the
 * ReEntrySequenceHandler):
 *
 * ORBIT – Earth spins below, sun arcs overhead, pod in low orbit.
 * REENTRY_BURN – startReEntry() shader triggered; fire / plasma effects begin.
 * DESCENT – Earth fills more and more of the screen as altitude drops.
 * TRANSFER – dimension transfer handled by ReEntrySequenceHandler.
 */
public class ReEntryRenderer extends IRenderHandler {

    // ---- Wired by ReEntryDimension.createEarthReEntry() ----
    private RenderableCelestialObject[] extraObjects = new RenderableCelestialObject[0];
    private RenderableCelestialObject earthObject = null;
    private RenderableCelestialObject sunObject = null;
    private Cubemap earthCubemap = null;
    private long earthOrbitalPeriodTicks = 110_400L;

    // ---- Sun shader params (mirrors SuSySpaceRenderer) ----
    public float sunAngularRadius = 0.00935f;
    public float[] sunColor = { 1.0f, 0.95f, 0.8f };
    public float diskIntensity = 20.0f;
    public float coronaScale = 6.0f;
    public float limbDarkening = 0.85f;

    // ---- Re-entry state set each frame by ReEntrySequenceHandler ----
    /** 0 = start of orbit, 1 = atmosphere reached. */
    public volatile float orbitProgress = 0f;
    /** true once startReEntry() has been triggered. */
    public volatile boolean reEntryStarted = false;
    /** Normalised descent progress: 0 = orbit burn start, 1 = transfer altitude. */
    public volatile float descentProgress = 0f;
    /** Intensity of the plasma/glow shader overlay (0-1). */
    public volatile float plasmaIntensity = 0f;

    // ---- internals ----
    private final QuadSphere mesh = new QuadSphere(64);
    private final AtmosphereRenderer atmosphereRenderer = new AtmosphereRenderer();
    private final PlanetSurfaceRenderer planetRenderer = new PlanetSurfaceRenderer();

    private int quadVbo = -1;
    private boolean loggedOnce = false;
    private float gameTime = 0f;

    private final FloatBuffer matBuf = BufferUtils.createFloatBuffer(16);
    private float[] capturedView = new float[16];
    private float[] capturedProj = new float[16];
    private float[] currentSunDir = { 0f, 1f, 0f };

    public volatile float podRotationT = 0f;

    // ---- fluent setters ----

    public ReEntryRenderer setCelestialObjects(RenderableCelestialObject... objs) {
        this.extraObjects = (objs != null) ? objs : new RenderableCelestialObject[0];
        return this;
    }

    public ReEntryRenderer setSunObject(RenderableCelestialObject sun) {
        this.sunObject = sun;
        return this;
    }

    public ReEntryRenderer setEarthObject(RenderableCelestialObject earth, Cubemap cubemap, long orbitTicks) {
        this.earthObject = earth;
        this.earthCubemap = cubemap;
        this.earthOrbitalPeriodTicks = orbitTicks;
        return this;
    }

    // ---- IRenderHandler ----

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        if (!loggedOnce) {
            SusyLog.logger.info("[ReEntry] ReEntryRenderer.render() called");
            loggedOnce = true;
        }

        ShaderManager.ensureInitialised();

        gameTime += partialTicks / 20f;
        long worldTime = world.getWorldTime();

        currentSunDir = (sunObject != null) ? sunObject.getWorldDirection(worldTime) : new float[] { 0f, 1f, 0f };

        capturedView = getMatrix(GL11.GL_MODELVIEW_MATRIX);
        capturedProj = getMatrix(GL11.GL_PROJECTION_MATRIX);

        // Single attrib push — one matching pop at the end
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
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // --- Screen-space passes OUTSIDE the tilt matrix ---
        // Sun shader operates in NDC, must not be rotated
        if (sunObject != null && ShaderManager.shadersAllowed()) {
            renderSunShader(worldTime);
        } else if (sunObject != null) {
            GlStateManager.pushMatrix();
            GL11.glScalef(100f, 100f, 100f);
            sunObject.renderAtPosition(worldTime, mesh);
            GlStateManager.popMatrix();
        }

        // --- World-space passes INSIDE the tilt matrix ---
        // The tilt rolls the sky scene: 90° sideways during orbit → 0° during descent
        float tiltDeg = (1.0f - podRotationT) * 90.0f;
        GlStateManager.pushMatrix();
        GL11.glRotatef(tiltDeg, 0f, 0f, 1f);

        // Earth hemisphere
        if (earthObject != null && earthCubemap != null) {
            renderEarthHemisphere(worldTime);
        }

        // Moon and other bodies
        GlStateManager.pushMatrix();
        GL11.glScalef(100f, 100f, 100f);
        for (RenderableCelestialObject obj : extraObjects) {
            obj.renderAtPosition(worldTime, mesh);
        }
        GlStateManager.popMatrix();

        // Atmosphere glow — uses capturedView/capturedProj (pre-tilt), rendered in world space
        if (earthObject != null && ShaderManager.shadersAllowed()) {
            float scale = computeEarthRenderScale();
            float planetY = -scale * 1.02f;
            atmosphereRenderer.render(capturedView, capturedProj, currentSunDir, planetY, scale);
        }

        GlStateManager.popMatrix(); // end tilt

        // --- Plasma overlay: screen-space, outside tilt ---
        if (reEntryStarted && plasmaIntensity > 0f && ShaderManager.shadersAllowed()) {
            renderPlasmaOverlay();
        }

        // Restore GL state
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.enableFog();
        GL11.glPopAttrib(); // matches the single push at the top
    }

    // ---- private rendering helpers ----

    /**
     * Returns the render scale for Earth in game units.
     * During orbit it matches the LEO scale (~2500).
     * During descent it grows toward a very large value so Earth fills the screen.
     */
    private float computeEarthRenderScale() {
        // Orbit scale: planet just below, large but not overwhelming
        float orbitScale = 2500f;
        // At transfer altitude (~5 km) the planet fills ~170° of view → enormous scale
        float descentScale = 25000f;
        return orbitScale + (descentScale - orbitScale) * descentProgress;
    }

    private void renderEarthHemisphere(long worldTime) {
        if (!earthCubemap.isLoaded()) {
            try {
                earthCubemap.loadAll();
            } catch (Exception e) {
                SusyLog.logger.error("[ReEntry] Failed to load Earth cubemap", e);
                return;
            }
        }

        // Earth spin: full speed during orbit, slows slightly during reentry
        double spinMult = reEntryStarted ? Math.max(0.1, 1.0 - descentProgress * 0.3) : 1.0;
        double orbitAngle = ((worldTime % earthOrbitalPeriodTicks) /
                (double) earthOrbitalPeriodTicks) * 2.0 * Math.PI * spinMult;

        float co = (float) Math.cos(orbitAngle);
        float so = (float) Math.sin(orbitAngle);
        float[] rot = {
                co, 0f, -so, 0f,
                so, 0f, co, 0f,
                0f, -1f, 0f, 0f,
                0f, 0f, 0f, 1f
        };

        int[] faceTexIds = new int[6];
        for (int i = 0; i < 6; i++) faceTexIds[i] = earthCubemap.getFaceTexId(i);

        // Earth starts far (orbit) and grows to fill screen (5km)
        // podRotationT drives the "getting closer" feel during deorbit burn
        // descentProgress drives the rapid approach during descent
        float approachT = reEntryStarted ? Math.min(1.0f, podRotationT * 0.2f + descentProgress * 0.8f) : 0f;
        float scale = 2500f + (25000f - 2500f) * approachT;
        float planetY = -scale * 1.02f;

        float savedSunR = planetRenderer.sunAngularRadius;
        planetRenderer.sunAngularRadius = 0.0f;
        planetRenderer.render(capturedView, capturedProj, currentSunDir,
                new float[] { 0f, planetY, 0f }, scale, rot, faceTexIds);
        planetRenderer.sunAngularRadius = savedSunR;
    }

    private void renderSunShader(long worldTime) {
        if (!ShaderManager.shadersAllowed()) return;
        float[] sd = currentSunDir;

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

        int progId = ShaderManager.getRawProgram("sun.vert", "sun.frag");
        if (progId <= 0) {
            GL11.glPopAttrib();
            return;
        }

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
        float[] sunScreen = ShaderUtils.projectDirToNDC(sd, capturedView, capturedProj);
        ShaderUtils.setUniform2f(progId, "u_sunScreenPos", sunScreen[0], sunScreen[1]);

        drawFullScreenQuad();
        GL20.glUseProgram(0);
        GL11.glPopAttrib();
    }

    /**
     * Full-screen plasma/heat-shield glow overlay shown during re-entry burn.
     * Requires a "reentry_plasma.vert" / "reentry_plasma.frag" shader pair.
     */
    private void renderPlasmaOverlay() {
        int progId = ShaderManager.getRawProgram("reentry_plasma.vert", "reentry_plasma.frag");
        if (progId <= 0) return;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glViewport(0, 0,
                Minecraft.getMinecraft().displayWidth,
                Minecraft.getMinecraft().displayHeight);

        GL20.glUseProgram(progId);
        ShaderUtils.setUniform1f(progId, "u_intensity", plasmaIntensity);
        ShaderUtils.setUniform1f(progId, "u_time", gameTime);
        ShaderUtils.setUniform1f(progId, "u_descent", descentProgress);

        drawFullScreenQuad();
        GL20.glUseProgram(0);
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

    private float[] getMatrix(int glEnum) {
        matBuf.clear();
        GL11.glGetFloat(glEnum, matBuf);
        float[] m = new float[16];
        matBuf.get(m);
        return m;
    }
}
