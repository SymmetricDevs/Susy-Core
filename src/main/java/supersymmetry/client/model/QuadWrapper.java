package supersymmetry.client.model;

import codechicken.lib.lighting.LC;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.consumer.UnpackingVertexConsumer;
import codechicken.lib.render.pipeline.IVertexSource;
import codechicken.lib.render.pipeline.attribute.AttributeKey;
import codechicken.lib.render.pipeline.attribute.LightCoordAttribute;
import codechicken.lib.render.pipeline.attribute.SideAttribute;
import codechicken.lib.vec.Vertex5;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;

public class QuadWrapper implements IVertexSource {

    public final TextureAtlasSprite sprite;
    public Vertex5[] vertices = new Vertex5[4];
    public LC[] lightCoords = new LC[4];
    public int side;

    public QuadWrapper(BakedQuad quad) {
        VertexFormat format = quad.getFormat();

        this.side = quad.getFace().getIndex();
        this.sprite = quad.getSprite();

        UnpackingVertexConsumer consumer = new UnpackingVertexConsumer(quad.getFormat());
        quad.pipe(consumer);
        float[][][] unpackedData = consumer.getUnpackedData();
        for (int v = 0; v < 4; v++) {
            vertices[v] = new Vertex5();
            lightCoords[v] = new LC();
            for (int e = 0; e < format.getElementCount(); e++) {
                float[] data = unpackedData[v][e];
                switch (format.getElement(e).getUsage()) {
                    case POSITION -> vertices[v].vec.set(data);
                    case UV -> {
                        if (format.getElement(e).getIndex() == 0) {
                            vertices[v].uv.set(data[0], data[1]);
                        }
                    }
                    default -> {}
                }
            }
            lightCoords[v].compute(vertices[v].vec, side);
        }
    }

    @Override
    public Vertex5[] getVertices() {
        return vertices;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttributes(AttributeKey<T> attr) {
        return LightCoordAttribute.attributeKey.equals(attr) ? (T) lightCoords : null;
    }

    @Override
    public boolean hasAttribute(AttributeKey<?> attr) {
        return SideAttribute.attributeKey.equals(attr) || LightCoordAttribute.attributeKey.equals(attr);
    }

    @Override
    public void prepareVertex(CCRenderState state) {
        state.side = this.side;
    }
}
