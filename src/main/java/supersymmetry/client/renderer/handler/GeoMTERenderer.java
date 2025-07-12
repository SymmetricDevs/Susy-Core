package supersymmetry.client.renderer.handler;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import software.bernie.geckolib3.core.IAnimatableModel;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import supersymmetry.api.metatileentity.IAnimatableMTE;

public enum GeoMTERenderer implements IGeoRenderer<IAnimatableMTE> {
    INSTANCE;

    public final AnimatedGeoModel<IAnimatableMTE> MODEL_DISPATCHER = new ModelDispatcher();

    @Override
    public GeoModelProvider<IAnimatableMTE> getGeoModelProvider() {
        return MODEL_DISPATCHER;
    }

    @Override
    public ResourceLocation getTextureLocation(IAnimatableMTE mte) {
        return MODEL_DISPATCHER.getTextureLocation(mte);
    }

    @SuppressWarnings("DuplicatedCode")
    public <T extends MetaTileEntity & IAnimatableMTE> void render(T mte, double x, double y, double z, float partialTicks) {
        GeoModel model = MODEL_DISPATCHER.getModel(MODEL_DISPATCHER.getModelLocation(mte));
        Vec3i vec3i = mte.getTransformation();
        MODEL_DISPATCHER.setLivingAnimations(mte, this.getUniqueID(mte));

        FixedGeoBlockRenderer.setupLight(mte.getWorld().getCombinedLight(mte.getPos().add(vec3i), 0));

        GlStateManager.pushMatrix();
        {
            GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
            GlStateManager.translate(vec3i.getX(), vec3i.getY(), vec3i.getZ());

            /// TODO: use [gregtech.api.util.RelativeDirection] to handle flipped multiblocks
            FixedGeoBlockRenderer.rotateBlock(mte.getFrontFacing());

            Minecraft.getMinecraft().getTextureManager().bindTexture(this.getTextureLocation(mte));

            Color renderColor = this.getRenderColor(mte, partialTicks);
            this.render(model, mte, partialTicks,
                    (float) renderColor.getRed() / 255.0F,
                    (float) renderColor.getGreen() / 255.0F,
                    (float) renderColor.getBlue() / 255.0F,
                    (float) renderColor.getAlpha() / 255.0F);
        }
        GlStateManager.popMatrix();
    }

    private static class ModelDispatcher extends AnimatedGeoModel<IAnimatableMTE> {

        static {
            AnimationController.addModelFetcher(object -> {
                if (object instanceof IAnimatableMTE) {
                    //noinspection rawtypes,unchecked
                    return (IAnimatableModel) INSTANCE.getGeoModelProvider();
                }
                return null;
            });
        }

        @Override
        public ResourceLocation getModelLocation(IAnimatableMTE mte) {
            return mte.modelRL();
        }

        @Override
        public ResourceLocation getTextureLocation(IAnimatableMTE mte) {
            return mte.textureRL();
        }

        @Override
        public ResourceLocation getAnimationFileLocation(IAnimatableMTE mte) {
            return mte.animationRL();
        }
    }
}
