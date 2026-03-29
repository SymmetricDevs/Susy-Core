package supersymmetry.common.world;

import static supersymmetry.client.shaders.util.ShaderUtils.invertMat4;

import java.nio.FloatBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import supersymmetry.api.space.QuadSphere;
import supersymmetry.api.space.RenderableCelestialObject;
import supersymmetry.client.shaders.ShaderManager;
import supersymmetry.client.shaders.space.atmosphere.AtmosphereRenderer;
import supersymmetry.client.shaders.space.planet.PlanetSurfaceRenderer;
import supersymmetry.client.shaders.util.ShaderUtils;
import supersymmetry.common.world.sky.SkyColorData;

public class SuSySkyRenderer extends IRenderHandler {

    private RenderableCelestialObject[] objects = new RenderableCelestialObject[0];
    private RenderableCelestialObject sunObject = null;

    private SkyColorData skyColorData = null;

    private final PlanetSurfaceRenderer planetSurfaceRenderer = new PlanetSurfaceRenderer();
    private final AtmosphereRenderer atmosphereRenderer = new AtmosphereRenderer();
    private final QuadSphere mesh = new QuadSphere(64);
    private int quadVbo = -1;

    public float sunAngularRadius = 0.00935f;
    public float[] sunColor = { 1.0f, 0.95f, 0.8f };
    public float diskIntensity = 20.0f;
    public float coronaScale = 6.0f;
    public float limbDarkening = 0.85f;

    private final FloatBuffer matBuf = BufferUtils.createFloatBuffer(16);
    private float[] capturedView = new float[16];
    private float[] capturedProj = new float[16];
    private float[] currentSunDir = { 0f, 1f, 0f };
    private float gameTime = 0f;

    public SuSySkyRenderer setCelestialObjects(RenderableCelestialObject... objs) {
        this.objects = (objs != null) ? objs : new RenderableCelestialObject[0];
        return this;
    }

    public SuSySkyRenderer setSunObject(RenderableCelestialObject sun) {
        this.sunObject = sun;
        return this;
    }

    public SuSySkyRenderer setSkyColorData(SkyColorData colorData) {
        this.skyColorData = colorData;
        return this;
    }

    public SkyColorData getSkyColorData() {
        return skyColorData;
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

        gameTime += partialTicks / 20f;
        long worldTime = world.getWorldTime();

        currentSunDir = (sunObject != null) ? sunObject.getWorldDirection(worldTime) : new float[] { 0f, 1f, 0f };

        capturedView = getMatrix(GL11.GL_MODELVIEW_MATRIX);
        capturedProj = getMatrix(GL11.GL_PROJECTION_MATRIX);

        if (skyColorData != null) {
            renderSkyBackground(world.getCelestialAngle(partialTicks));
        }
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
            renderSunShader();
        } else if (sunObject != null) {
            GlStateManager.pushMatrix();
            GL11.glScalef(100f, 100f, 100f);
            sunObject.renderAtPosition(worldTime, mesh);
            GlStateManager.popMatrix();
        }

        GlStateManager.pushMatrix();
        GL11.glScalef(100f, 100f, 100f);

        for (RenderableCelestialObject obj : objects) {
            if (obj == sunObject) continue;

            if (ShaderManager.shadersAllowed() && obj.ensureLoaded()) {
                float[] dir = obj.getWorldDirection(worldTime);
                float scale = 100f * (float) Math.tan(Math.toRadians(obj.getAngularSizeDeg() / 2.0));
                float[] rot = buildCubemapRotation(dir, currentSunDir);
                int[] faces = new int[6];
                for (int i = 0; i < 6; i++) faces[i] = obj.getCubemap().getFaceTexId(i);
                boolean hasAtmosphere = isEarthLike(obj);
                float savedSunR = planetSurfaceRenderer.sunAngularRadius;
                if (hasAtmosphere) planetSurfaceRenderer.sunAngularRadius = 0.0f;

                planetSurfaceRenderer.render(
                        capturedView, capturedProj, currentSunDir,
                        new float[] { dir[0] * 100f, dir[1] * 100f, dir[2] * 100f },
                        scale, rot, faces);

                if (hasAtmosphere) planetSurfaceRenderer.sunAngularRadius = savedSunR;
            } else {
                obj.renderAtPosition(worldTime, mesh);
            }
        }

