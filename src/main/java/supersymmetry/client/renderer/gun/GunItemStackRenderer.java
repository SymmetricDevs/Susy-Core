package supersymmetry.client.renderer.gun;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
import supersymmetry.common.item.behavior.SuSyGunBehavior;

@SideOnly(Side.CLIENT)
public final class GunItemStackRenderer extends TileEntityItemStackRenderer implements IGeoRenderer<ItemStack> {

    public static final GunItemStackRenderer INSTANCE = new GunItemStackRenderer();

    private static final String RIGHT_ARM_BONE = "rightarm_pos";
    private static final String LEFT_ARM_BONE = "leftarm_pos";
    private static final String IDLE_VIEW_BONE = "idle_view";
    private static final String SIGHT_VIEW_BONE = "sight_view";
    private static final String ANIMATION_DIRECTORY = "guns/animations/";
    private static final String MAGAZINE_PREFIX = "magazine_";
    private static final String MAGAZINE_INSERTED_SUFFIX = "_inserted";

    public static final ResourceLocation MUZZLE_FLASH_SPRITE = new ResourceLocation(Supersymmetry.MODID,
            "particle/muzzle_flash");
    private static final String MUZZLE_FLASH_EFFECT = "muzzle_flash";
    private static final float MUZZLE_FLASH_SIZE = 0.4f;
    private static final int MUZZLE_FLASH_TICKS = 1;

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
    private static final Set<String> WARNED_EFFECTS = new HashSet<>();
    private static final List<MuzzleFlash> MUZZLE_FLASHES = new ArrayList<>();
    private static FloatBuffer matrixBuffer;

    private GunItemStackRenderer() {}

    public static void registerGuns() {
        register(SuSyMetaItems.FLINTLOCK_PISTOL, "flintlock_pistol", 1);
        register(SuSyMetaItems.MUSKET, "musket", 1);
        register(SuSyMetaItems.BLUNDERBUSS, "blunderbuss", 1);
        register(SuSyMetaItems.REVOLVER, "revolver", 1);
        register(SuSyMetaItems.BREECHLOADING_RIFLE, "breechloading_rifle", 1);
        register(SuSyMetaItems.DOUBLE_BARREL_SHOTGUN, "double_barrel_shotgun", 1);
        register(SuSyMetaItems.BASIC_PISTOL, "basic_pistol", 1);
        register(SuSyMetaItems.BOLT_ACTION_RIFLE, "bolt_action_rifle", 1);
        register(SuSyMetaItems.PUMP_SHOTGUN, "pump_shotgun", 1);
        register(SuSyMetaItems.IMPROVED_PISTOL, "improved_pistol", 1);
        register(SuSyMetaItems.SEMI_AUTO_RIFLE, "semi_auto_rifle", 1);
        register(SuSyMetaItems.BASIC_SMG, "basic_smg", 1);
        register(SuSyMetaItems.ADVANCED_PISTOL, "advanced_pistol", 1);
        register(SuSyMetaItems.AUTOMATIC_BATTLE_RIFLE, "automatic_battle_rifle", 1);
        register(SuSyMetaItems.SEMI_AUTO_SHOTGUN, "semi_auto_shotgun", 1);
        register(SuSyMetaItems.ADVANCED_SMG, "advanced_smg", 1);
        register(SuSyMetaItems.BASIC_ASSAULT_RIFLE, "basic_assault_rifle", 1);
        register(SuSyMetaItems.BASIC_SNIPER_RIFLE, "basic_sniper_rifle", 1);
        register(SuSyMetaItems.HEAVY_PISTOL, "heavy_pistol", 1);
        register(SuSyMetaItems.BASIC_DMR, "basic_dmr", 1);
        register(SuSyMetaItems.AUTO_SHOTGUN, "auto_shotgun", 1);
        register(SuSyMetaItems.IMPROVED_ASSAULT_RIFLE, "improved_assault_rifle", 1);
        register(SuSyMetaItems.IMPROVED_SNIPER_RIFLE, "improved_sniper_rifle", 1);
        register(SuSyMetaItems.BASIC_ANTI_MATERIEL_RIFLE, "basic_anti_materiel_rifle", 1);
        register(SuSyMetaItems.BASIC_LMG, "basic_lmg", 1);
        register(SuSyMetaItems.IMPROVED_DMR, "improved_dmr", 1);
        register(SuSyMetaItems.PDW, "pdw", 1);
        register(SuSyMetaItems.ADVANCED_ASSAULT_RIFLE, "advanced_assault_rifle", 1);
        register(SuSyMetaItems.ADVANCED_SNIPER_RIFLE, "advanced_sniper_rifle", 1);
        register(SuSyMetaItems.IMPROVED_ANTI_MATERIEL_RIFLE, "improved_anti_materiel_rifle", 1);
        register(SuSyMetaItems.IMPROVED_LMG, "improved_lmg", 1);
        register(SuSyMetaItems.BASIC_GPMG, "basic_gpmg", 1);
    }

