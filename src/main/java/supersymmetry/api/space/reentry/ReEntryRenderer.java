package supersymmetry.api.space.reentry;

import static supersymmetry.client.shaders.util.ShaderUtils.invertMat4;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.IRenderHandler;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import supersymmetry.api.SusyLog;
import supersymmetry.api.image.Cubemap;
import supersymmetry.api.space.RenderableCelestialObject;
import supersymmetry.client.shaders.ShaderManager;
import supersymmetry.client.shaders.space.atmosphere.AtmosphereRenderer;
import supersymmetry.client.shaders.space.planet.PlanetSurfaceRenderer;
import supersymmetry.client.shaders.util.ShaderUtils;

public class ReEntryRenderer extends IRenderHandler {

    private RenderableCelestialObject sunObject = null;
    private RenderableCelestialObject earthObject = null;
    private Cubemap earthCubemap = null;
    private long earthOrbitalPeriodTicks = 110_400L;

    public float sunAngularRadius = 0.00935f;
    public float[] sunColor = { 1.0f, 0.95f, 0.8f };
    public float diskIntensity = 20.0f;
    public float limbDarkening = 0.85f;

    public volatile float orbitProgress = 0f;
    public volatile boolean reEntryStarted = false;
    public volatile float descentProgress = 0f;
    public volatile float plasmaIntensity = 0f;

    private final AtmosphereRenderer atmosphereRenderer = new AtmosphereRenderer();
    private final PlanetSurfaceRenderer planetRenderer = new PlanetSurfaceRenderer();

    private boolean loggedOnce = false;

    public volatile float podRotationT = 0f;

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

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        if (!loggedOnce) {
            SusyLog.logger.info("[ReEntry] ReEntryRenderer.render() called");
            loggedOnce = true;
        }

        ShaderManager.ensureInitialised();

        long worldTime = world.getWorldTime();
        float time = worldTime / 20f;

        float[] sunDir = (sunObject != null) ? sunObject.getWorldDirection(worldTime) : new float[] { 0f, 1f, 0f };

        float[] viewMat = ShaderUtils.getMatrix(GL11.GL_MODELVIEW_MATRIX);
        float[] projMat = ShaderUtils.getMatrix(GL11.GL_PROJECTION_MATRIX);

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

        if (sunObject != null && ShaderManager.shadersAllowed()) {
            renderSunShader(sunDir, viewMat, projMat, time);
        }

        float tiltDeg = (1.0f - podRotationT) * 90.0f;
        GlStateManager.pushMatrix();
        GL11.glRotatef(tiltDeg, 0f, 0f, 1f);

        if (earthObject != null && earthCubemap != null) {
            renderEarthHemisphere(worldTime, sunDir, viewMat, projMat);
        }

        if (earthObject != null && ShaderManager.shadersAllowed()) {
            float scale = computeEarthRenderScale();
            float planetY = -scale * 1.02f;
            atmosphereRenderer.render(viewMat, projMat, sunDir, planetY, scale);
        }

        GlStateManager.popMatrix();

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.enableFog();
        GL11.glPopAttrib();
    }

    private float computeEarthRenderScale() {
        float orbitScale = 2500f;
        float descentScale = 25000f;
        return orbitScale + (descentScale - orbitScale) * descentProgress;
    }

    private void renderEarthHemisphere(long worldTime, float[] sunDir, float[] viewMat, float[] projMat) {
        if (!earthCubemap.isLoaded()) {
            try {
                earthCubemap.loadAll();
            } catch (Exception e) {
                SusyLog.logger.error("[ReEntry] Failed to load Earth cubemap", e);
                return;
            }
        }

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

        float approachT = reEntryStarted ? Math.min(1.0f, podRotationT * 0.2f + descentProgress * 0.8f) : 0f;
        float scale = 2500f + (25000f - 2500f) * approachT;
        float planetY = -scale * 1.02f;

        float savedSunR = planetRenderer.sunAngularRadius;
        planetRenderer.sunAngularRadius = 0.0f;
        planetRenderer.render(viewMat, projMat, sunDir,
                new float[] { 0f, planetY, 0f }, scale, rot, faceTexIds);
        planetRenderer.sunAngularRadius = savedSunR;
    }

    private void renderSunShader(float[] sunDir, float[] viewMat, float[] projMat, float time) {
        if (!ShaderManager.shadersAllowed()) return;

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
        ShaderUtils.setUniform3f(progId, "u_sunDir", sunDir[0], sunDir[1], sunDir[2]);
        ShaderUtils.setUniform1f(progId, "u_angularRadius", sunAngularRadius);
        ShaderUtils.setUniform3f(progId, "u_sunColor", sunColor[0], sunColor[1], sunColor[2]);
        ShaderUtils.setUniform1f(progId, "u_diskIntensity", diskIntensity);
        ShaderUtils.setUniform1f(progId, "u_time", time);
        ShaderUtils.setUniform1f(progId, "u_limbDarkening", limbDarkening);
        ShaderUtils.setUniformMat4(progId, "u_invView", invertMat4(viewMat));
        ShaderUtils.setUniformMat4(progId, "u_invProjection", invertMat4(projMat));
        float[] sunScreen = ShaderUtils.projectDirToNDC(sunDir, viewMat, projMat);
        ShaderUtils.setUniform2f(progId, "u_sunScreenPos", sunScreen[0], sunScreen[1]);

        ShaderUtils.drawFullScreenQuad();
        GL20.glUseProgram(0);
        GL11.glPopAttrib();
    }
}
