package supersymmetry.client.shaders.space;

import static supersymmetry.client.shaders.util.ShaderUtils.invertMat4;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import supersymmetry.api.space.BodyRenderData;
import supersymmetry.api.space.BodyRenderer;
import supersymmetry.api.space.Star;
import supersymmetry.client.shaders.ShaderManager;
import supersymmetry.client.shaders.util.ShaderUtils;

public class SunGlowRenderer implements BodyRenderer {

    public float[] sunColor;
    public float diskIntensity = 1.3f;
    public float limbDarkening = 1.0f;
    public float coronaScale = 0.8f;
    public float detailFadeStart = 3.0f;
    public float detailFadeEnd = 15.0f;

    @Override
    public void render(BodyRenderData data) {
        if (!ShaderManager.shadersAllowed()) return;

        int progId = ShaderManager.getRawProgram("sun.vert", "sun.frag");
        if (progId <= 0) return;

        float[] viewMat = data.viewMatrix;
        float[] projMat = data.projectionMatrix;
        if (viewMat == null || projMat == null) return;

        float[] sunColor = this.sunColor;
        if (sunColor == null) {
            if (!(data.source instanceof Star)) return;
            Vec3d starColor = ((Star) data.source).getColor();
            sunColor = new float[] { (float) starColor.x, (float) starColor.y, (float) starColor.z };
        }

        float[] sunDir = new float[] {
                (float) data.direction.x,
                (float) data.direction.y,
                (float) data.direction.z
        };

        float angularRadius = (float) (Math.toRadians(data.angularSizeDeg) / 2.0);
        if (angularRadius <= 1e-7f) angularRadius = 0.00465f;

        Minecraft mc = Minecraft.getMinecraft();
        float time = (float) (data.worldTime / 20f);
        float detail = MathHelper.clamp(
                ((float) Math.tan(angularRadius) * Math.abs(projMat[5]) * mc.displayHeight * 0.5f - detailFadeStart) /
                        (detailFadeEnd - detailFadeStart),
                0.0f, 1.0f);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glColorMask(true, true, true, true);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);

        GL20.glUseProgram(progId);
        ShaderUtils.setUniform3f(progId, "u_sunDir", sunDir[0], sunDir[1], sunDir[2]);
        ShaderUtils.setUniform1f(progId, "u_angularRadius", angularRadius);
        ShaderUtils.setUniform3f(progId, "u_sunColor", sunColor[0], sunColor[1], sunColor[2]);
        ShaderUtils.setUniform1f(progId, "u_diskIntensity", diskIntensity);
        ShaderUtils.setUniform1f(progId, "u_time", time);
        ShaderUtils.setUniform1f(progId, "u_limbDarkening", limbDarkening);
        ShaderUtils.setUniform1f(progId, "u_coronaScale", coronaScale);
        ShaderUtils.setUniform1f(progId, "u_detail", detail);
        ShaderUtils.setUniformMat4(progId, "u_invView", invertMat4(viewMat));
        ShaderUtils.setUniformMat4(progId, "u_invProjection", invertMat4(projMat));

        ShaderUtils.drawFullScreenQuad();
        GL20.glUseProgram(0);
        GL11.glPopAttrib();
    }
}
