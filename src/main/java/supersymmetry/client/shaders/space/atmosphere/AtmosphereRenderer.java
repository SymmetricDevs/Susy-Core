package supersymmetry.client.shaders.space.atmosphere;

import static supersymmetry.client.shaders.util.ShaderUtils.invertMat4;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.*;

import supersymmetry.api.SusyLog;
import supersymmetry.client.shaders.ShaderManager;
import supersymmetry.client.shaders.util.ShaderUtils;

public class AtmosphereRenderer {

    public float[] rayleighCoefficients = { 5.802e-6f, 13.558e-6f, 33.1e-6f };
    public float rayleighScaleHeight = 8000f;
    public float[] mieCoefficients = { 2.0e-5f, 2.0e-5f, 2.0e-5f };
    public float mieScaleHeight = 1200f;
    public float[] mieAsymmetry = { 0.80f, 0.80f, 0.80f };
    public float mieAbsorptionMult = 1.11f;
    public float[] ozoneCoefficients = { 0.650e-6f, 1.881e-6f, 0.085e-6f };
    public float ozoneAltitude = 25000f;
    public float ozoneExtent = 15000f;
    public float sunIntensity = 36.0f;
    public float bottomRadius = 6.371e6f;
    public float topRadius = 6.471e6f;

    private static final int LUT_SIZE = 64; // height
    private static final int LUT_SIZE_W = 256; // width - more precision for cosZenith
    private static final int MS_LUT_SIZE = 32; // multiple scattering LUT (square)

    private int lutFbo = -1;
    private int lutTex = -1;
    private int msLutTex = -1; // multiple scattering LUT
    private boolean lutDirty = true;
    private boolean loggedOnce = false;

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

