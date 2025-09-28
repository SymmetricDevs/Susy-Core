package supersymmetry.client.renderer.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.RelativeDirection;
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
                // noinspection rawtypes,unchecked
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

    public static void rotateToFace(EnumFacing face, EnumFacing spin) {
        int angle = spin == EnumFacing.EAST ? 90 : spin == EnumFacing.SOUTH ? 180 : spin == EnumFacing.WEST ? 270 : 0;
        switch (face) {
            case UP -> {
                GlStateManager.scale(-1, 1, 1);
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(-angle, 0, 0, 1);
            }
            case DOWN -> {
                GlStateManager.rotate(270.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(spin == EnumFacing.EAST || spin == EnumFacing.WEST ? -angle : angle, 0, 0, 1);
            }
            case EAST -> {
                GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(angle, 0, 0, 1);
            }
            case WEST -> {
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(angle, 0, 0, 1);
            }
            case NORTH -> GlStateManager.rotate(angle, 0, 0, 1);
            case SOUTH -> {
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(angle, 0, 0, 1);
            }
        }
    }

    public static void flip(EnumFacing facing) {
        int fX = facing.getXOffset() == 0 ? 1 : -1;
        int fY = facing.getYOffset() == 0 ? 1 : -1;
        int fZ = facing.getZOffset() == 0 ? 1 : -1;
        GlStateManager.scale(fX, fY, fZ);
    }

    @SuppressWarnings("DuplicatedCode")
    public <T extends MetaTileEntity & IAnimatableMTE> void render(T mte, double x, double y, double z,
                                                                   float partialTicks) {
        GeoModel model = MODEL_DISPATCHER.getModel(MODEL_DISPATCHER.getModelLocation(mte));
        Vec3i vec3i = mte.getTransformation();
        MODEL_DISPATCHER.setLivingAnimations(mte, getUniqueID(mte));

        GlStateManager.pushMatrix();
        {
            FixedGeoBlockRenderer.setupLight(mte.getWorld().getCombinedLight(mte.getLightPos(), 0));

            EnumFacing front = mte.getFrontFacing();
            GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
            GlStateManager.translate(vec3i.getX(), vec3i.getY(), vec3i.getZ());

            if (mte instanceof MultiblockControllerBase controller) {
                EnumFacing upwards = controller.getUpwardsFacing();
                EnumFacing left = RelativeDirection.LEFT.getRelativeFacing(front, upwards, controller.isFlipped());

                if (/* controller.allowsFlip() && */ controller.isFlipped()) flip(left);
                rotateToFace(front, upwards);
            }

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
