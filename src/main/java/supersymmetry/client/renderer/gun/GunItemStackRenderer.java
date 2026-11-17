package supersymmetry.client.renderer.gun;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import software.bernie.geckolib3.file.GeoModelLoader;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import supersymmetry.Supersymmetry;
import supersymmetry.api.SusyLog;
import supersymmetry.common.item.SuSyMetaItems;

@SideOnly(Side.CLIENT)
public final class GunItemStackRenderer extends TileEntityItemStackRenderer implements IGeoRenderer<ItemStack> {

    public static final GunItemStackRenderer INSTANCE = new GunItemStackRenderer();

    private static final String RIGHT_ARM_BONE = "rightarm_pos";
    private static final String LEFT_ARM_BONE = "leftarm_pos";

    // rotation point of player arm
    private static final float ARM_PIVOT_X = 5f;
    private static final float ARM_PIVOT_Y = 2f;
    // corner of the arm with the smallest coordinates relative to the arm's rotation point
    private static final float ARM_BOX_MIN_X_RIGHT = -3f;
    private static final float ARM_BOX_MIN_X_LEFT = -1f;
    private static final float ARM_BOX_MIN_Y = -2f;
    private static final float ARM_BOX_MIN_Z = -2f;
    private static final float ITEM_CENTER = -0.5f;

    private static final GeoModelLoader MODEL_LOADER = new GeoModelLoader();
    private static final Map<MetaValueItem, GunModel> MODELS = new HashMap<>();

    private GunItemStackRenderer() {}

    public static void registerGuns() {
        register(SuSyMetaItems.SEMI_AUTO_RIFLE, "semi_auto_rifle", 1);
    }

    public static void register(MetaValueItem item, String name, float scale) {
        ResourceLocation model = new ResourceLocation(Supersymmetry.MODID, "guns/geo/" + name + ".geo.json");
        ResourceLocation texture = new ResourceLocation(Supersymmetry.MODID, "guns/textures/" + name + ".png");
        MODELS.put(item, new GunModel(model, texture, 180f, scale));
        item.getMetaItem().setTileEntityItemStackRenderer(INSTANCE);
    }

    private static GunModel getGun(ItemStack stack) {
        if (!(stack.getItem() instanceof MetaItem)) {
            return null;
        }
        MetaItem<?>.MetaValueItem valueItem = ((MetaItem<?>) stack.getItem()).getItem(stack);
        return valueItem == null ? null : MODELS.get(valueItem);
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        GunModel gun = getGun(stack);
        if (gun == null) {
            super.renderByItem(stack, partialTicks);
            return;
        }

        GeoModel model = gun.getModel();
        GeoBone gunBone = gun.getGunBone();
        if (model == null || gunBone == null) {
            return;
        }

        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(gun.texture);

        GlStateManager.translate(gunBone.getPositionX(), gunBone.getPositionY(), gunBone.getPositionZ());
        GlStateManager.rotate(gun.yaw, 0, 1, 0);
        GlStateManager.scale(gun.scale, gun.scale, gun.scale);

        render(model, stack, partialTicks, 1, 1, 1, 1);

        GlStateManager.popMatrix();
    }

    public static Matrix4f firstPersonArmTransform(ItemStack stack, EnumHandSide side, EnumHandSide primary,
                                                   float equipProgress) {
        GunModel gun = getGun(stack);
        if (gun == null) {
            return null;
        }

        Matrix4f target = gun.getArmTransform(side, primary, stack, equipProgress);
        if (target == null) {
            return null;
        }

        Matrix4f vanilla = vanillaArmTransform(side);
        vanilla.invert();
        target.mul(vanilla);
        return target;
    }

    @Override
    public ResourceLocation getTextureLocation(ItemStack stack) {
        GunModel gun = getGun(stack);
        return gun == null ? null : gun.texture;
    }

    @Override
    public GeoModelProvider getGeoModelProvider() {
        return null;
    }

    private static Matrix4f vanillaArmTransform(EnumHandSide side) {
        float sideMult = side == EnumHandSide.RIGHT ? 1 : -1;
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();

        translate(matrix, sideMult * 0.64000005f, -0.6f, -0.71999997f);
        rotateY(matrix, sideMult * 45f);
        translate(matrix, sideMult * -1, 3.6f, 3.5f);
        rotateZ(matrix, sideMult * 120f);
        rotateX(matrix, 200f);
        rotateY(matrix, sideMult * -135f);
        translate(matrix, sideMult * 5.6f, 0, 0);

        // RenderPlayer.renderRightArm / renderLeftArm, then ModelRenderer.render(0.0625F).
        translate(matrix, -sideMult * ARM_PIVOT_X / 16, ARM_PIVOT_Y / 16, 0);
        rotateZ(matrix, sideMult * (float) Math.toDegrees(0.1f));

        return matrix;
    }

