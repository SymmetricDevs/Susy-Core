package supersymmetry.client.renderer.handler;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.RelativeDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
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

    private static void rotateBlock(MetaTileEntity mte) {
        EnumFacing front = mte.getFrontFacing();
        if (mte instanceof MultiblockControllerBase controller) {
            EnumFacing upwards = controller.getUpwardsFacing();

            //            if (controller.allowsExtendedFacing()) {
            //                int degree = 90 * (upwards == EnumFacing.EAST ? -1 :
            //                        upwards == EnumFacing.SOUTH ? 2 : upwards == EnumFacing.WEST ? 1 : 0);
            //                GlStateManager.rotate(degree, 0, 0, -1);
            //                if (front == EnumFacing.DOWN && upwards.getAxis() == EnumFacing.Axis.Z) {
            //                    GlStateManager.rotate(180, 0, 1, 0);
            //                }
            //            }

            if (controller.allowsFlip() && controller.isFlipped()) {

                EnumFacing left = RelativeDirection.LEFT.getRelativeFacing(front, upwards, true);

                int fX = left.getXOffset() == 0 ? 1 : -1;
                int fY = left.getYOffset() == 0 ? 1 : -1;
                int fZ = left.getZOffset() == 0 ? 1 : -1;

                GlStateManager.scale(fX, fY, fZ);
            }
        }
        FixedGeoBlockRenderer.rotateBlock(front);
    }

    //    public static void rotateBlock(MetaTileEntity mte) {
    //        Matrix4 translation = new Matrix4();
    //        float halfPi = (float) (Math.PI / 2);
    //
    //        EnumFacing front = mte.getFrontFacing();
    //
    //        translation.apply(switch (front) {
    //            case SOUTH -> new Rotation(halfPi * 2, 0, 1, 0);
    //            case WEST ->  new Rotation(halfPi, 0, 1, 0);
    //            case EAST ->  new Rotation(halfPi * 3, 0, 1, 0);
    //            case UP ->  new Rotation(halfPi, 1, 0, 0);
    //            case DOWN ->  new Rotation(halfPi * 3, 1, 0, 0);
    //            default -> new RedundantTransformation(); // Do nothing for North
    //        });
    //
    //        if (mte instanceof MultiblockControllerBase controller) {
    //            EnumFacing upwards = controller.getUpwardsFacing();
    //
    //            if (controller.allowsExtendedFacing()) {
    //                double degree = Math.PI / 2 * (upwards == EnumFacing.EAST ? -1 :
    //                        upwards == EnumFacing.SOUTH ? 2 : upwards == EnumFacing.WEST ? 1 : 0);
    //                Rotation rotation = new Rotation(degree, front.getXOffset(), front.getYOffset(),
    //                        front.getZOffset());
    //                if (front == EnumFacing.DOWN && upwards.getAxis() == EnumFacing.Axis.Z) {
    //                    translation.apply(new Rotation(Math.PI, 0, 1, 0));
    //                }
    //                translation.apply(rotation);
    //            }
    //
    //            if (controller.allowsFlip() && controller.isFlipped()) {
    //
    //                EnumFacing left = RelativeDirection.LEFT.getRelativeFacing(front, upwards, true);
    //
    //                int fX = left.getXOffset() == 0 ? 1 : -1;
    //                int fY = left.getYOffset() == 0 ? 1 : -1;
    //                int fZ = left.getZOffset() == 0 ? 1 : -1;
    //
    //                translation.scale(fX, fY, fZ);
    //            }
    //        }
    //
    //    }

    @SuppressWarnings("DuplicatedCode")
    public <T extends MetaTileEntity & IAnimatableMTE> void render(
            T mte, double x, double y, double z, float partialTicks) {
        GeoModel model = MODEL_DISPATCHER.getModel(MODEL_DISPATCHER.getModelLocation(mte));
        Vec3i vec3i = mte.getTransformation();
        MODEL_DISPATCHER.setLivingAnimations(mte, getUniqueID(mte));

        GlStateManager.pushMatrix();
        {
            FixedGeoBlockRenderer.setupLight(mte.getWorld().getCombinedLight(mte.getLightPos(), 0));

            GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
            GlStateManager.translate(vec3i.getX(), vec3i.getY(), vec3i.getZ());
            rotateBlock(mte);

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