    public static void register(MetaValueItem item, String name, float scale) {
        ResourceLocation model = new ResourceLocation(Supersymmetry.MODID, "guns/geo/" + name + ".geo.json");
        ResourceLocation texture = new ResourceLocation(Supersymmetry.MODID, "guns/textures/" + name + ".png");
        ResourceLocation animations = new ResourceLocation(Supersymmetry.MODID,
                ANIMATION_DIRECTORY + name + ".animation.json");
        MODELS.put(item, new GunModel(model, texture, animations, name + ".", 180f, scale));
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
            super.renderByItem(stack, partialTicks);
            return;
        }

        // the first person pass may have left the bones in an animated pose; inventory and third person models are
        // shared with it, so they always start from the authored pose
        gun.resetAnimations();
        gun.applyMagazine(stack, null, 0f, 0);

        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(gun.texture);

        GlStateManager.translate(gunBone.getPositionX(), gunBone.getPositionY(), gunBone.getPositionZ());
        GlStateManager.rotate(gun.yaw, 0, 1, 0);
        GlStateManager.scale(gun.scale, gun.scale, gun.scale);

        render(model, stack, partialTicks, 1, 1, 1, 1);

        GlStateManager.popMatrix();
    }

    public static void renderFirstPerson(ItemStack stack, EnumHandSide primary,
                                         float partialTicks, float aimProgress,
                                         String animation, float animationTicks, int pendingMagazine) {
        GunModel gun = getGun(stack);
        if (gun == null) {
            return;
        }

        Matrix4f pose = firstPersonPose(gun, aimProgress);
        if (pose == null) {
            return;
        }

        // the pose is handed to the animation so an authored effect can turn its locator into camera space
        gun.applyAnimation(animation, animationTicks, pose, partialTicks);
        gun.applyMagazine(stack, animation, animationTicks, pendingMagazine);

        GlStateManager.pushMatrix();
        multMatrix(pose);
        // GeckoLib bakes the bone matrices into the vertices, so a translation left in the shared buffer would move
        // the gun and the flash differently. Reset it before both draws, while the pose matrix is still active.
        Tessellator.getInstance().getBuffer().setTranslation(0.0D, 0.0D, 0.0D);
        Minecraft.getMinecraft().getTextureManager().bindTexture(gun.texture);
        INSTANCE.render(gun.getModel(), stack, partialTicks, 1, 1, 1, 1);

        // effects author their own locators; drawing them in the gun's matrix keeps them on the muzzle at any FOV
        renderMuzzleFlashes(gun, partialTicks);
        GlStateManager.popMatrix();
    }

    public static float animationLength(ItemStack stack, String animation) {
        GunModel gun = getGun(stack);
        return gun == null || animation == null ? 0f : gun.animationLength(animation);
    }

    public static boolean hasViewNodes(ItemStack stack) {
        GunModel gun = getGun(stack);
        return gun != null && gun.hasViews();
    }

    /**
     * Magazine drawn this frame: the one the gun has installed, or the one the reload is inserting from the
     * {@code switch_mag} timeline instruction on. No magazine shows nothing.
     */
    static int displayedMagazineSize(int installed, GunAnimation clip, float ticks, int pendingMagazine) {
        if (pendingMagazine <= 0) {
            return installed;
        }

        if (clip != null && clip.hasInstruction(GunAnimation.SWITCH_MAG, ticks)) {
            // from the switch on, the magazine the reload is inserting is the one drawn
            return pendingMagazine;
        }
        if (clip == null && installed > 0 && installed != pendingMagazine) {
            // the reload clip just ended and the gun's inventory may not have caught up with it yet, so keep
            // drawing the incoming magazine until the installed one matches it
            return pendingMagazine;
        }
        return installed;
    }

    public static Matrix4f firstPersonArmTransform(ItemStack stack, EnumHandSide side,
                                                   float aimProgress, String animation, float animationTicks) {
        GunModel gun = getGun(stack);
        if (gun == null) {
            return null;
        }

        // the arms are posed after the gun in the same frame, so this pass only updates the bones; effect events
        // already fired in renderFirstPerson and the animation remembers that
        gun.applyAnimation(animation, animationTicks, null, 0f);

        Matrix4f pose = firstPersonPose(gun, aimProgress);
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

    private static Matrix4f firstPersonPose(GunModel gun, float aimProgress) {
        Matrix4f view = gun.getViewPose(aimProgress);
        if (view == null) {
            return null;
        }

        // The view node already anchors the gun to the eye. Vanilla's equip progress (the hand sliding up after a
        // slot change) and swing progress are deliberately not applied here: they would move the gun and arms away
        // from the authored pose, and an NBT-only change such as consuming a round makes Forge restart the equip
        // animation, which would drop both arms on every shot. A view-node gun is always fully equipped.
        return new Matrix4f(view);
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

    private static void collectBoneNames(GeoBone bone, Map<String, GeoBone> names) {
        names.put(bone.getName(), bone);
        for (GeoBone child : bone.childBones) {
            collectBoneNames(child, names);
        }
    }

    /** World matrix of a bone from the model root. A fresh Matrix4f is zero, so the parent must be set to identity. */
    static Matrix4f boneWorldMatrix(GeoModel model, String boneName) {
        Matrix4f root = new Matrix4f();
        root.setIdentity();
        return boneMatrix(root, model.topLevelBones, boneName);
    }

    // world matrix of the bone called name, or null when the model has no such bone
    private static Matrix4f boneMatrix(Matrix4f parent, List<GeoBone> bones, String boneName) {
        for (GeoBone bone : bones) {
            Matrix4f matrix = new Matrix4f(parent);
            transform(matrix, bone);
            if (boneName.equals(bone.getName())) {
                return matrix;
            }
            Matrix4f childMatrix = boneMatrix(matrix, bone.childBones, boneName);
            if (childMatrix != null) {
                return childMatrix;
            }
        }
        return null;
    }

    // the part of an effect name after the namespace, which is the name vanilla particles are registered under
    private static String particleName(String effect) {
        if (effect == null) {
            return "";
        }
        int index = effect.lastIndexOf(':');
        return index < 0 ? effect : effect.substring(index + 1);
    }

    /**
     * Sound name from an animation file as a resource location. A name without a namespace belongs to this mod, the
     * same way GeckoLib treats animation sound names.
     */
    static ResourceLocation soundLocation(String sound) {
        if (sound == null || sound.trim().isEmpty()) {
            return null;
        }
        try {
            return sound.indexOf(':') >= 0 ? new ResourceLocation(sound) :
                    new ResourceLocation(Supersymmetry.MODID, sound);
        } catch (RuntimeException e) {
            SusyLog.logger.warn("Gun animation uses an invalid sound name {}", sound, e);
            return null;
        }
    }

    /**
     * Locators of a geometry file, keyed by their name. GeckoLib's model classes do not keep the {@code locators}
     * block, so it is read here: every locator belongs to a bone and follows it when the bone animates.
     */
    static Map<String, Locator> readLocators(IResourceManager manager, ResourceLocation location) {
        Map<String, Locator> locators = new HashMap<>();
        try (IResource resource = manager.getResource(location);
                Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            JsonElement geometries = root.get("minecraft:geometry");
            if (geometries == null || !geometries.isJsonArray()) {
                return locators;
            }

            for (JsonElement geometry : geometries.getAsJsonArray()) {
                if (!geometry.isJsonObject()) {
                    continue;
                }
                JsonElement bones = geometry.getAsJsonObject().get("bones");
                if (bones == null || !bones.isJsonArray()) {
                    continue;
                }

                for (JsonElement boneElement : bones.getAsJsonArray()) {
                    if (!boneElement.isJsonObject()) {
                        continue;
                    }
                    JsonObject bone = boneElement.getAsJsonObject();
                    JsonElement name = bone.get("name");
                    JsonElement entries = bone.get("locators");
                    if (name == null || entries == null || !entries.isJsonObject()) {
                        continue;
                    }

                    for (Map.Entry<String, JsonElement> entry : entries.getAsJsonObject().entrySet()) {
                        Vector3f position = readPosition(entry.getValue());
                        if (position != null) {
                            locators.put(entry.getKey(), new Locator(name.getAsString(), position));
                        }
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            SusyLog.logger.warn("Failed to read gun locators from {}", location, e);
        }
        return locators;
    }

    private static Vector3f readPosition(JsonElement element) {
        if (element == null || !element.isJsonArray() || element.getAsJsonArray().size() < 3) {
            return null;
        }

        JsonArray array = element.getAsJsonArray();
        try {
            // Model coordinates are in sixteenths of a block, the unit the model loader uses for cubes. The loader
            // mirrors X so the model faces the way GeckoLib renders it, so a locator has to be mirrored as well to
            // land on the cube it was authored on.
            return new Vector3f(-array.get(0).getAsFloat() / 16f, array.get(1).getAsFloat() / 16f,
                    array.get(2).getAsFloat() / 16f);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Turns a point in first person camera space into a world position. The gun is drawn directly in camera space,
     * with +X to the right, +Y up and -Z along the view direction, so the player's eye and look rotation place it in
     * the world.
     */
    static Vec3d worldPosition(Point3f cameraPosition, float partialTicks) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) {
            return null;
        }

        double eyeX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double eyeY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks + player.getEyeHeight();
        double eyeZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;

        float yaw = (float) Math.toRadians(
                player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks);
        float pitch = (float) Math.toRadians(
                player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks);
        float sinYaw = MathHelper.sin(yaw);
        float cosYaw = MathHelper.cos(yaw);
        float sinPitch = MathHelper.sin(pitch);
        float cosPitch = MathHelper.cos(pitch);

        // camera axes in world space: screen right, screen up, and the -Z axis of camera space
        Vec3d right = new Vec3d(-cosYaw, 0, -sinYaw);
        Vec3d up = new Vec3d(-sinYaw * sinPitch, cosPitch, cosYaw * sinPitch);
        Vec3d forward = new Vec3d(-sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);

        return new Vec3d(
                eyeX + right.x * cameraPosition.x + up.x * cameraPosition.y - forward.x * cameraPosition.z,
                eyeY + right.y * cameraPosition.x + up.y * cameraPosition.y - forward.y * cameraPosition.z,
                eyeZ + right.z * cameraPosition.x + up.z * cameraPosition.y - forward.z * cameraPosition.z);
    }

    /**
     * Queues an effect that belongs to the gun in first person, so it is drawn with the gun at the locator the
     * animation file asks for. The locator is resolved again every frame, so the effect follows the bone it was
     * authored on while the gun recoils. Returns false for effects that have to be spawned in the world instead.
     */
    private static boolean queueMuzzleFlash(GunModel gun, String effect, String locator, float partialTicks) {
        if (!MUZZLE_FLASH_EFFECT.equals(particleName(effect))) {
            return false;
        }

        World world = Minecraft.getMinecraft().world;
        if (world != null) {
            MUZZLE_FLASHES.add(new MuzzleFlash(gun, locator, world.getTotalWorldTime() + partialTicks));
        }
        return true;
    }

    private static void spawnWorldEffect(String effect, Vec3d position, float partialTicks) {
        World world = Minecraft.getMinecraft().world;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (world == null || player == null) {
            return;
        }

        String name = particleName(effect);
        EnumParticleTypes type = EnumParticleTypes.getByName(name);
        if (type == null) {
            if (WARNED_EFFECTS.add(effect)) {
                SusyLog.logger.warn("Gun animation uses the particle effect {}, but the gun has no effect and no " +
                        "vanilla particle is registered under that name", effect);
            }
            return;
        }

        Vec3d direction = player.getLook(partialTicks);
        world.spawnParticle(type, position.x, position.y, position.z, direction.x * 0.02, direction.y * 0.02,
                direction.z * 0.02);
    }

    /**
     * Draws the queued muzzle flashes in the gun's current matrix. Every flash resolves its locator again, so a model
     * space point like the gun's vertices follows the animated muzzle, and the caller's pose puts it on the barrel
     * tip.
     */
    private static void renderMuzzleFlashes(GunModel gun, float partialTicks) {
        if (MUZZLE_FLASHES.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.world;
        if (world == null) {
            MUZZLE_FLASHES.clear();
            return;
        }

        double now = world.getTotalWorldTime() + partialTicks;
        for (Iterator<MuzzleFlash> iterator = MUZZLE_FLASHES.iterator(); iterator.hasNext();) {
            MuzzleFlash flash = iterator.next();
            float age = (float) (now - flash.startTicks) / MUZZLE_FLASH_TICKS;
            if (age < 0f || age >= 1f) {
                iterator.remove();
            }
        }
        if (MUZZLE_FLASHES.isEmpty()) {
            return;
        }

        TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(MUZZLE_FLASH_SPRITE.toString());

        GlStateManager.pushMatrix();
        boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
        int blendSourceRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        int blendDestinationRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int blendSourceAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        int blendDestinationAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean depthWrite = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        // Keep the depth test so the barrel can hide the flash, but do not let one effect hide the next one.
        GlStateManager.enableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        // the gun's pose is still on the modelview stack, so the muzzle point needs no extra transform here
        buffer.setTranslation(0.0D, 0.0D, 0.0D);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        for (MuzzleFlash flash : MUZZLE_FLASHES) {
            if (flash.gun != gun) {
                continue;
            }

            Vector3f position = flash.position();
            if (position == null) {
                continue;
            }

            float age = (float) (now - flash.startTicks) / MUZZLE_FLASH_TICKS;
            float alpha = 1f - age;
            float half = MUZZLE_FLASH_SIZE * (1f - 0.35f * age) / 2f;

            Point3f center = new Point3f(position);

            buffer.pos(center.x - half, center.y - half, center.z).tex(sprite.getMinU(), sprite.getMinV())
                    .color(1f, 1f, 1f, alpha).endVertex();
            buffer.pos(center.x - half, center.y + half, center.z).tex(sprite.getMinU(), sprite.getMaxV())
                    .color(1f, 1f, 1f, alpha).endVertex();
            buffer.pos(center.x + half, center.y + half, center.z).tex(sprite.getMaxU(), sprite.getMaxV())
                    .color(1f, 1f, 1f, alpha).endVertex();
            buffer.pos(center.x + half, center.y - half, center.z).tex(sprite.getMaxU(), sprite.getMinV())
                    .color(1f, 1f, 1f, alpha).endVertex();
        }
        tessellator.draw();

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.depthMask(depthWrite);
        if (depthTest) {
            GlStateManager.enableDepth();
        } else {
            GlStateManager.disableDepth();
        }
        // the arms enable blending after the flash, so the previous blend function has to be back in place
        GlStateManager.tryBlendFuncSeparate(blendSourceRgb, blendDestinationRgb, blendSourceAlpha,
                blendDestinationAlpha);
        if (blend) {
            GlStateManager.enableBlend();
        } else {
            GlStateManager.disableBlend();
        }
        GlStateManager.popMatrix();
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
        private final ResourceLocation animationLocation;
        private final String animationPrefix;
        private final float yaw;
        private final float scale;

        private final Map<String, ArmMarker> armMarkers = new HashMap<>();
        private final Map<String, GunAnimation> animations = new HashMap<>();
        private final Map<String, GeoBone> bonesByName = new HashMap<>();
        private final Map<String, Locator> locators = new HashMap<>();
        private final Set<String> warnedLocators = new HashSet<>();
        private final Map<Integer, GeoBone> magazineBones = new HashMap<>();
        private final Map<Integer, GeoBone> insertedMagazineBones = new HashMap<>();

        private ViewNode idleView;
        private ViewNode sightView;

        private GeoModel model;
        private GeoBone gunBone;
        private GunAnimation rest;
        private boolean loadFailed;

        // first person pose and frame time of the apply that is currently running, for turning effect locators
        // into world positions when an effect is not drawn with the gun
        private Matrix4f effectPose;
        private float effectPartialTicks;

        private GunModel(ResourceLocation modelLocation, ResourceLocation texture, ResourceLocation animationLocation,
                         String animationPrefix, float yaw, float scale) {
            this.modelLocation = modelLocation;
            this.texture = texture;
            this.animationLocation = animationLocation;
            this.animationPrefix = animationPrefix;
            this.yaw = yaw;
            this.scale = scale;
        }

        private GeoModel getModel() {
            if (this.model == null && !this.loadFailed) {
                try {
                    this.model = loadModel(this);
                    this.gunBone = this.model.getBone("gun").orElse(null);
                    this.armMarkers.clear();
                    this.bonesByName.clear();
                    this.locators.clear();
                    this.locators.putAll(readLocators(Minecraft.getMinecraft().getResourceManager(),
                            this.modelLocation));
                    Map<String, ViewNode> views = new HashMap<>();
                    Matrix4f root = new Matrix4f();
                    root.setIdentity();
                    for (GeoBone bone : this.model.topLevelBones) {
                        collectNodes(bone, root, this.armMarkers, views);
                        collectBoneNames(bone, this.bonesByName);
                    }
                    this.magazineBones.clear();
                    this.insertedMagazineBones.clear();
                    for (GeoBone bone : this.bonesByName.values()) {
                        String name = bone.getName();
                        if (name == null || !name.startsWith(MAGAZINE_PREFIX)) {
                            continue;
                        }
                        String capacity = name.substring(MAGAZINE_PREFIX.length());
                        boolean inserted = capacity.endsWith(MAGAZINE_INSERTED_SUFFIX);
                        String value = inserted ?
                                capacity.substring(0, capacity.length() - MAGAZINE_INSERTED_SUFFIX.length()) : capacity;
                        try {
                            int size = Integer.parseInt(value);
                            (inserted ? this.insertedMagazineBones : this.magazineBones).put(size, bone);
                        } catch (NumberFormatException ignored) {
                            // a bone named after a prefix that is not a capacity cannot be a magazine variant
                        }
                    }
                    this.idleView = views.get(IDLE_VIEW_BONE);
                    this.sightView = views.get(SIGHT_VIEW_BONE);
                    if (this.idleView != null && !this.idleView.authored) {
                        warnUnauthored(IDLE_VIEW_BONE);
                    }
                    if (this.sightView != null && !this.sightView.authored) {
                        warnUnauthored(SIGHT_VIEW_BONE);
                    }
                    // the model is in its authored pose right here, which is the rest pose the animations reset to
                    this.loadAnimations();
                } catch (RuntimeException e) {
                    this.loadFailed = true;
                    SusyLog.logger.error("Failed to load gun model {}", this.modelLocation, e);
                }
            }
            return this.model;
        }

        private void loadAnimations() {
            try {
                this.animations.clear();
                this.animations.putAll(GunAnimation.loadAll(Minecraft.getMinecraft().getResourceManager(),
                        this.animationLocation, this.animationPrefix, this.model));
            } catch (RuntimeException e) {
                SusyLog.logger.error("Failed to load gun animations from {}", this.animationLocation, e);
            }
            // captured now, while the model still has the authored pose every clip resets to
            this.rest = GunAnimation.rest(this.model);
        }

        private float animationLength(String name) {
            GunAnimation animation = this.getModel() == null ? null : this.animations.get(name);
            return animation == null ? 0f : animation.length();
        }

        private void applyAnimation(String name, float ticks, Matrix4f pose, float partialTicks) {
            if (this.getModel() == null) {
                return;
            }

            this.resetAnimations();
            GunAnimation animation = name == null ? null : this.animations.get(name);
            if (isPlaying(animation, ticks)) {
                this.effectPose = pose;
                this.effectPartialTicks = partialTicks;
                animation.setParticleListener(pose == null ? null : this::summonParticle);
                animation.setSoundListener(this::playSound);
                animation.apply(ticks);
                this.effectPose = null;
            }

            this.refreshNodes();
        }

        // animation-driven sounds fire here; the animation file decides the sound and the time it happens at
        private void playSound(GunAnimation.SoundKeyFrame event) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP player = mc.player;
            if (player == null || mc.world == null || event == null) {
                return;
            }

            ResourceLocation location = soundLocation(event.sound);
            if (location != null) {
                player.playSound(new SoundEvent(location), 1.0F, 1.0F);
            }
        }

        // animation-driven particle effects fire here; the animation file decides the effect, the locator and the
        // time it happens at, so the actual spawn stays in one place
        private void summonParticle(GunAnimation.ParticleEvent event) {
            Matrix4f pose = this.effectPose;
            if (pose == null || this.getModel() == null) {
                return;
            }

            Vector3f modelPosition = locatorPosition(event.locator);
            if (modelPosition == null) {
                return;
            }

            // effects that belong to the gun are drawn with it and keep tracking the locator while they fade; the rest
            // are world particles
            if (queueMuzzleFlash(this, event.effect, event.locator, this.effectPartialTicks)) {
                return;
            }

            Point3f cameraPosition = new Point3f(modelPosition);
            pose.transform(cameraPosition);
            Vec3d spawnPosition = worldPosition(cameraPosition, this.effectPartialTicks);
            if (spawnPosition != null) {
                spawnWorldEffect(event.effect, spawnPosition, this.effectPartialTicks);
            }
        }

        /**
         * Position of an animation locator in the model's authored space. Locators live under a bone in the model
         * file and follow that bone; a bone name is accepted as well so a locator can simply be the bone pivot.
         */
        private Vector3f locatorPosition(String name) {
            GeoModel model = this.getModel();
            if (model == null || name == null || name.isEmpty()) {
                return null;
            }

            Locator locator = this.locators.get(name);
            if (locator != null) {
                Matrix4f matrix = boneWorldMatrix(model, locator.bone);
                if (matrix == null) {
                    return null;
                }
                Point3f position = new Point3f(locator.position);
                matrix.transform(position);
                return new Vector3f(position.x, position.y, position.z);
            }

            GeoBone bone = this.bonesByName.get(name);
            if (bone == null) {
                if (this.warnedLocators.add(name)) {
                    SusyLog.logger.warn("Gun model {}: the animation refers to a locator named {}, but the model " +
                            "has neither a locator nor a bone with that name", this.modelLocation, name);
                }
                return null;
            }

            Matrix4f matrix = boneWorldMatrix(model, name);
            return matrix == null ? null : nodePosition(matrix, bone);
        }

        // arm markers and views are cached from the rest pose; the markers have to follow the animated bones
        private void refreshNodes() {
            this.armMarkers.clear();
            Matrix4f root = new Matrix4f();
            root.setIdentity();
            Map<String, ViewNode> ignored = new HashMap<>();
            for (GeoBone bone : this.model.topLevelBones) {
                collectNodes(bone, root, this.armMarkers, ignored);
            }
        }

        private void resetAnimations() {
            if (this.rest != null) {
                this.rest.reset();
            }
        }

        /**
         * Hides every magazine variant of the model but the one the current stack has installed, or none when the gun
         * has no magazine at all. A gun fed without a detachable magazine only shows its magazine bone while a clip
         * is playing, so an authored reload can still pose it. The hidden state survives animation resets, so a swap
         * inside a reload clip can hide and show the variants again without the animation doing anything.
         */
        void applyMagazine(ItemStack stack, String clip, float ticks, int pendingMagazine) {
            if (this.magazineBones.isEmpty() && this.insertedMagazineBones.isEmpty()) {
                // the model has no magazine variants to switch between
                return;
            }

            GunAnimation animation = clip == null ? null : this.animations.get(clip);
            if (usesInternalMagazine(stack) && !isPlaying(animation, ticks)) {
                // integral magazines and stripper clips have no detachable magazine to draw, so their bone is only
                // shown while a clip is playing and can pose it
                hideMagazines();
                return;
            }

            int installed = installedMagazineSize(stack);
            int size = displayedMagazineSize(installed, animation, ticks, pendingMagazine);
            boolean inserted = pendingMagazine > 0 && size == pendingMagazine && pendingMagazine != installed;
            GeoBone selected = size > 0 ? (inserted ? insertedBone(size) : installedBone(size)) : null;
            if (selected == null && size > 0) {
                // every modelled magazine size of the gun accepted, but the exact size has no bone of its own
                selected = closestMagazineBone(inserted ? this.insertedMagazineBones : this.magazineBones, size);
            }
            for (GeoBone bone : this.magazineBones.values()) {
                bone.setHidden(bone != selected, true);
            }
            for (GeoBone bone : this.insertedMagazineBones.values()) {
                bone.setHidden(bone != selected, true);
            }
        }

        /** Bone of the magazine the gun currently has installed, by capacity. */
        private GeoBone installedBone(int size) {
            GeoBone bone = this.magazineBones.get(size);
            return bone == null && size > 0 ? closestMagazineBone(this.magazineBones, size) : bone;
        }

        /** Bone of the magazine a reload inserts, by capacity, named after the inserted variant. */
        private GeoBone insertedBone(int size) {
            GeoBone bone = this.insertedMagazineBones.get(size);
            return bone == null && size > 0 ? closestMagazineBone(this.insertedMagazineBones, size) : bone;
        }

        /** Capacity the gun currently has in its magazine, or zero when it has none. */
        private int installedMagazineSize(ItemStack stack) {
            NBTTagCompound tag = stack.getTagCompound();
            int size = tag == null ? 0 : tag.getInteger(SuSyGunBehavior.NBT_MAGAZINE_SIZE);
            if (size > 0) {
                return size;
            }

            // integral magazines read their capacity from the gun behavior instead of the magazine NBT
            SuSyGunBehavior gun = SuSyMetaItems.getGunBehaviour(stack);
            if (gun != null && !usesInternalMagazine(gun)) {
                return gun.getMaxMagazineSize();
            }
            return 0;
        }

        private static boolean usesInternalMagazine(ItemStack stack) {
            return usesInternalMagazine(SuSyMetaItems.getGunBehaviour(stack));
        }

        private static boolean usesInternalMagazine(SuSyGunBehavior gun) {
            if (gun == null) {
                return false;
            }
            SuSyGunBehavior.GunReloadMode mode = gun.getReloadMode();
            return mode == SuSyGunBehavior.GunReloadMode.INTEGRAL_MAGAZINE ||
                    mode == SuSyGunBehavior.GunReloadMode.STRIPPER_CLIP;
        }

        /** Whether the clip exists and its playhead is inside it, the same range the animation is applied in. */
        private static boolean isPlaying(GunAnimation animation, float ticks) {
            return animation != null && ticks >= 0f && ticks < animation.length();
        }

        private void hideMagazines() {
            for (GeoBone bone : this.magazineBones.values()) {
                bone.setHidden(true, true);
            }
            for (GeoBone bone : this.insertedMagazineBones.values()) {
                bone.setHidden(true, true);
            }
        }

        /**
         * Fills in the variant a capacity without its own bone should draw, so a gun can never end up without a
         * magazine because the author forgot one of its sizes.
         */
        private GeoBone closestMagazineBone(Map<Integer, GeoBone> bones, int size) {
            int bestSize = Integer.MAX_VALUE;
            GeoBone best = null;
            for (Map.Entry<Integer, GeoBone> entry : bones.entrySet()) {
                int candidate = entry.getKey();
                if (candidate <= size && candidate > bestSize) {
                    best = entry.getValue();
                    bestSize = candidate;
                }
            }
            if (best != null) {
                return best;
            }

            bestSize = Integer.MAX_VALUE;
            for (Map.Entry<Integer, GeoBone> entry : bones.entrySet()) {
                int candidate = entry.getKey();
                if (candidate < bestSize) {
                    best = entry.getValue();
                    bestSize = candidate;
                }
            }
            return best;
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

    /**
     * A point authored under a bone's {@code locators} block, in model space. It follows the bone it belongs to.
     */
    static final class Locator {

        final String bone;
        final Vector3f position;

        private Locator(String bone, Vector3f position) {
            this.bone = bone;
            this.position = position;
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

    // a muzzle flash waiting to be drawn: the animating locator it follows, and when it started
    private static final class MuzzleFlash {

        private final GunModel gun;
        private final String locator;
        private final double startTicks;

        private MuzzleFlash(GunModel gun, String locator, double startTicks) {
            this.gun = gun;
            this.locator = locator;
            this.startTicks = startTicks;
        }

        // resolving the locator again each frame keeps the flash on the muzzle while the bone animates
        private Vector3f position() {
            return this.gun.locatorPosition(this.locator);
        }
    }
}
