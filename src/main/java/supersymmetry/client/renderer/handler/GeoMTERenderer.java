package supersymmetry.client.renderer.handler;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import software.bernie.geckolib3.core.IAnimatableModel;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import supersymmetry.api.metatileentity.IAnimatableMTE;

public enum GeoMTERenderer implements IGeoRenderer<IAnimatableMTE> {
    INSTANCE;

    public final AnimatedGeoModel<IAnimatableMTE> MODEL_DISPATCHER = new ModelDispatcher();

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
    public GeoModelProvider<IAnimatableMTE> getGeoModelProvider() {
        return MODEL_DISPATCHER;
    }

    @Override
    public ResourceLocation getTextureLocation(IAnimatableMTE mte) {
        return MODEL_DISPATCHER.getTextureLocation(mte);
    }

    private static boolean isFlipped(MetaTileEntity mte) {
        if (mte instanceof MultiblockControllerBase controller) {
            return controller.isFlipped();
        }
        return false;
    }

    @SuppressWarnings("DuplicatedCode")
    public <T extends MetaTileEntity & IAnimatableMTE> void render(T mte, double x, double y, double z, float partialTicks) {
        GeoModel model = MODEL_DISPATCHER.getModel(MODEL_DISPATCHER.getModelLocation(mte));
        Vec3i vec3i = mte.getTransformation();
        MODEL_DISPATCHER.setLivingAnimations(mte, getUniqueID(mte));

        GlStateManager.pushMatrix();
        {
            int light = mte.getWorld().getCombinedLight(mte.getLightPos(), 0);

            FixedGeoBlockRenderer.setupLight(light);

            GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
            GlStateManager.translate(vec3i.getX(), vec3i.getY(), vec3i.getZ());

            // TODO: use [RelativeDirection] to handle flipped multiblocks
            FixedGeoBlockRenderer.rotateBlock(mte.getFrontFacing(), isFlipped(mte));

            Minecraft.getMinecraft().getTextureManager().bindTexture(getTextureLocation(mte));
            render(model, mte, partialTicks, 1, 1, 1, 1);
        }
        GlStateManager.popMatrix();
    }

    private static class ModelDispatcher extends AnimatedGeoModel<IAnimatableMTE> {

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
