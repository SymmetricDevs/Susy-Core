package supersymmetry.client.shaders.space;

import static supersymmetry.client.shaders.util.ShaderUtils.invertMat4;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import supersymmetry.api.space.BodyRenderData;
import supersymmetry.api.space.BodyRenderer;
import supersymmetry.api.space.Star;
import supersymmetry.client.shaders.ShaderManager;
import supersymmetry.client.shaders.util.ShaderUtils;

public class SunGlowRenderer implements BodyRenderer {

    public float sunAngularRadius = 0.00935f;
    public float[] sunColor = { 1.0f, 0.95f, 0.8f };
    public float diskIntensity = 20.0f;
    public float limbDarkening = 0.85f;

    @Override
    public void render(BodyRenderData data) {
        if (!ShaderManager.shadersAllowed()) return;

        int progId = ShaderManager.getRawProgram("sun.vert", "sun.frag");
        if (progId <= 0) return;

        float[] viewMat = data.viewMatrix;
        float[] projMat = data.projectionMatrix;
        if (viewMat == null || projMat == null) return;

        Vec3d starColor = null;
        if (data.source instanceof Star) starColor = ((Star) data.source).getColor();
        float[] sunColor = starColor == null ? this.sunColor
                : new float[] { (float) starColor.x, (float) starColor.y, (float) starColor.z };

        float[] sunDir = new float[] {
                (float) data.direction.x,
                (float) data.direction.y,
                (float) data.direction.z
        };

        Minecraft mc = Minecraft.getMinecraft();
        float time = (float) (data.worldTime / 20f);

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
