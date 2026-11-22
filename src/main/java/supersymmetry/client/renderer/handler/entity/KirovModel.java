package supersymmetry.client.renderer.handler.entity;

import net.minecraft.util.ResourceLocation;

import software.bernie.geckolib3.model.AnimatedGeoModel;
import supersymmetry.Supersymmetry;
import supersymmetry.common.entities.EntityKirov;

public class KirovModel extends AnimatedGeoModel<EntityKirov> {

    private static final ResourceLocation modelResource = new ResourceLocation(Supersymmetry.MODID,
            "geo/kirov_airship.geo.json");
    private static final ResourceLocation textureResource = new ResourceLocation(Supersymmetry.MODID,
            "textures/entities/kirov_airship.png");
    private static final ResourceLocation animationResource = new ResourceLocation(Supersymmetry.MODID,
            "animations/kirov_airship.model.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityKirov entityKirov) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityKirov entityKirov) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityKirov entityKirov) {
        return animationResource;
    }
}
