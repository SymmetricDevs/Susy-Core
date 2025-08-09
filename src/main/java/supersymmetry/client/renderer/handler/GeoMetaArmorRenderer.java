package supersymmetry.client.renderer.handler;

import gregtech.api.items.armor.ArmorMetaItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimatableModel;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.util.GeoUtils;
import supersymmetry.api.items.IGeoMetaArmor;

import java.util.Objects;

// I'm just lazy
@SuppressWarnings("DuplicatedCode")
public class GeoMetaArmorRenderer extends ModelBiped implements IGeoRenderer<IGeoMetaArmor> {

    public static final GeoMetaArmorRenderer INSTANCE = new GeoMetaArmorRenderer();
    // Set these to the names of your armor's bones
    public static final String BONE_HEAD = "head";
    public static final String BONE_BODY = "body";
    public static final String BONE_ARM_RIGHT = "rightArm";
    public static final String BONE_ARM_LEFT = "leftArm";
    public static final String BONE_LEG_RIGHT = "rightLeg";
    public static final String BONE_LEG_LEFT = "leftLeg";
    public static final String BONE_BOOT_RIGHT = "rightBoot";
    public static final String BONE_BOOT_LEFT = "leftBoot";

    static {
        AnimationController.addModelFetcher((IAnimatable object) -> {
            if (object instanceof IGeoMetaArmor) {
                //noinspection rawtypes,unchecked
                return (IAnimatableModel) INSTANCE.getGeoModelProvider();
            }
            return null;
        });
    }

    public final AnimatedGeoModel<IGeoMetaArmor> modelDispatcher = new ModelDispatcher();
    private IGeoMetaArmor currentMetaArmor;
    private EntityLivingBase entityLiving;
    private ItemStack itemStack;
    private EntityEquipmentSlot armorSlot;
    // Workaround to respect the entity model
    private ModelBiped defaultArmor;

    private static void scale(float scale, IBone... bones) {
        for (var bone : bones) {
            bone.setScaleX(scale);
            bone.setScaleY(scale);
            bone.setScaleZ(scale);
        }
    }

    private static void translate(float x, float y, float z, IBone... bones) {
        for (var bone : bones) {
            bone.setPositionX(bone.getPositionX() + x);
            bone.setPositionY(bone.getPositionY() + y);
            bone.setPositionZ(bone.getPositionZ() + z);
        }
    }

    @Override
    public GeoModelProvider<IGeoMetaArmor> getGeoModelProvider() {
        return modelDispatcher;
    }

    @Override
    public ResourceLocation getTextureLocation(IGeoMetaArmor geoMetaArmor) {
        return geoMetaArmor.textureRL();
    }

