package supersymmetry.client.renderer.handler;

import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import supersymmetry.Supersymmetry;
import supersymmetry.common.entities.EntityLander;

public class LanderModel extends AnimatedGeoModel<EntityLander> {

    private static final ResourceLocation modelResource = new ResourceLocation(Supersymmetry.MODID, "geo/lander.geo.json");
    private static final ResourceLocation textureResource = new ResourceLocation(Supersymmetry.MODID, "textures/entities/lander.png");
    private static final ResourceLocation animationResource = new ResourceLocation(Supersymmetry.MODID, "animations/lander.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityLander entityLander) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityLander entityLander) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityLander entityLander) {
        return animationResource;
    }

}
