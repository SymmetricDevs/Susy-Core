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

    public float threshold = 0.6f;
    public float knee = 0.1f;
    public float blurRadius = 4.0f;
    public float intensity = 1.0f;

    public float[] emissiveColor = { 1.0f, 1.0f, 1.0f };
    public float emissivePower = 1.5f;
    public float fresnelPower = 2.0f;
    public float pulseSpeed = 1.0f;
    public float pulseAmp = 0.2f;

    private Framebuffer emissiveFBO;
    private Framebuffer maskFBO;
    private Framebuffer horizFBO;
    private Framebuffer vertFBO;

    private BloomRenderer() {}

    public void beginEmissivePass() {
        if (!ShaderManager.shadersAllowed())
            return;

        Minecraft mc = Minecraft.getMinecraft();
        ensureFBOs(mc.displayWidth, mc.displayHeight);

        emissiveFBO.bindFramebuffer(true);
        emissiveFBO.framebufferClear();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
    }

    public void endEmissivePass() {
        if (!ShaderManager.shadersAllowed())
            return;

        GlStateManager.disableBlend();
        GlStateManager.enableLighting();

        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
    }

    public void renderBloom() {
        if (!ShaderManager.shadersAllowed()) return;
        if (ShaderManager.isOptiFineShaderPackLoaded()) return;

        Minecraft mc = Minecraft.getMinecraft();
        Framebuffer sceneFBO = mc.getFramebuffer();

        bindTextureToUnit(emissiveFBO.framebufferTexture, 0);
        ShaderManager.renderFullImageInFBO(maskFBO, ShaderManager.EMISSIVE_MASK_F, cache -> {
            cache.glUniform1F("u_threshold", threshold);
            cache.glUniform1F("u_knee", knee);
            cache.glUniform1I("u_texture", 0);
        });

        bindTextureToUnit(maskFBO.framebufferTexture, 0);
        ShaderManager.renderFullImageInFBO(horizFBO, ShaderManager.S_BLUR_F, cache -> {
            cache.glUniform2F("u_direction", 1.0f, 0.0f);
            cache.glUniform1F("u_radius", blurRadius);
            cache.glUniform1I("u_texture", 0);
        });

        bindTextureToUnit(horizFBO.framebufferTexture, 0);
        ShaderManager.renderFullImageInFBO(vertFBO, ShaderManager.S_BLUR_F, cache -> {
            cache.glUniform2F("u_direction", 0.0f, 1.0f);
            cache.glUniform1F("u_radius", blurRadius);
            cache.glUniform1I("u_texture", 0);
        });

        bindTextureToUnit(sceneFBO.framebufferTexture, 0);
        bindTextureToUnit(vertFBO.framebufferTexture, 1);
        ShaderManager.renderFullImageInFBO(sceneFBO, ShaderManager.COMPOSITE_F, cache -> {
            cache.glUniform1I("u_texture", 0);
            cache.glUniform1I("u_bloomTexture", 1);
            cache.glUniform1F("u_intensity", intensity);
        });
    }

    private void ensureFBOs(int w, int h) {
        if (emissiveFBO != null && emissiveFBO.framebufferWidth == w && emissiveFBO.framebufferHeight == h) {
            return;
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

    private static void bindTextureToUnit(int textureId, int unit) {
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit + unit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
