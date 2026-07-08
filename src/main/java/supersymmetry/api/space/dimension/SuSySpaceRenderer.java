package supersymmetry.api.space.dimension;

import static supersymmetry.client.shaders.util.ShaderUtils.invertMat4;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.IRenderHandler;

import org.lwjgl.opengl.*;

import supersymmetry.api.SusyLog;
import supersymmetry.api.image.Cubemap;
import supersymmetry.api.space.CelestialObjects;
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

    public float sunAngularRadius = 0.00935f;
    public float[] sunColor = { 1.0f, 0.95f, 0.8f };
    public float diskIntensity = 20.0f;
    public float limbDarkening = 0.85f;

    private boolean loggedOnce = false;

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
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        if (sunObject != null && ShaderManager.shadersAllowed()) {
            renderSunShader(sunDir, viewMat, projMat, time);
        }

        if (mainPlanet != null && mainCubemap != null) {
            renderMainPlanetoidHemisphere(worldTime, sunDir, viewMat, projMat);
        }

        GlStateManager.pushMatrix();
        GL11.glScalef(100.0f, 100.0f, 100.0f);
        for (RenderableCelestialObject obj : objects) {
            if (obj.getCelestialObject() == CelestialObjects.EARTH) continue;
            if (obj.getCelestialObject() == CelestialObjects.SUN) continue;
            if (obj == sunObject) continue;

            if (obj.getCelestialObject() == CelestialObjects.MOON && ShaderManager.shadersAllowed()) {
                if (obj.ensureLoaded()) {
                    float[] moonDir = obj.getWorldDirection(worldTime);
                    float moonScale = 100.0f * (float) Math.tan(Math.toRadians(4.0 / 2.0));

                    float[] moonRot = buildTidalLockRotation(moonDir);

                    int[] moonFaces = new int[6];

                    for (int i = 0; i < 6; i++) moonFaces[i] = obj.getCubemap().getFaceTexId(i);
                    planetSurfaceRenderer.render(
                            viewMat, projMat, sunDir,
                            new float[] { moonDir[0] * 100f, moonDir[1] * 100f, moonDir[2] * 100f },
                            moonScale, moonRot, moonFaces);
                }
            }
        }
        GlStateManager.popMatrix();

        if (mainPlanet != null && ShaderManager.shadersAllowed()) {
            float scale = 2500.0f;
            float planetY = -scale * 1.02f;
            atmosphereRenderer.render(viewMat, projMat, sunDir, planetY, scale);
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

    private void renderSunShader(float[] sunDir, float[] viewMat, float[] projMat, float time) {
        if (!ShaderManager.shadersAllowed()) return;

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
        ShaderUtils.setUniform3f(progId, "u_sunDir", sunDir[0], sunDir[1], sunDir[2]);
        ShaderUtils.setUniform1f(progId, "u_angularRadius", sunAngularRadius);
        ShaderUtils.setUniform3f(progId, "u_sunColor", sunColor[0], sunColor[1], sunColor[2]);
        ShaderUtils.setUniform1f(progId, "u_diskIntensity", diskIntensity);
        ShaderUtils.setUniform1f(progId, "u_time", time);
        ShaderUtils.setUniform1f(progId, "u_limbDarkening", limbDarkening);
        ShaderUtils.setUniformMat4(progId, "u_invView", invertMat4(viewMat));
        ShaderUtils.setUniformMat4(progId, "u_invProjection", invertMat4(projMat));
        float[] sunScreenPos = ShaderUtils.projectDirToNDC(sunDir, viewMat, projMat);
        ShaderUtils.setUniform2f(progId, "u_sunScreenPos", sunScreenPos[0], sunScreenPos[1]);

        ShaderUtils.drawFullScreenQuad();
        GL20.glUseProgram(0);
        GL11.glPopAttrib();
    }

    private void renderMainPlanetoidHemisphere(long worldTime, float[] sunDir, float[] viewMat, float[] projMat) {
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

        float savedSunR = planetSurfaceRenderer.sunAngularRadius;
        planetSurfaceRenderer.sunAngularRadius = 0.0f;

        planetSurfaceRenderer.render(
                viewMat, projMat, sunDir,
                new float[] { 0f, planetY, 0f },
                scale, rot, faceTexIds);

        planetSurfaceRenderer.sunAngularRadius = savedSunR;
    }

    private static float[] buildTidalLockRotation(float[] moonDir) {
        float tx = -moonDir[0], ty = -moonDir[1], tz = -moonDir[2];
        float len = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
        if (len < 1e-6f) return new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
        tx /= len;
        ty /= len;
        tz /= len;

        float ax = ty, ay = -tx, az = 0f;
        float sinA = (float) Math.sqrt(ax * ax + ay * ay);
        float cosA = -tz;

        if (sinA < 1e-6f) {
            if (cosA > 0) return new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
            else return new float[] { 1, 0, 0, 0, 0, -1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1 };
        }

        float nx = ax / sinA, ny = ay / sinA;
        float c = cosA, s = sinA, mc = 1f - c;
        return new float[] {
                c + nx * nx * mc, ny * nx * mc, -ny * s, 0f,
                nx * ny * mc, c + ny * ny * mc, nx * s, 0f,
                ny * s, -nx * s, c, 0f,
                0f, 0f, 0f, 1f
        };
    }
}
