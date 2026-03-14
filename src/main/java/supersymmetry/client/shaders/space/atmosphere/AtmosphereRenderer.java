package supersymmetry.client.shaders.space.atmosphere;

import java.nio.FloatBuffer;

import net.minecraft.client.Minecraft;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import supersymmetry.api.SusyLog;
import supersymmetry.client.shaders.ShaderManager;
import supersymmetry.client.shaders.util.ShaderUtils;

public class AtmosphereRenderer {

    public float[] rayleighCoefficients = { 5.802e-6f, 13.558e-6f, 33.1e-6f };
    public float rayleighScaleHeight = 8000f;
    public float[] mieCoefficients = { 3.996e-6f, 3.996e-6f, 3.996e-6f };
    public float mieScaleHeight = 1200f;
    public float[] mieAsymmetry = { 0.80f, 0.80f, 0.80f };
    public float mieAbsorptionMult = 1.11f;
    public float[] ozoneCoefficients = { 0.650e-6f, 1.881e-6f, 0.085e-6f };
    public float ozoneAltitude = 25000f;
    public float ozoneExtent = 15000f;
    public float bottomRadius = 6.371e6f;
    public float topRadius = 6.471e6f;
    public float sunIntensity = 20.0f;

    private static final int LUT_SIZE = 64;

    private int quadVbo = -1;
    private int lutFbo = -1;
    private int lutTex = -1;
    private boolean lutDirty = true;
    private boolean loggedOnce = false;

    private final FloatBuffer fb16 = BufferUtils.createFloatBuffer(16);

    public void markDirty() {
        lutDirty = true;
    }

    public void render(float[] capturedView, float[] capturedProj,
                       float[] sunDir, float planetY, float scale) {
        if (!ShaderManager.shadersAllowed()) {
            if (!loggedOnce) SusyLog.logger.warn("[Atmos] shadersAllowed=false, skipping");
            loggedOnce = true;
            return;
        }

        ensureGLResources();

        if (lutDirty) bakeLut();

        int prog = ShaderManager.getRawProgram("atmosphere_orbital.vert",
                "atmosphere_orbital.frag");
        if (!loggedOnce) {
            SusyLog.logger.info("[Atmos] progId={} lutTex={} lutFbo={} sunDir=({},{},{})",
                    prog, lutTex, lutFbo, sunDir[0], sunDir[1], sunDir[2]);
            SusyLog.logger.info("[Atmos] planetY={} scale={} bottomRadius={} topRadius={}",
                    planetY, scale, bottomRadius, topRadius);
            loggedOnce = true;
        }
        if (prog <= 0) {
            SusyLog.logger.error("[Atmos] atmosphere shader prog <= 0, aborting");
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
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL13.glActiveTexture(GL13.GL_TEXTURE8);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, lutTex);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        GL20.glUseProgram(prog);

        set3f(prog, "u_rayleighCoefficients", rayleighCoefficients);
        set1f(prog, "u_rayleighScaleHeight", rayleighScaleHeight);
        set3f(prog, "u_mieCoefficients", mieCoefficients);
        set1f(prog, "u_mieScaleHeight", mieScaleHeight);
        set3f(prog, "u_mieAsymmetry", mieAsymmetry);
        set1f(prog, "u_mieAbsorptionMult", mieAbsorptionMult);
        set3f(prog, "u_ozoneCoefficients", ozoneCoefficients);
        set1f(prog, "u_ozoneAltitude", ozoneAltitude);
        set1f(prog, "u_ozoneExtent", ozoneExtent);
        set1f(prog, "u_bottomRadius", bottomRadius);
        set1f(prog, "u_topRadius", topRadius);
        set3f(prog, "u_cameraPos", new float[] { 0f, 0f, 0f });
        set3f(prog, "u_planetPos", new float[] { 0f, planetY, 0f });
        set3f(prog, "u_sunDir", sunDir);
        set1f(prog, "u_sunIntensity", sunIntensity);
        set1f(prog, "u_renderUnitRadius", scale);
        setMat4(prog, "u_invProjection", ShaderUtils.invertMat4(capturedProj));
        setMat4(prog, "u_invView", ShaderUtils.invertMat4(capturedView));
        set1i(prog, "u_transmittanceLut", 8);
        set2f(prog, "u_lutSize", LUT_SIZE, LUT_SIZE);

        drawQuad();

        GL20.glUseProgram(0);
        GL13.glActiveTexture(GL13.GL_TEXTURE8);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glPopAttrib();
    }

