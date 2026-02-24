package supersymmetry.api.image;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * Loads a cubemap as 6 individual GL textures — one per face.
 * Used directly by RenderableCelestialObject to texture each QuadSphere face
 * with the matching cubemap face, eliminating seams entirely.
 *
 * Face index order matches QuadSphere.build() face order:
 * 0=+X, 1=-X, 2=+Y, 3=-Y, 4=+Z, 5=-Z
 */
public class Cubemap {

    // 6 separate GL texture IDs, one per face
    protected final int[] faceTexIds = new int[] { -1, -1, -1, -1, -1, -1 };

    private final ResourceLocation[] faces; // PX, NX, PY, NY, PZ, NZ
    private final ResourceLocation cross;
    protected boolean loaded = false;

    public Cubemap(ResourceLocation px, ResourceLocation nx,
                   ResourceLocation py, ResourceLocation ny,
                   ResourceLocation pz, ResourceLocation nz) {
        this.faces = new ResourceLocation[] { px, nx, py, ny, pz, nz };
        this.cross = null;
    }

    public Cubemap(ResourceLocation cross) {
        this.faces = null;
        this.cross = cross;
    }

    /** Legacy single-id load — returns the PX face id for compatibility. */
    public int load() throws IOException {
        loadAll();
        return faceTexIds[0];
    }

    public void loadAll() throws IOException {
        if (loaded) return;
        loaded = true;

        BufferedImage[] imgs = loadFaceImages();
        for (int i = 0; i < 6; i++) {
            faceTexIds[i] = uploadTexture(imgs[i]);
        }
    }

    /** Returns the GL texture id for a specific face (0=PX,1=NX,2=PY,3=NY,4=PZ,5=NZ). */
    public int getFaceTexId(int face) {
        return faceTexIds[face];
    }

    public boolean isLoaded() {
        return loaded;
    }

    public int getTextureId() {
        return faceTexIds[0];
    }

    // ------------------------------------------------------------------

    private int uploadTexture(BufferedImage img) {
        int id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        int w = img.getWidth(), h = img.getHeight();
        int[] pixels = new int[w * h];
        img.getRGB(0, 0, w, h, pixels, 0, w);

        byte[] data = new byte[w * h * 4];
        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            data[i * 4] = (byte) ((p >> 16) & 0xFF); // R
            data[i * 4 + 1] = (byte) ((p >> 8) & 0xFF); // G
            data[i * 4 + 2] = (byte) (p & 0xFF); // B
            data[i * 4 + 3] = (byte) ((p >> 24) & 0xFF); // A
        }

        java.nio.ByteBuffer buf = org.lwjgl.BufferUtils.createByteBuffer(data.length);
        buf.put(data).flip();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        return id;
    }

    private BufferedImage[] loadFaceImages() throws IOException {
        IResourceManager rm = Minecraft.getMinecraft().getResourceManager();
        BufferedImage[] imgs = new BufferedImage[6];

        if (faces != null) {
            for (int i = 0; i < 6; i++) {
                try (java.io.InputStream s = rm.getResource(faces[i]).getInputStream()) {
                    imgs[i] = ImageIO.read(s);
                    if (imgs[i] == null) throw new IOException("ImageIO returned null for " + faces[i]);
                }
            }
        } else {
            try (java.io.InputStream s = rm.getResource(cross).getInputStream()) {
                BufferedImage sheet = ImageIO.read(s);
                if (sheet == null) throw new IOException("ImageIO returned null for " + cross);
                int w = sheet.getWidth() / 4;
                int h = sheet.getHeight() / 3;

                // {col, row} for each face in order: PX, NX, PY, NY, PZ, NZ
                int[][] layout = {
                        { 2, 1 }, // PX (face 0) — 3rd column, middle row
                        { 0, 1 }, // NX (face 1) — 1st column, middle row
                        { 1, 0 }, // PY (face 2) — 2nd column, top row
                        { 1, 2 }, // NY (face 3) — 2nd column, bottom row
                        { 1, 1 }, // PZ (face 4) — 2nd column, middle row
                        { 3, 1 }, // NZ (face 5) — 4th column, middle row
                };

                for (int i = 0; i < 6; i++) {
                    BufferedImage sub = sheet.getSubimage(layout[i][0] * w, layout[i][1] * h, w, h);
                    BufferedImage copy = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    copy.getGraphics().drawImage(sub, 0, 0, null);
                    imgs[i] = copy;
                }
            }
        }
        return imgs;
    }
}
