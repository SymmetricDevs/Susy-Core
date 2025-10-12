package supersymmetry.client.renderer.handler.entity;

import net.minecraft.client.renderer.entity.RenderManager;

import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import supersymmetry.common.entities.EntityLander;

public class LanderRenderer extends GeoEntityRenderer<EntityLander> {

    public LanderRenderer(RenderManager renderManager) {
        super(renderManager, new LanderModel());
    }
}
