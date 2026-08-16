package supersymmetry.client.shaders.space.planet;

import static supersymmetry.client.shaders.util.ShaderUtils.invertMat4;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.*;

import supersymmetry.client.shaders.ShaderManager;
import supersymmetry.client.shaders.util.ShaderUtils;

public class PlanetSurfaceRenderer {

    public float sunAngularRadius = 0.00935f; // radians, Earth's sun

    public void render(float[] capturedView, float[] capturedProj,
                       float[] sunDir, float[] sunColor,
                       float[] planetPos,  // render units xyz
                       float planetRadius, // render units
                       float[] rotationMatrix, // column-major float[16]
                       int[] faceTexIds) {
        // 6 GL texture IDs: +X,-X,+Y,-Y,+Z,-Z

        if (!ShaderManager.shadersAllowed())
            return;
        int prog = ShaderManager.getRawProgram("planet_surface.vert", "planet_surface.frag");
        if (prog <= 0)
            return;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glColorMask(true, true, true, true);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glViewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        GL11.glDisable(GL11.GL_BLEND); // opaque surface layer

        // Bind 6 cubemap face textures on units 0-5
        for (int i = 0; i < 6; i++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, faceTexIds[i]);
        }
        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        GL20.glUseProgram(prog);

        // Face samplers
        for (int i = 0; i < 6; i++) {
            ShaderUtils.set1i(prog, "u_face" + i, i);
        }

        ShaderUtils.set3f(prog, "u_planetPos", planetPos);
        ShaderUtils.set1f(prog, "u_planetRadius", planetRadius);
        ShaderUtils.set3f(prog, "u_sunDir", sunDir);
        ShaderUtils.set3f(prog, "u_sunColor", sunColor);
        ShaderUtils.set1f(prog, "u_sunAngularRadius", sunAngularRadius);
        ShaderUtils.setMat4(prog, "u_invView", invertMat4(capturedView));
        ShaderUtils.setMat4(prog, "u_invProjection", invertMat4(capturedProj));
        ShaderUtils.setMat4(prog, "u_planetRotation", rotationMatrix);

        ShaderUtils.drawFullScreenQuad();

        GL20.glUseProgram(0);
        for (int i = 5; i >= 0; i--) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glPopAttrib();
    }
}
