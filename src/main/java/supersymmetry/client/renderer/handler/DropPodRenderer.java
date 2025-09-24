package supersymmetry.client.renderer.handler;

import net.minecraft.client.renderer.entity.RenderManager;

import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import supersymmetry.common.entities.EntityDropPod;

public class DropPodRenderer extends GeoEntityRenderer<EntityDropPod> {

    public DropPodRenderer(RenderManager renderManager) {
        super(renderManager, new DropPodModel());
    }
}