        GlStateManager.popMatrix();

        for (RenderableCelestialObject obj : objects) {
            if (obj == sunObject) continue;
            if (!isEarthLike(obj)) continue;
            if (!ShaderManager.shadersAllowed()) continue;

            float[] dir = obj.getWorldDirection(worldTime);
            float scale = 100f * (float) Math.tan(Math.toRadians(obj.getAngularSizeDeg() / 2.0));
            float planetY = dir[1] * 100f * scale; // position along direction
            atmosphereRenderer.render(
                    capturedView, capturedProj, currentSunDir,
                    dir[1] * 100f, // planetY: the object's Y in sky space
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

    private static boolean isEarthLike(RenderableCelestialObject obj) {
        return obj.getCelestialObject() == supersymmetry.api.space.CelestialObjects.EARTH;
    }

    private static float[] buildCubemapRotation(float[] dir, float[] sunDir) {
        float len = (float) Math.sqrt(dir[0] * dir[0] + dir[1] * dir[1] + dir[2] * dir[2]);
        if (len < 1e-6f) return identity();
        float dx = dir[0] / len, dy = dir[1] / len, dz = dir[2] / len;

        float ux = 0, uy = 1, uz = 0;
        if (Math.abs(dy) > 0.99f) {
            ux = 0;
            uy = 0;
            uz = -1;
        }

        float rx = dy * uz - dz * uy;
        float ry = dz * ux - dx * uz;
        float rz = dx * uy - dy * ux;
        float rlen = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
        if (rlen < 1e-6f) return identity();
        rx /= rlen;
        ry /= rlen;
        rz /= rlen;

        float upx = ry * dz - rz * dy;
        float upy = rz * dx - rx * dz;
        float upz = rx * dy - ry * dx;

        return new float[] {
                rx, ry, rz, 0f,
                upx, upy, upz, 0f,
                dx, dy, dz, 0f,
                0f, 0f, 0f, 1f
        };
    }

    private void renderSkyBackground(float celestialAngle) {
        net.minecraft.util.math.Vec3d skyColor = skyColorData.getSkyColor(celestialAngle);

        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color((float) skyColor.x, (float) skyColor.y, (float) skyColor.z, 1.0f);

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

    private void renderSunShader() {
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
        ShaderUtils.setUniform3f(progId, "u_sunDir", currentSunDir[0], currentSunDir[1], currentSunDir[2]);
        ShaderUtils.setUniform1f(progId, "u_angularRadius", sunAngularRadius);
        ShaderUtils.setUniform3f(progId, "u_sunColor", sunColor[0], sunColor[1], sunColor[2]);
        ShaderUtils.setUniform1f(progId, "u_diskIntensity", diskIntensity);
        ShaderUtils.setUniform1f(progId, "u_coronaScale", coronaScale);
        ShaderUtils.setUniform1f(progId, "u_time", gameTime);
        ShaderUtils.setUniform1f(progId, "u_limbDarkening", limbDarkening);
        ShaderUtils.setUniformMat4(progId, "u_invView", invertMat4(capturedView));
        ShaderUtils.setUniformMat4(progId, "u_invProjection", invertMat4(capturedProj));

        float[] sunScreenPos = ShaderUtils.projectDirToNDC(currentSunDir, capturedView, capturedProj);
        ShaderUtils.setUniform2f(progId, "u_sunScreenPos", sunScreenPos[0], sunScreenPos[1]);

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

    private static float[] identity() {
        return new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
    }
}
