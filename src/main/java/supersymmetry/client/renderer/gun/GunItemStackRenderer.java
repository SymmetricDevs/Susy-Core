package supersymmetry.client.renderer.gun;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.BufferUtils;

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
    private static final String IDLE_VIEW_BONE = "idle_view";
    private static final String SIGHT_VIEW_BONE = "sight_view";

    // rotation point of player arm
    private static final float ARM_PIVOT_X = 5f;
    private static final float ARM_PIVOT_Y = 2f;
    // corner of the arm with the smallest coordinates relative to the arm's rotation point
    private static final float ARM_BOX_MIN_X_RIGHT = -3f;
    private static final float ARM_BOX_MIN_X_LEFT = -1f;
    private static final float ARM_BOX_MIN_Y = -2f;
    private static final float ARM_BOX_MIN_Z = -2f;

    private static final GeoModelLoader MODEL_LOADER = new GeoModelLoader();
    private static final Map<MetaValueItem, GunModel> MODELS = new HashMap<>();
    // created on demand so the class can be loaded (and its matrix maths tested) without LWJGL
    private static FloatBuffer matrixBuffer;

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

    public static void renderFirstPerson(ItemStack stack, EnumHandSide primary, float equipProgress,
                                         float partialTicks, float aimProgress) {
        GunModel gun = getGun(stack);
        if (gun == null) {
            return;
        }

        Matrix4f pose = firstPersonPose(gun, equipProgress, aimProgress);
        if (pose == null) {
            return;
        }

        GlStateManager.pushMatrix();
        multMatrix(pose);
        Minecraft.getMinecraft().getTextureManager().bindTexture(gun.texture);
        INSTANCE.render(gun.getModel(), stack, partialTicks, 1, 1, 1, 1);
        GlStateManager.popMatrix();
    }

    public static boolean hasViewNodes(ItemStack stack) {
        GunModel gun = getGun(stack);
        return gun != null && gun.hasViews();
    }

    public static Matrix4f firstPersonArmTransform(ItemStack stack, EnumHandSide side,
                                                   float equipProgress, float aimProgress) {
        GunModel gun = getGun(stack);
        if (gun == null) {
            return null;
        }

        Matrix4f pose = firstPersonPose(gun, equipProgress, aimProgress);
        if (pose == null) {
            return null;
        }

        Matrix4f target = gun.getArmTransform(side, pose);
        if (target == null) {
            return null;
        }

        Matrix4f vanilla = vanillaArmTransform(side);
        vanilla.invert();
        target.mul(vanilla);
        return target;
    }

    private static Matrix4f firstPersonPose(GunModel gun, float equipProgress,
                                            float aimProgress) {
        Matrix4f view = gun.getViewPose(aimProgress);
        if (view == null) {
            return null;
        }

        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();

        translate(matrix, 0, equipProgress * -0.6f, 0);

        matrix.mul(view);
        return matrix;
    }

    public static void multMatrix(Matrix4f matrix) {
        if (matrixBuffer == null) {
            matrixBuffer = BufferUtils.createFloatBuffer(16);
        }
        matrixBuffer.clear();
        float[] column = new float[4];
        for (int i = 0; i < 4; i++) {
            matrix.getColumn(i, column);
            matrixBuffer.put(column);
        }
        matrixBuffer.flip();
        GlStateManager.multMatrix(matrixBuffer);
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

    private static void collectNodes(GeoBone bone, Matrix4f parent, Map<String, ArmMarker> markers,
                                     Map<String, ViewNode> views) {
        Matrix4f matrix = new Matrix4f(parent);
        transform(matrix, bone);

        if (!bone.childCubes.isEmpty()) {
            GeoCube cube = bone.childCubes.get(0);
            Matrix4f marker = new Matrix4f(matrix);
            transform(marker, cube);
            markers.put(bone.getName(), new ArmMarker(marker, cubeMin(cube)));
        }

        if (IDLE_VIEW_BONE.equals(bone.getName()) || SIGHT_VIEW_BONE.equals(bone.getName())) {
            // The camera sits on the bone's pivot, looking where the bone points. A bone with no rotation
            // points back up the -Z axis, which is away from the gun, so a node that has not been rotated
            // is not usable as a view node; it still gives a position, so fall back to looking along +Z.
            Vector3f position = nodePosition(matrix, bone);
            Matrix3f rotation = new Matrix3f();
            matrix.getRotationScale(rotation);
            rotation.normalize();
            boolean authored = !isIdentity(rotation);
            views.put(bone.getName(), new ViewNode(position, authored ? rotation : forwardRotation(), authored));
        }

        for (GeoBone child : bone.childBones) {
            collectNodes(child, matrix, markers, views);
        }
    }

    static Vector3f nodePosition(Matrix4f matrix, GeoBone bone) {
        Point3f pivot = new Point3f(bone.rotationPointX / 16, bone.rotationPointY / 16,
                bone.rotationPointZ / 16);
        Point3f origin = new Point3f();
        matrix.transform(pivot, origin);
        return new Vector3f(origin.x, origin.y, origin.z);
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

    static void transform(Matrix4f matrix, GeoBone bone) {
        translate(matrix, -bone.getPositionX() / 16, bone.getPositionY() / 16, bone.getPositionZ() / 16);
        translate(matrix, bone.rotationPointX / 16, bone.rotationPointY / 16, bone.rotationPointZ / 16);
        rotate(matrix, bone.getRotationX(), bone.getRotationY(), bone.getRotationZ());
        scale(matrix, bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
        translate(matrix, -bone.rotationPointX / 16, -bone.rotationPointY / 16,
                -bone.rotationPointZ / 16);
    }

    static void transform(Matrix4f matrix, GeoCube cube) {
        if (cube.pivot != null) {
            translate(matrix, cube.pivot.x / 16, cube.pivot.y / 16, cube.pivot.z / 16);
        }
        if (cube.rotation != null) {
            // GeoCube rotations are already radians; only bone rotations are degrees in the model file
            rotateZRadians(matrix, cube.rotation.z);
            rotateYRadians(matrix, cube.rotation.y);
            rotateXRadians(matrix, cube.rotation.x);
        }
        if (cube.pivot != null) {
            translate(matrix, -cube.pivot.x / 16, -cube.pivot.y / 16, -cube.pivot.z / 16);
        }
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

    private static Matrix3f interpolate(Matrix3f from, Matrix3f to, float progress) {
        Quat4f start = new Quat4f();
        start.set(from);
        Quat4f end = new Quat4f();
        end.set(to);
        if (start.x * end.x + start.y * end.y + start.z * end.z + start.w * end.w < 0) {
            end.negate();
        }

        Quat4f mixed = new Quat4f();
        mixed.interpolate(start, end, progress);
        mixed.normalize();

        Matrix3f rotation = new Matrix3f();
        rotation.set(mixed);
        return rotation;
    }

    private static boolean isIdentity(Matrix3f matrix) {
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                float expected = column == row ? 1f : 0f;
                if (Math.abs(matrix.getElement(column, row) - expected) > 1.0e-6f) {
                    return false;
                }
            }
        }
        return true;
    }

    // camera basis that looks along +Z, the way gun models face in the model file
    private static Matrix3f forwardRotation() {
        Matrix3f rotation = new Matrix3f();
        rotation.rotY((float) Math.PI);
        return rotation;
    }

    private static final class GunModel {

        private final ResourceLocation modelLocation;
        private final ResourceLocation texture;
        private final float yaw;
        private final float scale;

        private final Map<String, ArmMarker> armMarkers = new HashMap<>();

        private ViewNode idleView;
        private ViewNode sightView;

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
                    Map<String, ViewNode> views = new HashMap<>();
                    Matrix4f root = new Matrix4f();
                    root.setIdentity();
                    for (GeoBone bone : this.model.topLevelBones) {
                        collectNodes(bone, root, this.armMarkers, views);
                    }
                    this.idleView = views.get(IDLE_VIEW_BONE);
                    this.sightView = views.get(SIGHT_VIEW_BONE);
                    if (this.idleView != null && !this.idleView.authored) {
                        warnUnauthored(IDLE_VIEW_BONE);
                    }
                    if (this.sightView != null && !this.sightView.authored) {
                        warnUnauthored(SIGHT_VIEW_BONE);
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

        private void warnUnauthored(String boneName) {
            SusyLog.logger.warn(
                    "Gun model {}: the {} bone has no rotation, so its camera has no direction. " +
                            "In blockbench rotate that bone 180 degrees around Y so it looks down the barrel.",
                    this.modelLocation, boneName);
        }

        private boolean hasViews() {
            return this.getModel() != null && this.idleView != null;
        }

        /**
         * Camera space transform for the model: the inverse of the view node, scaled down to the gun's display size.
         * Aiming slides and turns the model between the two view nodes, so the camera ends up on the sight node.
         */
        private Matrix4f getViewPose(float aimProgress) {
            if (!this.hasViews()) {
                return null;
            }

            float progress = MathHelper.clamp(aimProgress, 0f, 1f);
            ViewNode target = this.sightView == null ? this.idleView : this.sightView;

            Vector3f position = new Vector3f();
            position.interpolate(this.idleView.position, target.position, progress);
            Matrix3f rotation = target == this.idleView ? this.idleView.rotation :
                    interpolate(this.idleView.rotation, target.rotation, progress);

            // the scale only dollies the model around the eye, so the view stays the same either way
            return viewPose(position, rotation, this.scale);
        }

        private Matrix4f getArmTransform(EnumHandSide side, Matrix4f pose) {
            ArmMarker marker = this.armMarkers.get(side == EnumHandSide.RIGHT ? RIGHT_ARM_BONE : LEFT_ARM_BONE);
            if (marker == null) {
                return null;
            }

            float sideMult = side == EnumHandSide.RIGHT ? 1f : -1f;

            Matrix4f matrix = new Matrix4f(pose);
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

    // inverse of the camera frame (position and rotation) in model space, scaled to display size
    private static Matrix4f viewPose(Vector3f position, Matrix3f rotation, float scale) {
        Matrix3f inverse = new Matrix3f(rotation);
        inverse.transpose();

        Matrix4f pose = new Matrix4f();
        pose.setIdentity();
        pose.setRotation(inverse);

        Vector3f offset = new Vector3f(-position.x, -position.y, -position.z);
        inverse.transform(offset);
        pose.setTranslation(offset);

        scale(pose, scale, scale, scale);
        return pose;
    }

    private static final class ViewNode {

        private final Vector3f position;
        private final Matrix3f rotation;
        private final boolean authored;

        private ViewNode(Vector3f position, Matrix3f rotation, boolean authored) {
            this.position = position;
            this.rotation = rotation;
            this.authored = authored;
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
