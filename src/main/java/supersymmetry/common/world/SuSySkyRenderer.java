package supersymmetry.common.world;

import static supersymmetry.client.shaders.util.ShaderUtils.invertMat4;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.*;

import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.space.RenderableCelestialObject;
import supersymmetry.client.shaders.ShaderManager;
import supersymmetry.client.shaders.space.atmosphere.AtmosphereRenderer;
import supersymmetry.client.shaders.space.planet.PlanetSurfaceRenderer;
import supersymmetry.client.shaders.util.ShaderUtils;

public class SuSySkyRenderer extends IRenderHandler {

    private RenderableCelestialObject[] objects = new RenderableCelestialObject[0];
    private RenderableCelestialObject sunObject = null;

    private final PlanetSurfaceRenderer planetSurfaceRenderer = new PlanetSurfaceRenderer();
    private final AtmosphereRenderer atmosphereRenderer = new AtmosphereRenderer();

    public float sunAngularRadius = 0.00935f;
    public float[] sunColor = { 1.0f, 0.95f, 0.8f };
    public float diskIntensity = 20.0f;
    public float limbDarkening = 0.85f;

    public SuSySkyRenderer setCelestialObjects(RenderableCelestialObject... objs) {
        this.objects = (objs != null) ? objs : new RenderableCelestialObject[0];
        return this;
    }

    public SuSySkyRenderer setSunObject(RenderableCelestialObject sun) {
        this.sunObject = sun;
        return this;
    }

    public RenderableCelestialObject getSunObject() {
        return sunObject;
    }

    public RenderableCelestialObject[] getObjects() {
        return objects;
    }

    public RenderableCelestialObject getPrimaryBody() {
        return (objects != null && objects.length > 0) ? objects[0] : null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        ShaderManager.ensureInitialised();

        long worldTime = world.getWorldTime();
        float time = worldTime / 20f;

        float[] sunDir = (sunObject != null) ? sunObject.getWorldDirection(worldTime) : new float[] { 0f, 1f, 0f };

        float[] viewMat = ShaderUtils.getMatrix(GL11.GL_MODELVIEW_MATRIX);
        float[] projMat = ShaderUtils.getMatrix(GL11.GL_PROJECTION_MATRIX);

        renderSkyBackground();

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

        GlStateManager.pushMatrix();
        GL11.glScalef(100f, 100f, 100f);

        for (RenderableCelestialObject obj : objects) {
            if (obj == sunObject) continue;

            if (ShaderManager.shadersAllowed() && obj.ensureLoaded()) {
                float[] dir = obj.getWorldDirection(worldTime);
                float scale = 100f * (float) Math.tan(Math.toRadians(obj.getAngularSizeDeg() / 2.0));
                float[] rot = buildCubemapRotation(dir);
                int[] faces = new int[6];
                for (int i = 0; i < 6; i++) faces[i] = obj.getCubemap().getFaceTexId(i);

                boolean hasAtmosphere = isEarthLike(obj);
                float savedSunR = planetSurfaceRenderer.sunAngularRadius;
                if (hasAtmosphere) planetSurfaceRenderer.sunAngularRadius = 0.0f;

                planetSurfaceRenderer.render(
                        viewMat, projMat, sunDir,
                        new float[] { dir[0] * 100f, dir[1] * 100f, dir[2] * 100f },
                        scale, rot, faces);

                if (hasAtmosphere) planetSurfaceRenderer.sunAngularRadius = savedSunR;
            }
        }

        GlStateManager.popMatrix();

        for (RenderableCelestialObject obj : objects) {
            if (obj == sunObject || !isEarthLike(obj)) continue;
            if (!ShaderManager.shadersAllowed()) continue;

            float[] dir = obj.getWorldDirection(worldTime);
            float scale = 100f * (float) Math.tan(Math.toRadians(obj.getAngularSizeDeg() / 2.0));
            atmosphereRenderer.render(
                    viewMat, projMat, sunDir,
                    dir[1] * 100f,
                    scale);
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

    private static float[] buildCubemapRotation(float[] dir) {
        float len = (float) Math.sqrt(dir[0] * dir[0] + dir[1] * dir[1] + dir[2] * dir[2]);
        if (len < 1e-6f) return new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
        float dx = dir[0] / len, dy = dir[1] / len, dz = dir[2] / len;

        float ux = 0f, uy = 1f, uz = 0f;
        if (Math.abs(dy) > 0.99f) {
            ux = 0f;
            uy = 0f;
            uz = -1f;
        }

        float rx = dy * uz - dz * uy;
        float ry = dz * ux - dx * uz;
        float rz = dx * uy - dy * ux;
        float rlen = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
        if (rlen < 1e-6f) return new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
        rx /= rlen;
        ry /= rlen;
        rz /= rlen;

        float upx = ry * dz - rz * dy;
        float upy = rz * dx - rx * dz;
        float upz = rx * dy - ry * dx;

        return new float[] {
                -rx, -ry, -rz, 0f,
                upx, upy, upz, 0f,
                dx, dy, dz, 0f,
                0f, 0f, 0f, 1f
        };
    }

    private static boolean isEarthLike(RenderableCelestialObject obj) {
        return obj.getCelestialObject() == CelestialObjects.EARTH;
    }

    private void renderSkyBackground() {
        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(0.0f, 0.0f, 0.0f, 1.0f);

        net.minecraft.client.renderer.Tessellator tess = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.BufferBuilder buf = tess.getBuffer();

        GlStateManager.depthMask(false);

        for (int i = 0; i < 6; i++) {
            GlStateManager.pushMatrix();
            if (i == 1) GlStateManager.rotate(90f, 1f, 0f, 0f);
            if (i == 2) GlStateManager.rotate(-90f, 1f, 0f, 0f);
            if (i == 3) GlStateManager.rotate(180f, 1f, 0f, 0f);
            if (i == 4) GlStateManager.rotate(90f, 0f, 0f, 1f);
            if (i == 5) GlStateManager.rotate(-90f, 0f, 0f, 1f);

            buf.begin(GL11.GL_QUADS,
                    net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION);
            buf.pos(-100, -100, -100).endVertex();
            buf.pos(-100, -100, 100).endVertex();
            buf.pos(100, -100, 100).endVertex();
            buf.pos(100, -100, -100).endVertex();
            tess.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
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
}
