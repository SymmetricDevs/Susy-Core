package supersymmetry.client.shaders.space.planet;

import static supersymmetry.client.shaders.util.ShaderUtils.invertMat4;

import java.nio.FloatBuffer;

import net.minecraft.client.Minecraft;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import supersymmetry.client.shaders.ShaderManager;

/**
 * Renders a planet/moon surface cubemap with a physically correct terminator.
 * Used for bodies WITHOUT an atmosphere (moon, rocky planets).
 * Bodies WITH an atmosphere use the atmosphere shader which handles
 * terminator darkening internally.
 *
 * Usage in SuSySpaceRenderer:
 * private final PlanetSurfaceRenderer moonSurface = new PlanetSurfaceRenderer();
 *
 * // In render(), before atmosphere pass:
 * moonSurface.render(capturedView, capturedProj, sunDir,
 * planetPos, planetRadius, rotationMatrix,
 * faceTexIds); // int[6] of GL texture IDs
 */
public class PlanetSurfaceRenderer {

    public float sunAngularRadius = 0.00935f; // radians, Earth's sun

    private int quadVbo = -1;
    private final FloatBuffer fb16 = BufferUtils.createFloatBuffer(16);

    public void render(float[] capturedView, float[] capturedProj,
                       float[] sunDir,
                       float[] planetPos,  // render units xyz
                       float planetRadius, // render units
                       float[] rotationMatrix, // column-major float[16]
                       int[] faceTexIds) {
        // 6 GL texture IDs: +X,-X,+Y,-Y,+Z,-Z

        if (!ShaderManager.shadersAllowed()) return;
        int prog = ShaderManager.getRawProgram("planet_surface.vert", "planet_surface.frag");
        if (prog <= 0) return;

        ensureVbo();

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glColorMask(true, true, true, true);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glViewport(0, 0,
                Minecraft.getMinecraft().displayWidth,
                Minecraft.getMinecraft().displayHeight);
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
            set1i(prog, "u_face" + i, i);
        }

        set3f(prog, "u_planetPos", planetPos);
        set1f(prog, "u_planetRadius", planetRadius);
        set3f(prog, "u_sunDir", sunDir);
        set1f(prog, "u_sunAngularRadius", sunAngularRadius);
        setMat4(prog, "u_invView", invertMat4(capturedView));
        setMat4(prog, "u_invProjection", invertMat4(capturedProj));
        setMat4(prog, "u_planetRotation", rotationMatrix);

        drawQuad();

        GL20.glUseProgram(0);
        for (int i = 5; i >= 0; i--) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glPopAttrib();
    }

    private void ensureVbo() {
        if (quadVbo != -1) return;
        FloatBuffer v = BufferUtils.createFloatBuffer(8);
        v.put(new float[] { -1, -1, 1, -1, -1, 1, 1, 1 }).flip();
        quadVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, quadVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, v, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
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