    private void bakeLut() {
        int prog = ShaderManager.getRawProgram("transmittance_lut.vert",
                "transmittance_lut.frag");
        SusyLog.logger.info("[Atmos] bakeLut progId={}", prog);
        if (prog <= 0) {
            SusyLog.logger.error("[Atmos] transmittance_lut shader failed to compile");
            return;
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        int prevFbo = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
        int prevProg = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);

        EXTFramebufferObject.glBindFramebufferEXT(
                EXTFramebufferObject.GL_FRAMEBUFFER_EXT, lutFbo);

        // Verify FBO completeness
        int status = EXTFramebufferObject.glCheckFramebufferStatusEXT(
                EXTFramebufferObject.GL_FRAMEBUFFER_EXT);
        if (status != EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT) {
            SusyLog.logger.error("[Atmos] LUT FBO incomplete, status=0x{}", Integer.toHexString(status));
            EXTFramebufferObject.glBindFramebufferEXT(
                    EXTFramebufferObject.GL_FRAMEBUFFER_EXT, prevFbo);
            GL11.glPopAttrib();
            return;
        }

        GL11.glViewport(0, 0, LUT_SIZE, LUT_SIZE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColorMask(true, true, true, true);

        GL20.glUseProgram(prog);
        set2f(prog, "u_lutSize", LUT_SIZE, LUT_SIZE);
        set1f(prog, "u_bottomRadius", bottomRadius);
        set1f(prog, "u_topRadius", topRadius);
        set3f(prog, "u_rayleighCoefficients", rayleighCoefficients);
        set1f(prog, "u_rayleighScaleHeight", rayleighScaleHeight);
        set3f(prog, "u_mieCoefficients", mieCoefficients);
        set1f(prog, "u_mieScaleHeight", mieScaleHeight);
        set1f(prog, "u_mieAbsorptionMult", mieAbsorptionMult);
        set3f(prog, "u_ozoneCoefficients", ozoneCoefficients);
        set1f(prog, "u_ozoneAltitude", ozoneAltitude);
        set1f(prog, "u_ozoneExtent", ozoneExtent);

        drawQuad();

        GL20.glUseProgram(prevProg);
        EXTFramebufferObject.glBindFramebufferEXT(
                EXTFramebufferObject.GL_FRAMEBUFFER_EXT, prevFbo);
        GL11.glPopAttrib();

        SusyLog.logger.info("[Atmos] bakeLut complete");
        lutDirty = false;
    }

    private void ensureGLResources() {
        if (lutTex != -1) return;

        FloatBuffer v = BufferUtils.createFloatBuffer(8);
        v.put(new float[] { -1, -1, 1, -1, -1, 1, 1, 1 }).flip();
        quadVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, quadVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, v, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        lutTex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, lutTex);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGB16F,
                LUT_SIZE, LUT_SIZE, 0, GL11.GL_RGB, GL11.GL_FLOAT,
                (java.nio.ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        lutFbo = EXTFramebufferObject.glGenFramebuffersEXT();
        EXTFramebufferObject.glBindFramebufferEXT(
                EXTFramebufferObject.GL_FRAMEBUFFER_EXT, lutFbo);
        EXTFramebufferObject.glFramebufferTexture2DEXT(
                EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT,
                GL11.GL_TEXTURE_2D, lutTex, 0);
        EXTFramebufferObject.glBindFramebufferEXT(
                EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);

        SusyLog.logger.info("[Atmos] GL resources created: quadVbo={} lutTex={} lutFbo={}",
                quadVbo, lutTex, lutFbo);
    }

    private void drawQuad() {
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, quadVbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 8, 0);
        GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
        GL20.glDisableVertexAttribArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private static void set1f(int p, String n, float v) {
        int l = GL20.glGetUniformLocation(p, n);
        if (l >= 0) GL20.glUniform1f(l, v);
    }

    private static void set1i(int p, String n, int v) {
        int l = GL20.glGetUniformLocation(p, n);
        if (l >= 0) GL20.glUniform1i(l, v);
    }

    private static void set2f(int p, String n, float x, float y) {
        int l = GL20.glGetUniformLocation(p, n);
        if (l >= 0) GL20.glUniform2f(l, x, y);
    }

    private static void set3f(int p, String n, float[] v) {
        int l = GL20.glGetUniformLocation(p, n);
        if (l >= 0) GL20.glUniform3f(l, v[0], v[1], v[2]);
    }

    private void setMat4(int p, String n, float[] m) {
        int l = GL20.glGetUniformLocation(p, n);
        if (l < 0) return;
        fb16.clear();
        fb16.put(m).flip();
        GL20.glUniformMatrix4(l, false, fb16);
    }
}
