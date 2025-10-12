package supersymmetry.client.renderer.handler.entity;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import supersymmetry.Supersymmetry;
import supersymmetry.common.entities.EntityDropPod;
import supersymmetry.common.entities.EntityLander;

public class LanderModel extends AnimatedGeoModel<EntityLander> {

    // TODO: Replace with Lander model when it is added
    private static final ResourceLocation modelResource = new ResourceLocation(Supersymmetry.MODID,
            "geo/drop_pod.geo.json");
    private static final ResourceLocation textureResource = new ResourceLocation(Supersymmetry.MODID,
            "textures/entities/drop_pod.png");
    private static final ResourceLocation animationResource = new ResourceLocation(Supersymmetry.MODID,
            "animations/drop_pod.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityLander entityDropPod) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityLander entityDropPod) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityLander entityDropPod) {
        return animationResource;
    }
}
