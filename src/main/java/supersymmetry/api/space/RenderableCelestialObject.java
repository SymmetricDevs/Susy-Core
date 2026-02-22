package supersymmetry.api.space;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import supersymmetry.api.image.Cubemap;

public class RenderableCelestialObject {

    private final CelestialObject object;
    private final Cubemap cubemap;

    private int textureId = -1;

    public RenderableCelestialObject(CelestialObject object, Cubemap cubemap) {
        this.object = object;
        this.cubemap = cubemap;
    }

    public CelestialObject getCelestialObject() {
        return object;
    }

    public int getTextureId() {
        if (textureId == -1) {
            try {
                textureId = cubemap.load();
            } catch (Exception e) {
                e.printStackTrace();
                textureId = -1;
            }
        }
        return textureId;
    }

    public void render(double scale, QuadSphere mesh) {
        int tex = getTextureId();
        if (tex == -1) return;

        GL11.glPushMatrix();

        GL11.glScaled(scale, scale, scale);

        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, tex);

        GL11.glBegin(GL11.GL_QUADS);
        for (int[] quad : mesh.getQuads()) {
            for (int idx : quad) {
                QuadSphere.Vertex v = mesh.getVertices().get(idx);
                GL11.glNormal3f(v.nx, v.ny, v.nz);
                GL11.glVertex3f(v.x, v.y, v.z);
            }
        }
        GL11.glEnd();

        GL11.glPopMatrix();
    }
}
