package supersymmetry.api.image;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Cubemap {

    private final ResourceLocation[] faces;
    private final ResourceLocation cross;
    private int textureId = -1;

    // Constructor for 6 textures
    public Cubemap(ResourceLocation px, ResourceLocation nx,
                   ResourceLocation py, ResourceLocation ny,
                   ResourceLocation pz, ResourceLocation nz) {
        this.faces = new ResourceLocation[]{px, nx, py, ny, pz, nz};
        this.cross = null;
    }

    // Constructor for 1 cross-layout texture
    public Cubemap(ResourceLocation cross) {
        this.faces = null;
        this.cross = cross;
    }

    public int load() throws IOException {
        if (textureId != -1) return textureId;

        textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, textureId);

        // Basic filtering
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);

        if (faces != null) {
            loadSixFaceTextures();
        } else {
            loadCrossTexture();
        }

        return textureId;
    }

    private void uploadToCubemap(int target, BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();

        int[] pixels = new int[w * h];
        img.getRGB(0, 0, w, h, pixels, 0, w);

        // Convert ARGB → ABGR (OpenGL expects ABGR for glTexImage2D)
        byte[] data = new byte[w * h * 4];
        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            int a = (p >> 24) & 0xFF;
            int r = (p >> 16) & 0xFF;
            int g = (p >> 8) & 0xFF;
            int b = p & 0xFF;

            int idx = i * 4;
            data[idx]     = (byte)b;
            data[idx + 1] = (byte)g;
            data[idx + 2] = (byte)r;
            data[idx + 3] = (byte)a;
        }

        GL11.glTexImage2D(target, 0, GL11.GL_RGBA8, w, h, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, org.lwjgl.BufferUtils.createByteBuffer(data.length).put(data).flip());
    }

    private void loadSixFaceTextures() throws IOException {
        IResourceManager rm = Minecraft.getMinecraft().getResourceManager();

        for (int i = 0; i < 6; i++) {
            IResource res = rm.getResource(faces[i]);
            BufferedImage img = ImageIO.read(res.getInputStream());
            uploadToCubemap(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, img);
        }
    }

    private void loadCrossTexture() throws IOException {
        IResourceManager rm = Minecraft.getMinecraft().getResourceManager();
        IResource res = rm.getResource(cross);
        BufferedImage img = ImageIO.read(res.getInputStream());

        int w = img.getWidth() / 3;
        int h = img.getHeight() / 4;

        // Standard vertical cross layout:
        //        PY
        //   NX   PZ   PX
        //        NY
        //        NZ

        int[][] layout = {
                {2, 1}, // PX
                {0, 1}, // NX
                {1, 0}, // PY
                {1, 2}, // NY
                {1, 1}, // PZ
                {1, 3}  // NZ
        };

        for (int i = 0; i < 6; i++) {
            int sx = layout[i][0] * w;
            int sy = layout[i][1] * h;
            BufferedImage face = img.getSubimage(sx, sy, w, h);

            uploadToCubemap(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, face);
        }
    }

    public int getTextureId() {
        return textureId;
    }
}