    @Override
    public void render(@NotNull Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
        this.renderGeo(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }

    public void renderGeo(@NotNull Entity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float netHeadYaw, float headPitch, float scale) {
        GeoModel model = modelDispatcher.getModel(modelDispatcher.getModelLocation(currentMetaArmor));
        modelDispatcher.setLivingAnimations(currentMetaArmor, getUniqueID(currentMetaArmor), null);

        this.defaultArmor.setModelAttributes(this);
        this.defaultArmor.setRotationAngles(limbSwing, limbSwingAmount, partialTicks, netHeadYaw, headPitch, scale, entityIn);

        this.fitToBiped();

        IBone headBone = this.modelDispatcher.getBone(BONE_HEAD);
        IBone bodyBone = this.modelDispatcher.getBone(BONE_BODY);
        IBone rightArmBone = this.modelDispatcher.getBone(BONE_ARM_RIGHT);
        IBone leftArmBone = this.modelDispatcher.getBone(BONE_ARM_LEFT);
        IBone rightLegBone = this.modelDispatcher.getBone(BONE_LEG_RIGHT);
        IBone leftLegBone = this.modelDispatcher.getBone(BONE_LEG_LEFT);
        IBone rightBootBone = this.modelDispatcher.getBone(BONE_BOOT_RIGHT);
        IBone leftBootBone = this.modelDispatcher.getBone(BONE_BOOT_LEFT);

        // These are all magic numbers from simply trying
        if (this.isChild) {
            translate(0.0F, -12.0F, 0.0F,
                    headBone, bodyBone);
            translate(2.5F, -11.0F, 0.0F,
                    rightArmBone);
            translate(-2.5F, -11.0F, 0.0F,
                    leftArmBone);
            translate(1.0F, -6.0F, 0.0F,
                    rightLegBone, rightBootBone);
            translate(-1.0F, -6.0F, 0.0F,
                    leftLegBone, leftBootBone);
            scale(0.75F,
                    headBone);
            scale(0.5F,
                    bodyBone,
                    rightArmBone, leftArmBone,
                    rightLegBone, leftLegBone,
                    rightBootBone, leftBootBone);
        } else if (this.isSneak) {
            translate(0.0F, -3.0F, 0.0F,
                    headBone);
            translate(0.0F, -3.0F, 0.0F,
                    bodyBone,
                    rightArmBone, leftArmBone);
            translate(0.0F, -0.2F, 0.0F,
                    headBone, bodyBone,
                    rightArmBone, leftArmBone,
                    rightLegBone, leftLegBone,
                    rightBootBone, leftBootBone);
            translate(0.0F, -3.0F, 0.0F,
                    rightLegBone, leftLegBone,
                    rightBootBone, leftBootBone);
        }

        Color renderColor = getRenderColor(currentMetaArmor, partialTicks);

        float red = (float) renderColor.getRed() / 255F;
        float green = (float) renderColor.getGreen() / 255F;
        float blue = (float) renderColor.getBlue() / 255F;
        float alpha = (float) renderColor.getAlpha() / 255F;

        GlStateManager.pushMatrix();
        {
            GlStateManager.translate(0.0F, 1.5F, 0.0F);
            GlStateManager.scale(-1.0F, -1.0F, 1.0F);


            Minecraft.getMinecraft().renderEngine.bindTexture(getTextureLocation(currentMetaArmor));

            render(model, currentMetaArmor, partialTicks, red, green, blue, alpha);

            GlStateManager.scale(-1.0F, -1.0F, 1.0F);
            GlStateManager.translate(0.0F, -1.5F, 0.0F);
        }
        GlStateManager.popMatrix();
    }

    private void fitToBiped() {
        if (!(this.entityLiving instanceof EntityArmorStand)) {
            IBone headBone = this.modelDispatcher.getBone(BONE_HEAD);
            IBone bodyBone = this.modelDispatcher.getBone(BONE_BODY);
            IBone rightArmBone = this.modelDispatcher.getBone(BONE_ARM_RIGHT);
            IBone leftArmBone = this.modelDispatcher.getBone(BONE_ARM_LEFT);
            IBone rightLegBone = this.modelDispatcher.getBone(BONE_LEG_RIGHT);
            IBone leftLegBone = this.modelDispatcher.getBone(BONE_LEG_LEFT);
            IBone rightBootBone = this.modelDispatcher.getBone(BONE_BOOT_RIGHT);
            IBone leftBootBone = this.modelDispatcher.getBone(BONE_BOOT_LEFT);
            try {
                GeoUtils.copyRotations(defaultArmor.bipedHead, headBone);
                GeoUtils.copyRotations(defaultArmor.bipedBody, bodyBone);
                GeoUtils.copyRotations(defaultArmor.bipedRightArm, rightArmBone);
                GeoUtils.copyRotations(defaultArmor.bipedLeftArm, leftArmBone);
                GeoUtils.copyRotations(defaultArmor.bipedRightLeg, rightLegBone);
                GeoUtils.copyRotations(defaultArmor.bipedLeftLeg, leftLegBone);
                GeoUtils.copyRotations(defaultArmor.bipedRightLeg, rightBootBone);
                GeoUtils.copyRotations(defaultArmor.bipedLeftLeg, leftBootBone);

                headBone.setPositionX(defaultArmor.bipedHead.rotationPointX);
                headBone.setPositionY(-defaultArmor.bipedHead.rotationPointY);
                headBone.setPositionZ(defaultArmor.bipedHead.rotationPointZ);
                bodyBone.setPositionX(defaultArmor.bipedBody.rotationPointX);
                bodyBone.setPositionY(-defaultArmor.bipedBody.rotationPointY);
                bodyBone.setPositionZ(defaultArmor.bipedBody.rotationPointZ);

                rightArmBone.setPositionX(defaultArmor.bipedRightArm.rotationPointX + 5);
                rightArmBone.setPositionY(2 - defaultArmor.bipedRightArm.rotationPointY);
                rightArmBone.setPositionZ(defaultArmor.bipedRightArm.rotationPointZ);
                leftArmBone.setPositionX(defaultArmor.bipedLeftArm.rotationPointX - 5);
                leftArmBone.setPositionY(2 - defaultArmor.bipedLeftArm.rotationPointY);
                leftArmBone.setPositionZ(defaultArmor.bipedLeftArm.rotationPointZ);

                rightLegBone.setPositionX(defaultArmor.bipedRightLeg.rotationPointX + 2);
                rightLegBone.setPositionY(12 - defaultArmor.bipedRightLeg.rotationPointY);
                rightLegBone.setPositionZ(defaultArmor.bipedRightLeg.rotationPointZ);
                leftLegBone.setPositionX(defaultArmor.bipedLeftLeg.rotationPointX - 2);
                leftLegBone.setPositionY(12 - defaultArmor.bipedLeftLeg.rotationPointY);
                leftLegBone.setPositionZ(defaultArmor.bipedLeftLeg.rotationPointZ);
                rightBootBone.setPositionX(defaultArmor.bipedRightLeg.rotationPointX + 2);
                rightBootBone.setPositionY(12 - defaultArmor.bipedRightLeg.rotationPointY);
                rightBootBone.setPositionZ(defaultArmor.bipedRightLeg.rotationPointZ);
                leftBootBone.setPositionX(defaultArmor.bipedLeftLeg.rotationPointX - 2);
                leftBootBone.setPositionY(12 - defaultArmor.bipedLeftLeg.rotationPointY);
                leftBootBone.setPositionZ(defaultArmor.bipedLeftLeg.rotationPointZ);
            } catch (Exception e) {
                throw new RuntimeException("Could not find an armor bone.", e);
            }
        }
    }

    public GeoMetaArmorRenderer setCurrentItem(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot) {
        this.entityLiving = entityLiving;
        this.itemStack = itemStack;
        this.armorSlot = armorSlot;
        this.currentMetaArmor = (IGeoMetaArmor) Objects.requireNonNull(((ArmorMetaItem<?>) itemStack.getItem()).getItem(itemStack)).getArmorLogic();
        return this;
    }

    public final GeoMetaArmorRenderer applyEntityStats(ModelBiped defaultArmor) {
        this.isChild = defaultArmor.isChild;
        this.isSneak = defaultArmor.isSneak;
        this.isRiding = defaultArmor.isRiding;
        this.rightArmPose = defaultArmor.rightArmPose;
        this.leftArmPose = defaultArmor.leftArmPose;
        this.defaultArmor = defaultArmor;
        return this;
    }

    public GeoMetaArmorRenderer applySlot(EntityEquipmentSlot slot) {
        // To load the model
        // Cursed logic... took me 10 minute to debug
        modelDispatcher.getModel(modelDispatcher.getModelLocation(currentMetaArmor));
        try {
            IBone headBone = this.modelDispatcher.getBone(BONE_HEAD);
            IBone bodyBone = this.modelDispatcher.getBone(BONE_BODY);
            IBone rightArmBone = this.modelDispatcher.getBone(BONE_ARM_RIGHT);
            IBone leftArmBone = this.modelDispatcher.getBone(BONE_ARM_LEFT);
            IBone rightLegBone = this.modelDispatcher.getBone(BONE_LEG_RIGHT);
            IBone leftLegBone = this.modelDispatcher.getBone(BONE_LEG_LEFT);
            IBone rightBootBone = this.modelDispatcher.getBone(BONE_BOOT_RIGHT);
            IBone leftBootBone = this.modelDispatcher.getBone(BONE_BOOT_LEFT);

            headBone.setHidden(true);
            bodyBone.setHidden(true);
            rightArmBone.setHidden(true);
            leftArmBone.setHidden(true);
            rightLegBone.setHidden(true);
            leftLegBone.setHidden(true);
            rightBootBone.setHidden(true);
            leftBootBone.setHidden(true);

            switch (slot) {
                case HEAD -> headBone.setHidden(false);
                case CHEST -> {
                    bodyBone.setHidden(false);
                    rightArmBone.setHidden(false);
                    leftArmBone.setHidden(false);
                }
                case LEGS -> {
                    rightLegBone.setHidden(false);
                    leftLegBone.setHidden(false);
                }
                case FEET -> {
                    rightBootBone.setHidden(false);
                    leftBootBone.setHidden(false);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not find an armor bone.", e);
        }
        return this;
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public Integer getUniqueID(IGeoMetaArmor geoMetaArmor) {
        return Objects.hash(this.armorSlot, itemStack.getItem(), itemStack.getCount(),
                itemStack.hasTagCompound() ? itemStack.getTagCompound().toString() : 1,
                this.entityLiving.getUniqueID().toString());
    }

    private static class ModelDispatcher extends AnimatedGeoModel<IGeoMetaArmor> {

        @Override
        public ResourceLocation getModelLocation(IGeoMetaArmor geoMetaArmor) {
            return geoMetaArmor.modelRL();
        }

        @Override
        public ResourceLocation getTextureLocation(IGeoMetaArmor geoMetaArmor) {
            return geoMetaArmor.textureRL();
        }

        @Override
        public ResourceLocation getAnimationFileLocation(IGeoMetaArmor geoMetaArmor) {
            return geoMetaArmor.animationRL();
        }
    }
}
