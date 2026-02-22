package supersymmetry.api.image;

import java.awt.Color;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

/**
 * Debug cubemap that uploads a solid color to all 6 faces.
 * Use this to verify orientation, rendering, and GL state without
 * worrying about texture resources.
 */
public class DebugCubemap {

    private final int r, g, b, a;
    private int size = 16;
    private int textureId = -1;

    /**
     * @param color The solid color used for all 6 faces
     */
    public DebugCubemap(Color color, int size) {
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
        this.a = color.getAlpha();
        this.size = size;
    }

    /**
     * Generates the cubemap texture.
     */
    public int load() {
        if (textureId != -1) return textureId;

        textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, textureId);

        // Filtering
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);

        uploadSolidColorToAllFaces();

        return textureId;
    }

    private void uploadSolidColorToAllFaces() {
        // Create 1 face of solid color
        ByteBuffer buf = BufferUtils.createByteBuffer(size * size * 4);

        for (int i = 0; i < size * size; i++) {
            buf.put((byte) b);  // OpenGL expects BGRA
            buf.put((byte) g);
            buf.put((byte) r);
            buf.put((byte) a);
        }

        buf.flip();

        // Upload to all six faces
        for (int face = 0; face < 6; face++) {
            GL11.glTexImage2D(
                    GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + face,
                    0,
                    GL11.GL_RGBA8,
                    size,
                    size,
                    0,
                    GL11.GL_RGBA,
                    GL11.GL_UNSIGNED_BYTE,
                    buf);
        }
    }

    public int getTextureId() {
        return textureId;
    }
}
