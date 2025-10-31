package supersymmetry.client.renderer.handler;

import net.minecraft.util.ResourceLocation;

import software.bernie.geckolib3.model.AnimatedGeoModel;
import supersymmetry.Supersymmetry;
import supersymmetry.common.entities.EntityDropPod;

public class DropPodModel extends AnimatedGeoModel<EntityDropPod> {

    private static final ResourceLocation modelResource = new ResourceLocation(Supersymmetry.MODID,
            "geo/drop_pod.geo.json");
    private static final ResourceLocation textureResource = new ResourceLocation(Supersymmetry.MODID,
            "textures/entities/drop_pod.png");
    private static final ResourceLocation animationResource = new ResourceLocation(Supersymmetry.MODID,
            "animations/drop_pod.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityDropPod entityDropPod) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityDropPod entityDropPod) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityDropPod entityDropPod) {
        return animationResource;
    }
}
