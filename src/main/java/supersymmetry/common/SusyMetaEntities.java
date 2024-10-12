package supersymmetry.common;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import supersymmetry.Supersymmetry;
import supersymmetry.client.renderer.handler.DroneRenderer;
import supersymmetry.client.renderer.handler.DropPodRenderer;
import supersymmetry.client.renderer.handler.RocketRenderer;
import supersymmetry.common.entities.EntityDrone;
import supersymmetry.common.entities.EntityDropPod;
import supersymmetry.common.entities.EntityRocket;

public class SusyMetaEntities {

    public static void init() {
        EntityRegistry.registerModEntity(new ResourceLocation(Supersymmetry.MODID, "drop_pod"), EntityDropPod.class, "Drop Pod", 1, Supersymmetry.instance, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Supersymmetry.MODID, "drone"), EntityDrone.class, "Drone", 2, Supersymmetry.instance, 64, 3, true);
        EntityRegistry.registerModEntity(new ResourceLocation(Supersymmetry.MODID, "rocket_basic"), EntityRocket.class, "Rocket", 1, Supersymmetry.instance, 64, 3, true);
    }

    @SideOnly(Side.CLIENT)
    public static void initRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(EntityDropPod.class, DropPodRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityDrone.class, DroneRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityRocket.class, RocketRenderer::new);
    }

}
