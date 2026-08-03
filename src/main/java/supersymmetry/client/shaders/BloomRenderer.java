package supersymmetry.client.shaders;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class BloomRenderer {

    public static final BloomRenderer INSTANCE = new BloomRenderer();

    /** Luminance threshold above which pixels contribute to bloom [0, 1]. */
    public float threshold = 0.6f;
    /** Soft-knee width around the threshold – avoids a hard cut-off. */
    public float knee = 0.1f;
    /** Gaussian blur radius in pixels. */
    public float blurRadius = 4.0f;
    /** Final bloom intensity – values > 1 give an HDR over-exposure feel. */
    public float intensity = 1.0f;

    /** Base emission colour tint (RGB). */
    public float[] emissiveColor = { 1.0f, 1.0f, 1.0f };
    /** Brightness multiplier – push above 1 to guarantee bloom threshold hit. */
    public float emissivePower = 1.5f;
    /** Fresnel exponent (0 = off). */
    public float fresnelPower = 2.0f;
    /** Pulse speed in Hz (0 = off). */
    public float pulseSpeed = 1.0f;
    /** Pulse amplitude [0, 1]. */
    public float pulseAmp = 0.2f;

    private Framebuffer emissiveFBO;  // emissive mesh render target
    private Framebuffer maskFBO;      // after threshold pass
    private Framebuffer horizFBO;     // after horizontal blur
    private Framebuffer vertFBO;      // after vertical blur (= final bloom)

    private float gameTime = 0f;

    private BloomRenderer() {}

    /**
     * Call every tick/frame to keep the pulse uniform advancing.
     * 
     * @param partialTicks interpolation factor (0–1)
     */
    public void tick(float partialTicks) {
        gameTime += partialTicks / 20f; // convert ticks → seconds
    }

    /**
     * Bind the emissive FBO and set up GL state so that subsequent draw calls
     * render into it with the emissive mesh shader active.
     */
    public void beginEmissivePass() {
        if (!ShaderManager.shadersAllowed()) return;

        Minecraft mc = ShaderManager.getMC();
        ensureFBOs(mc.displayWidth, mc.displayHeight);

        emissiveFBO.bindFramebuffer(true);
        emissiveFBO.framebufferClear();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // additive
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
    }

    /**
     * Stop drawing into the emissive FBO and restore GL state.
     */
    public void endEmissivePass() {
        if (!ShaderManager.shadersAllowed()) return;

        GlStateManager.disableBlend();
        GlStateManager.enableLighting();

        // Restore the main Minecraft framebuffer
        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
    }

    /**
     * Runs all three bloom post-process passes and composites the result onto
     * the main Minecraft framebuffer. Call after {@link #endEmissivePass()}.
     */
    public void renderBloom() {
        if (!ShaderManager.shadersAllowed()) return;
        if (ShaderManager.isOptiFineShaderPackLoaded()) return; // OF has its own pipeline

        Minecraft mc = ShaderManager.getMC();
        Framebuffer sceneFBO = mc.getFramebuffer();

        // ── Pass A: threshold / emissive mask ───────────────────────────────
        bindTextureToUnit(emissiveFBO.framebufferTexture, 0);
        ShaderManager.renderFullImageInFBO(maskFBO, ShaderManager.EMISSIVE_MASK_F, cache -> {
            cache.glUniform1F("u_threshold", threshold);
            cache.glUniform1F("u_knee", knee);
            cache.glUniform1I("u_texture", 0);
        });

        // ── Pass B: horizontal Gaussian blur ────────────────────────────────
        bindTextureToUnit(maskFBO.framebufferTexture, 0);
        ShaderManager.renderFullImageInFBO(horizFBO, ShaderManager.S_BLUR_F, cache -> {
            cache.glUniform2F("u_direction", 1.0f, 0.0f);
            cache.glUniform1F("u_radius", blurRadius);
            cache.glUniform1I("u_texture", 0);
        });

        // ── Pass C: vertical Gaussian blur ──────────────────────────────────
        bindTextureToUnit(horizFBO.framebufferTexture, 0);
        ShaderManager.renderFullImageInFBO(vertFBO, ShaderManager.S_BLUR_F, cache -> {
            cache.glUniform2F("u_direction", 0.0f, 1.0f);
            cache.glUniform1F("u_radius", blurRadius);
            cache.glUniform1I("u_texture", 0);
        });

        // ── Pass D: composite bloom onto scene ──────────────────────────────
        bindTextureToUnit(sceneFBO.framebufferTexture, 0);
        bindTextureToUnit(vertFBO.framebufferTexture, 1);
        ShaderManager.renderFullImageInFBO(sceneFBO, ShaderManager.COMPOSITE_F, cache -> {
            cache.glUniform1I("u_texture", 0);
            cache.glUniform1I("u_bloomTexture", 1);
            cache.glUniform1F("u_intensity", intensity);
        });
    }

    // ── private helpers ──────────────────────────────────────────────────────

    /** Lazily creates (or recreates) FBOs when the window size changes. */
    private void ensureFBOs(int w, int h) {
        if (emissiveFBO != null && emissiveFBO.framebufferWidth == w && emissiveFBO.framebufferHeight == h) {
            return; // already the right size
        }
        disposeFBOs();
        emissiveFBO = new Framebuffer(w, h, true);
        maskFBO = new Framebuffer(w, h, false);
        horizFBO = new Framebuffer(w, h, false);
        vertFBO = new Framebuffer(w, h, false);
    }

    private void disposeFBOs() {
        if (emissiveFBO != null) {
            emissiveFBO.deleteFramebuffer();
            emissiveFBO = null;
        }
        if (maskFBO != null) {
            maskFBO.deleteFramebuffer();
            maskFBO = null;
        }
        if (horizFBO != null) {
            horizFBO.deleteFramebuffer();
            horizFBO = null;
        }
        if (vertFBO != null) {
            vertFBO.deleteFramebuffer();
            vertFBO = null;
        }
    }

    /**
     * Binds a raw GL texture ID to the given texture unit without disturbing
     * other state.
     */
    private static void bindTextureToUnit(int textureId, int unit) {
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit + unit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
