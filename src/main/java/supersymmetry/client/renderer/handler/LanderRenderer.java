package supersymmetry.client.renderer.handler;

import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import supersymmetry.common.entities.EntityLander;

public class LanderRenderer extends GeoEntityRenderer<EntityLander> {
    public LanderRenderer(RenderManager manager) {
        super(manager, new LanderModel());
    }
}
