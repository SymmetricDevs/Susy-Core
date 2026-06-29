package supersymmetry.integration.immersiverailroading.model;

import java.nio.DoubleBuffer;

import net.minecraft.client.Minecraft;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import cam72cam.immersiverailroading.model.StockModel;
import cam72cam.immersiverailroading.model.components.ComponentProvider;
import cam72cam.immersiverailroading.model.components.ModelComponent;
import cam72cam.mod.render.obj.OBJRender;
import cam72cam.mod.render.opengl.RenderState;
import supersymmetry.common.entities.EntityTransporterErector;
import supersymmetry.integration.immersiverailroading.model.part.Rocket;
import supersymmetry.integration.immersiverailroading.model.part.TransporterLifter;
import supersymmetry.integration.immersiverailroading.registry.TransporterErectorDefinition;
import util.Matrix4;

public class TransporterErectorModel extends StockModel<EntityTransporterErector, TransporterErectorDefinition> {

    // The rocket is rotated about this point (in OBJ model space) by the lifter arm. It is also the
    // end the rocket is assembled from, so the assembly sweep grows away from it along the X axis.
    private static final double PIVOT_X = -7.0;
    private static final double PIVOT_Y = 1.1;

    public Rocket rocket;
    public TransporterLifter lifter;

    // Reused buffer for the clip-plane equation, to avoid per-frame allocation.
    private final DoubleBuffer clipPlane = BufferUtils.createDoubleBuffer(4);

    public TransporterErectorModel(TransporterErectorDefinition def) throws Exception {
        super(def);
    }

    @Override
    protected void parseComponents(ComponentProvider provider, TransporterErectorDefinition def) {
        super.parseComponents(provider, def);
        this.rocket = new Rocket(provider, this.base);
        this.lifter = new TransporterLifter(provider, this.base, (stock) -> stock.getLifterAngle());
    }

    @Override
    protected void postRender(EntityTransporterErector stock, RenderState state, float partialTicks) {
        // Clone the incoming state before super mutates it: the rocket hangs off the non-rocking
        // base, so it must not inherit the sway roll that postRender applies for the other parts.
        RenderState rocketState = state.clone();
        super.postRender(stock, state, partialTicks);

        float renderTime = partialTicks + Minecraft.getMinecraft().world.getWorldTime();
        float progress = stock.getAssemblyProgress(renderTime);
        ModelComponent component = rocket != null ? rocket.getComponent() : null;
        if (progress <= 0f || component == null) {
            return;
        }

        // Match the GL state and transforms StockModel#renderEntity sets up for the base model.
        double scale = stock.gauge.scale();
        rocketState.lighting(true).cull_face(false).rescale_normal(true).scale(scale, scale, scale);
        rocketState.model_view().multiply(new Matrix4()
                .translate(PIVOT_X, PIVOT_Y, 0)
                .rotate(stock.getLifterAngle(), 0, 0, 1)
                .translate(-PIVOT_X, -PIVOT_Y, 0));

        // bind() applies the above onto the fixed-function modelview matrix, so a clip plane set
        // afterwards (expressed in the rocket's local coordinates) cleaves the geometry along the
        // length axis regardless of how the individual triangles are laid out.
        OBJRender.Binding binding = this.binder().texture(stock.getTexture()).bind(rocketState);
        boolean clip = false;
        try {
            if (binding.isLoaded()) {
                clip = progress < 1f;
                if (clip) {
                    setLengthClipPlane(component, progress);
                    GL11.glEnable(GL11.GL_CLIP_PLANE0);
                }
                binding.draw(component.modelIDs);
            }
        } finally {
            // Always disable the clip plane if it was enabled, or it would clip everything else too.
            if (clip) {
                GL11.glDisable(GL11.GL_CLIP_PLANE0);
            }
            binding.restore();
        }
    }

    /**
     * Configures {@link GL11#GL_CLIP_PLANE0} (in the rocket's local space) to keep only the portion
     * of the rocket up to {@code progress} of its length, sweeping from the base (the end nearest
     * the lifter pivot) toward the nose. The plane equation is given in object coordinates; OpenGL
     * transforms it by the currently-bound modelview matrix.
     */
    private void setLengthClipPlane(ModelComponent component, float progress) {
        double minX = component.min.x;
        double maxX = component.max.x;
        boolean baseAtMin = Math.abs(minX - PIVOT_X) <= Math.abs(maxX - PIVOT_X);

        double a;
        double d;
        if (baseAtMin) {
            // keep x <= threshold -> -x + threshold >= 0
            double threshold = minX + progress * (maxX - minX);
            a = -1.0;
            d = threshold;
        } else {
            // keep x >= threshold -> x - threshold >= 0
            double threshold = maxX - progress * (maxX - minX);
            a = 1.0;
            d = -threshold;
        }

        clipPlane.clear();
        clipPlane.put(a).put(0.0).put(0.0).put(d);
        clipPlane.flip();
        GL11.glClipPlane(GL11.GL_CLIP_PLANE0, clipPlane);
    }
}