        if (lutDirty) {
            bakeLut();
            bakeMultipleScatteringLut();
        }

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
        GL13.glActiveTexture(GL13.GL_TEXTURE9);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, msLutTex);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        GL20.glUseProgram(prog);

        ShaderUtils.set3f(prog, "u_rayleighCoefficients", rayleighCoefficients);
        ShaderUtils.set1f(prog, "u_rayleighScaleHeight", rayleighScaleHeight);
        ShaderUtils.set3f(prog, "u_mieCoefficients", mieCoefficients);
        ShaderUtils.set1f(prog, "u_mieScaleHeight", mieScaleHeight);
        ShaderUtils.set3f(prog, "u_mieAsymmetry", mieAsymmetry);
        ShaderUtils.set1f(prog, "u_mieAbsorptionMult", mieAbsorptionMult);
        ShaderUtils.set3f(prog, "u_ozoneCoefficients", ozoneCoefficients);
        ShaderUtils.set1f(prog, "u_ozoneAltitude", ozoneAltitude);
        ShaderUtils.set1f(prog, "u_ozoneExtent", ozoneExtent);
        ShaderUtils.set1f(prog, "u_bottomRadius", bottomRadius);
        ShaderUtils.set1f(prog, "u_topRadius", topRadius);
        ShaderUtils.set1f(prog, "u_sunIntensity", sunIntensity);
        ShaderUtils.set3f(prog, "u_cameraPos", new float[] { 0f, 0f, 0f });
        ShaderUtils.set3f(prog, "u_planetPos", new float[] { 0f, planetY, 0f });
        ShaderUtils.set3f(prog, "u_sunDir", sunDir);
        ShaderUtils.set1f(prog, "u_renderUnitRadius", scale);
        ShaderUtils.setMat4(prog, "u_invProjection", invertMat4(capturedProj));
        ShaderUtils.setMat4(prog, "u_invView", invertMat4(capturedView));
        ShaderUtils.set1i(prog, "u_transmittanceLut", 8);
        ShaderUtils.set2f(prog, "u_lutSize", LUT_SIZE_W, LUT_SIZE);
        ShaderUtils.set1i(prog, "u_multipleScatteringLut", 9);
        ShaderUtils.set2f(prog, "u_msLutSize", MS_LUT_SIZE, MS_LUT_SIZE);

        ShaderUtils.drawFullScreenQuad();

        GL20.glUseProgram(0);
        GL13.glActiveTexture(GL13.GL_TEXTURE8);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE9);
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
        // Always restore to 0 (default) - avoids LWJGL2 glGetInteger issues
        int prevFbo = 0;
        int prevProg = 0;

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lutFbo);

        // Verify FBO completeness
        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            SusyLog.logger.error("[Atmos] LUT FBO incomplete, status=0x{}", Integer.toHexString(status));
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo);
            GL11.glPopAttrib();
            return;
        }

        GL11.glViewport(0, 0, LUT_SIZE_W, LUT_SIZE);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColorMask(true, true, true, true);

        GL20.glUseProgram(prog);
        ShaderUtils.set2f(prog, "u_lutSize", LUT_SIZE_W, LUT_SIZE);
        ShaderUtils.set1f(prog, "u_bottomRadius", bottomRadius);
        ShaderUtils.set1f(prog, "u_topRadius", topRadius);
        ShaderUtils.set3f(prog, "u_rayleighCoefficients", rayleighCoefficients);
        ShaderUtils.set1f(prog, "u_rayleighScaleHeight", rayleighScaleHeight);
        ShaderUtils.set3f(prog, "u_mieCoefficients", mieCoefficients);
        ShaderUtils.set1f(prog, "u_mieScaleHeight", mieScaleHeight);
        ShaderUtils.set1f(prog, "u_mieAbsorptionMult", mieAbsorptionMult);
        ShaderUtils.set3f(prog, "u_ozoneCoefficients", ozoneCoefficients);
        ShaderUtils.set1f(prog, "u_ozoneAltitude", ozoneAltitude);
        ShaderUtils.set1f(prog, "u_ozoneExtent", ozoneExtent);

        ShaderUtils.drawFullScreenQuad();

        GL20.glUseProgram(prevProg);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo);
        GL11.glPopAttrib();

        SusyLog.logger.info("[Atmos] bakeLut complete");
        lutDirty = false;
    }

    private void bakeMultipleScatteringLut() {
        // Uses GL 4.3 compute shader - dispatched as MS_LUT_SIZE x MS_LUT_SIZE workgroups
        // Each workgroup (8x8 threads) computes one texel via parallel reduction
        int prog = ShaderManager.getRawComputeProgram("multiple_scattering_lut.comp");
        if (prog <= 0) {
            SusyLog.logger.error("[Atmos] multiple_scattering_lut compute shader failed");
            return;
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        int prevProg = 0; // restore to 0

        GL20.glUseProgram(prog);

        // Bind transmittance LUT for sampling
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, lutTex);
        ShaderUtils.set1i(prog, "u_transmittanceLut", 0);
        ShaderUtils.set2f(prog, "u_transmittanceLutSize", LUT_SIZE_W, LUT_SIZE);

        // Bind output image - unit 0, level 0, rgba16f
        GL42.glBindImageTexture(0, msLutTex, 0, false, 0, GL15.GL_WRITE_ONLY, GL30.GL_RGBA16F);
        ShaderUtils.set1i(prog, "u_multipleScatteringLut", 0);

        // Atmosphere uniforms
        ShaderUtils.set1f(prog, "u_bottomRadius", bottomRadius);
        ShaderUtils.set1f(prog, "u_topRadius", topRadius);
        ShaderUtils.set3f(prog, "u_rayleighCoefficients", rayleighCoefficients);
        ShaderUtils.set1f(prog, "u_rayleighScaleHeight", rayleighScaleHeight);
        ShaderUtils.set3f(prog, "u_mieCoefficients", mieCoefficients);
        ShaderUtils.set1f(prog, "u_mieScaleHeight", mieScaleHeight);
        ShaderUtils.set1f(prog, "u_mieAbsorptionMult", mieAbsorptionMult);
        ShaderUtils.set3f(prog, "u_ozoneCoefficients", ozoneCoefficients);
        ShaderUtils.set1f(prog, "u_ozoneAltitude", ozoneAltitude);
        ShaderUtils.set1f(prog, "u_ozoneExtent", ozoneExtent);

        // Dispatch: one workgroup per texel
        GL43.glDispatchCompute(MS_LUT_SIZE, MS_LUT_SIZE, 1);

        // Wait for compute to finish before sampling
        GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

        GL20.glUseProgram(prevProg);
        GL11.glPopAttrib();

        SusyLog.logger.info("[Atmos] bakeMultipleScatteringLut complete");
    }

    private void ensureGLResources() {
        if (lutTex != -1) return;

        lutTex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, lutTex);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGB16F,
                LUT_SIZE_W, LUT_SIZE, 0, GL11.GL_RGB, GL11.GL_FLOAT,
                (java.nio.ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        lutFbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lutFbo);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, lutTex, 0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        // Multiple scattering LUT texture (no FBO needed - uses image binding)
        msLutTex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, msLutTex);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F,
                MS_LUT_SIZE, MS_LUT_SIZE, 0, GL11.GL_RGBA, GL11.GL_FLOAT,
                (java.nio.ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        SusyLog.logger.info("[Atmos] GL resources created: lutTex={} lutFbo={} msLutTex={}",
                lutTex, lutFbo, msLutTex);
    }
}