    private static GeoModel loadModel(GunModel gun) {
        GeoModel model = MODEL_LOADER.loadModel(Minecraft.getMinecraft().getResourceManager(), gun.modelLocation);
        for (String boneName : new String[] { RIGHT_ARM_BONE, LEFT_ARM_BONE }) {
            model.getBone(boneName).ifPresent(bone -> bone.setHidden(true, true));
        }
        return model;
    }

    private static void collectArmMarkers(GeoBone bone, Matrix4f parent, Map<String, ArmMarker> markers) {
        Matrix4f matrix = new Matrix4f(parent);
        transform(matrix, bone);

        if (!bone.childCubes.isEmpty()) {
            GeoCube cube = bone.childCubes.get(0);
            Matrix4f marker = new Matrix4f(matrix);
            if (cube.pivot != null) {
                translate(marker, cube.pivot.x / 16, cube.pivot.y / 16, cube.pivot.z / 16);
            }
            if (cube.rotation != null) {
                rotate(marker, cube.rotation.x, cube.rotation.y, cube.rotation.z);
            }
            if (cube.pivot != null) {
                translate(marker, -cube.pivot.x / 16, -cube.pivot.y / 16, -cube.pivot.z / 16);
            }
            markers.put(bone.getName(), new ArmMarker(marker, cubeMin(cube)));
        }

        for (GeoBone child : bone.childBones) {
            collectArmMarkers(child, matrix, markers);
        }
    }

    // smallest corner of a cube
    private static Vector3f cubeMin(GeoCube cube) {
        Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        for (GeoQuad quad : cube.quads) {
            if (quad == null) {
                continue;
            }
            for (GeoVertex vertex : quad.vertices) {
                min.x = Math.min(min.x, vertex.position.x);
                min.y = Math.min(min.y, vertex.position.y);
                min.z = Math.min(min.z, vertex.position.z);
            }
        }
        return min;
    }

