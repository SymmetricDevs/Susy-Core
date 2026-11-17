package supersymmetry.client.renderer.handler.entity;

import net.minecraft.client.renderer.entity.RenderManager;

import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import supersymmetry.common.entities.EntityKirov;

public class KirovRenderer extends GeoEntityRenderer<EntityKirov> {

    public KirovRenderer(RenderManager renderManager) {
        super(renderManager, new KirovModel());
    }
}
