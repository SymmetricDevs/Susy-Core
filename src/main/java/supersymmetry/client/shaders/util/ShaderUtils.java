package supersymmetry.client.shaders.util;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

public class ShaderUtils {

    public static float[] invertMat4(float[] m) {
        float[] inv = new float[16];
        inv[0] = m[5] * m[10] * m[15] - m[5] * m[11] * m[14] - m[9] * m[6] * m[15] + m[9] * m[7] * m[14] +
                m[13] * m[6] * m[11] - m[13] * m[7] * m[10];
        inv[4] = -m[4] * m[10] * m[15] + m[4] * m[11] * m[14] + m[8] * m[6] * m[15] - m[8] * m[7] * m[14] -
                m[12] * m[6] * m[11] + m[12] * m[7] * m[10];
        inv[8] = m[4] * m[9] * m[15] - m[4] * m[11] * m[13] - m[8] * m[5] * m[15] + m[8] * m[7] * m[13] +
                m[12] * m[5] * m[11] - m[12] * m[7] * m[9];
        inv[12] = -m[4] * m[9] * m[14] + m[4] * m[10] * m[13] + m[8] * m[5] * m[14] - m[8] * m[6] * m[13] -
                m[12] * m[5] * m[10] + m[12] * m[6] * m[9];
        inv[1] = -m[1] * m[10] * m[15] + m[1] * m[11] * m[14] + m[9] * m[2] * m[15] - m[9] * m[3] * m[14] -
                m[13] * m[2] * m[11] + m[13] * m[3] * m[10];
        inv[5] = m[0] * m[10] * m[15] - m[0] * m[11] * m[14] - m[8] * m[2] * m[15] + m[8] * m[3] * m[14] +
                m[12] * m[2] * m[11] - m[12] * m[3] * m[10];
        inv[9] = -m[0] * m[9] * m[15] + m[0] * m[11] * m[13] + m[8] * m[1] * m[15] - m[8] * m[3] * m[13] -
                m[12] * m[1] * m[11] + m[12] * m[3] * m[9];
        inv[13] = m[0] * m[9] * m[14] - m[0] * m[10] * m[13] - m[8] * m[1] * m[14] + m[8] * m[2] * m[13] +
                m[12] * m[1] * m[10] - m[12] * m[2] * m[9];
        inv[2] = m[1] * m[6] * m[15] - m[1] * m[7] * m[14] - m[5] * m[2] * m[15] + m[5] * m[3] * m[14] +
                m[13] * m[2] * m[7] - m[13] * m[3] * m[6];
        inv[6] = -m[0] * m[6] * m[15] + m[0] * m[7] * m[14] + m[4] * m[2] * m[15] - m[4] * m[3] * m[14] -
                m[12] * m[2] * m[7] + m[12] * m[3] * m[6];
        inv[10] = m[0] * m[5] * m[15] - m[0] * m[7] * m[13] - m[4] * m[1] * m[15] + m[4] * m[3] * m[13] +
                m[12] * m[1] * m[7] - m[12] * m[3] * m[5];
        inv[14] = -m[0] * m[5] * m[14] + m[0] * m[6] * m[13] + m[4] * m[1] * m[14] - m[4] * m[2] * m[13] -
                m[12] * m[1] * m[6] + m[12] * m[2] * m[5];
        inv[3] = -m[1] * m[6] * m[11] + m[1] * m[7] * m[10] + m[5] * m[2] * m[11] - m[5] * m[3] * m[10] -
                m[9] * m[2] * m[7] + m[9] * m[3] * m[6];
        inv[7] = m[0] * m[6] * m[11] - m[0] * m[7] * m[10] - m[4] * m[2] * m[11] + m[4] * m[3] * m[10] +
                m[8] * m[2] * m[7] - m[8] * m[3] * m[6];
        inv[11] = -m[0] * m[5] * m[11] + m[0] * m[7] * m[9] + m[4] * m[1] * m[11] - m[4] * m[3] * m[9] -
                m[8] * m[1] * m[7] + m[8] * m[3] * m[5];
        inv[15] = m[0] * m[5] * m[10] - m[0] * m[6] * m[9] - m[4] * m[1] * m[10] + m[4] * m[2] * m[9] +
                m[8] * m[1] * m[6] - m[8] * m[2] * m[5];
        float det = m[0] * inv[0] + m[1] * inv[4] + m[2] * inv[8] + m[3] * inv[12];
        if (Math.abs(det) < 1e-10f) return new float[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
        float id = 1f / det;
        for (int i = 0; i < 16; i++) inv[i] *= id;
        return inv;
    }

    public static void setUniform1f(int prog, String name, float v) {
        int loc = GL20.glGetUniformLocation(prog, name);
        if (loc >= 0) GL20.glUniform1f(loc, v);
    }

    public static void setUniform3f(int prog, String name, float x, float y, float z) {
        int loc = GL20.glGetUniformLocation(prog, name);
        if (loc >= 0) GL20.glUniform3f(loc, x, y, z);
    }

    public static void setUniformMat4(int prog, String name, float[] m) {
        int loc = GL20.glGetUniformLocation(prog, name);
        if (loc < 0) return;
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        buf.put(m).flip();
        GL20.glUniformMatrix4(loc, false, buf);
    }

    public static void setUniform2f(int prog, String name, float x, float y) {
        int loc = GL20.glGetUniformLocation(prog, name);
        if (loc >= 0) GL20.glUniform2f(loc, x, y);
    }

    public static void set1f(int p, String n, float v) {
        int l = GL20.glGetUniformLocation(p, n);
        if (l >= 0) GL20.glUniform1f(l, v);
    }

    public static void set1i(int p, String n, int v) {
        int l = GL20.glGetUniformLocation(p, n);
        if (l >= 0) GL20.glUniform1i(l, v);
    }

    public static void set2f(int p, String n, float x, float y) {
        int l = GL20.glGetUniformLocation(p, n);
        if (l >= 0) GL20.glUniform2f(l, x, y);
    }

    public static void set3f(int p, String n, float[] v) {
        int l = GL20.glGetUniformLocation(p, n);
        if (l >= 0) GL20.glUniform3f(l, v[0], v[1], v[2]);
    }

    public static float[] projectDirToNDC(float[] dir, float[] view, float[] proj) {
        // Transform direction by view (rotation only, no translation - w=0)
        float vx = view[0] * dir[0] + view[4] * dir[1] + view[8] * dir[2];
        float vy = view[1] * dir[0] + view[5] * dir[1] + view[9] * dir[2];
        float vz = view[2] * dir[0] + view[6] * dir[1] + view[10] * dir[2];
        // Behind camera
        if (vz >= 0) return new float[] { 0f, 0f };
        // Perspective projection: NDC.x = proj[0]*vx / -vz, NDC.y = proj[5]*vy / -vz
        float ndcX = proj[0] * vx / -vz;
        float ndcY = proj[5] * vy / -vz;
        return new float[] { ndcX, ndcY };
    }
}