    private static void transform(Matrix4f matrix, GeoBone bone) {
        translate(matrix, -bone.getPositionX() / 16, bone.getPositionY() / 16, bone.getPositionZ() / 16);
        translate(matrix, bone.rotationPointX / 16, bone.rotationPointY / 16, bone.rotationPointZ / 16);
        rotate(matrix, bone.getRotationX(), bone.getRotationY(), bone.getRotationZ());
        scale(matrix, bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
        translate(matrix, -bone.rotationPointX / 16, -bone.rotationPointY / 16,
                -bone.rotationPointZ / 16);
    }

    private static void rotate(Matrix4f matrix, float x, float y, float z) {
        rotateZRadians(matrix, z);
        rotateYRadians(matrix, y);
        rotateXRadians(matrix, x);
    }

    private static void translate(Matrix4f matrix, float x, float y, float z) {
        Matrix4f translation = new Matrix4f();
        translation.setIdentity();
        translation.setTranslation(new Vector3f(x, y, z));
        matrix.mul(translation);
    }

    private static void rotateXRadians(Matrix4f matrix, float radians) {
        Matrix4f rotation = new Matrix4f();
        rotation.rotX(radians);
        matrix.mul(rotation);
    }

    private static void rotateYRadians(Matrix4f matrix, float radians) {
        Matrix4f rotation = new Matrix4f();
        rotation.rotY(radians);
        matrix.mul(rotation);
    }

    private static void rotateZRadians(Matrix4f matrix, float radians) {
        Matrix4f rotation = new Matrix4f();
        rotation.rotZ(radians);
        matrix.mul(rotation);
    }

    private static void rotateX(Matrix4f matrix, float degrees) {
        rotateXRadians(matrix, (float) Math.toRadians(degrees));
    }

    private static void rotateY(Matrix4f matrix, float degrees) {
        rotateYRadians(matrix, (float) Math.toRadians(degrees));
    }

    private static void rotateZ(Matrix4f matrix, float degrees) {
        rotateZRadians(matrix, (float) Math.toRadians(degrees));
    }

    private static void scale(Matrix4f matrix, float x, float y, float z) {
        Matrix4f scale = new Matrix4f();
        scale.setIdentity();
        scale.m00 = x;
        scale.m11 = y;
        scale.m22 = z;
        matrix.mul(scale);
    }

    private static Matrix4f itemPlacement(EnumHandSide primary, float equipProgress) {
        float sideMult = primary == EnumHandSide.RIGHT ? 1f : -1f;

        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();

        translate(matrix, sideMult * 0.56f, -0.52f + equipProgress * -0.6f, -0.72f);
        rotateY(matrix, sideMult * 45f);
        rotateY(matrix, sideMult * -45f);
        return matrix;
    }

    private static Matrix4f displayTransform(ItemStack stack, EnumHandSide primary) {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();

        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
        if (model == null) {
            return matrix;
        }

        ItemCameraTransforms.TransformType type = primary == EnumHandSide.RIGHT ?
                ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND :
                ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
        Pair<? extends IBakedModel, Matrix4f> perspective = model.handlePerspective(type);
        if (perspective != null && perspective.getRight() != null) {
            matrix.set(perspective.getRight());
        }

        if (primary != EnumHandSide.RIGHT) {
            matrix.m01 = -matrix.m01;
            matrix.m02 = -matrix.m02;
            matrix.m03 = -matrix.m03;
            matrix.m10 = -matrix.m10;
            matrix.m20 = -matrix.m20;
            matrix.m30 = -matrix.m30;
        }

        translate(matrix, ITEM_CENTER, ITEM_CENTER, ITEM_CENTER);
        return matrix;
    }

    private static final class GunModel {

        private final ResourceLocation modelLocation;
        private final ResourceLocation texture;
        private final float yaw;
        private final float scale;

        private final Map<String, ArmMarker> armMarkers = new HashMap<>();

        private GeoModel model;
        private GeoBone gunBone;
        private boolean loadFailed;

        private GunModel(ResourceLocation modelLocation, ResourceLocation texture, float yaw, float scale) {
            this.modelLocation = modelLocation;
            this.texture = texture;
            this.yaw = yaw;
            this.scale = scale;
        }

        private GeoModel getModel() {
            if (this.model == null && !this.loadFailed) {
                try {
                    this.model = loadModel(this);
                    this.gunBone = this.model.getBone("gun").orElse(null);
                    this.armMarkers.clear();
                    Matrix4f root = new Matrix4f();
                    root.setIdentity();
                    for (GeoBone bone : this.model.topLevelBones) {
                        collectArmMarkers(bone, root, this.armMarkers);
                    }
                } catch (RuntimeException e) {
                    this.loadFailed = true;
                    SusyLog.logger.error("Failed to load gun model {}", this.modelLocation, e);
                }
            }
            return this.model;
        }

        private GeoBone getGunBone() {
            return this.getModel() == null ? null : this.gunBone;
        }

        private Matrix4f getArmTransform(EnumHandSide side, EnumHandSide primary, ItemStack stack, float equipProgress) {
            if (this.getModel() == null || this.gunBone == null) {
                return null;
            }

            ArmMarker marker = this.armMarkers.get(side == EnumHandSide.RIGHT ? RIGHT_ARM_BONE : LEFT_ARM_BONE);
            if (marker == null) {
                return null;
            }

            float sideMult = side == EnumHandSide.RIGHT ? 1f : -1f ;

            // Everything the gun itself is drawn inside of, in the order ItemRenderer applies it.
            Matrix4f matrix = itemPlacement(primary, equipProgress);
            matrix.mul(displayTransform(stack, primary));
            translate(matrix, this.gunBone.getPositionX(), this.gunBone.getPositionY(), this.gunBone.getPositionZ());
            rotateY(matrix, this.yaw);
            scale(matrix, this.scale, this.scale, this.scale);
            matrix.mul(marker.matrix);
            // The arm is drawn as a box around its rotation point, so it is moved by the difference between the
            // smallest corners of the arm box and of the marker cube. That puts the arm exactly on the marker,
            // including bone and cube translations and rotations, and follows the marker if it is moved in the model.
            float armMinX = (sideMult > 0 ? ARM_BOX_MIN_X_RIGHT : ARM_BOX_MIN_X_LEFT) / 16;
            translate(matrix, marker.min.x - armMinX, marker.min.y - ARM_BOX_MIN_Y / 16,
                    marker.min.z - ARM_BOX_MIN_Z / 16);
            return matrix;
        }
    }

    private static final class ArmMarker {

        private final Matrix4f matrix;
        private final Vector3f min;

        private ArmMarker(Matrix4f matrix, Vector3f min) {
            this.matrix = matrix;
            this.min = min;
        }
    }
}
