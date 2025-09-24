package supersymmetry.client.renderer.handler;

import net.minecraft.client.renderer.entity.RenderManager;

import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import supersymmetry.common.entities.EntityDrone;

public class DroneRenderer extends GeoEntityRenderer<EntityDrone> {

    public DroneRenderer(RenderManager manager) {
        super(manager, new DroneModel());
    }
}
