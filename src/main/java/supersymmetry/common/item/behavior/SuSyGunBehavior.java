package supersymmetry.common.item.behavior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import supersymmetry.api.guns.SuSyAmmoType;
import supersymmetry.api.util.SuSyDamageSources;
import supersymmetry.client.renderer.particles.SusyParticleFlame;
import supersymmetry.common.guns.bullettypes.ExplosiveBullet;
import supersymmetry.common.guns.bullettypes.IncendiaryBullet;
import supersymmetry.common.guns.bullettypes.TracerBullet;
import supersymmetry.common.item.SuSyMetaItems;

public class SuSyGunBehavior implements IItemBehaviour {

    private final double muzzleVelocity;
    private final double spread;
    private final float damage;
    private final int cooldownTicks;
    private final List<GunFireMode> fireModes;
    private final int burstCount;
    private final int burstIntervalTicks;

    private static final String NBT_NEXT_SHOT = "SuSyGunNextShot";
    private static final String NBT_FIRE_MODE = "SuSyGunFireMode";
    public static final String NBT_AMMO_TYPE = "SuSyGunAmmoType";
    private static final boolean DEBUG_TRAJECTORY = false;

    private static final List<PendingBurst> PENDING_BURSTS = new ArrayList<>();
    private static boolean initialized = false;

    public SuSyGunBehavior(double muzzleVelocity, double spread, float damage,
                           int cooldownTicks, List<GunFireMode> fireModes, int burstCount,
                           int burstIntervalTicks) {
        if (fireModes == null || fireModes.isEmpty()) {
            throw new IllegalArgumentException("gun needs at least one fire mode");
        }
        List<GunFireMode> modes = new ArrayList<>();
        for (GunFireMode mode : fireModes) {
            Objects.requireNonNull(mode, "fireMode");
            if (modes.contains(mode)) {
                throw new IllegalArgumentException("duplicate fire mode " + mode);
            }
            modes.add(mode);
        }
        if (modes.contains(GunFireMode.BOLT_ACTION) && modes.size() > 1) {
            throw new IllegalArgumentException("BOLT_ACTION cannot be combined with other modes");
        }
        if (modes.contains(GunFireMode.BURST) != (burstCount > 1)) {
            throw new IllegalArgumentException(
                    "BURST needs burstCount > 1, guns without BURST need burstCount == 1 (got " +
                            modes + ", " + burstCount + ")");
        }

        this.muzzleVelocity = muzzleVelocity;
        this.spread = spread;
        this.damage = damage;
        this.cooldownTicks = Math.max(cooldownTicks, 0);
        this.fireModes = Collections.unmodifiableList(modes);
        this.burstCount = burstCount;
        this.burstIntervalTicks = burstIntervalTicks;
    }

    public static void init() {
        if (initialized) return;
        initialized = true;
        MinecraftForge.EVENT_BUS.register(new BurstHandler());
    }

    public static Builder gun(double muzzleVelocity, double spread, float damage, int cooldownTicks) {
        return new Builder(muzzleVelocity, spread, damage, cooldownTicks);
    }

    /// Fires a single bullet from the specified position
    public void fire(World world, Entity shooter, Vec3d firePos, double yaw, double pitch, SuSyAmmoType ammoType) {
        Vec3d currentPos = firePos;
        Vec3d oldPos = currentPos;
        yaw = yaw % 360;
        // projections of the total speed onto the X, Y and Z axes

        double bulletCheckSpeed = 1.0; // distance between checked spots

        double motionY = MathHelper.sin((float) (-pitch * 0.0174532922)) * bulletCheckSpeed;
        double motionX = -MathHelper.sin((float) (yaw * 0.0174532922)) *
                MathHelper.cos((float) (-pitch * 0.0174532922)) * bulletCheckSpeed;
        double motionZ = MathHelper.cos((float) (yaw * 0.0174532922)) *
                MathHelper.cos((float) (-pitch * 0.0174532922)) * bulletCheckSpeed;
        int i = 0;
        // total number of iterations is limited
        while (i < 10000) {
            currentPos = new Vec3d(currentPos.x + motionX, currentPos.y + motionY, currentPos.z + motionZ);
            motionX *= (1 - muzzleVelocity * 0.000001); // air resistance (tm)
            motionY -= 2 * (bulletCheckSpeed / muzzleVelocity); // gravity dropoff (tm)
            motionZ *= (1 - muzzleVelocity * 0.000001);
            // spawnWorkingParticles(world, currentPos); // debug, spawns particles in checked spots
            // check if we hit any blocks
            RayTraceResult rayTraceResult = world.rayTraceBlocks(oldPos, currentPos, false, true, false);
            List<EntityLivingBase> potentialHitEntities = world.getEntitiesWithinAABB(EntityLivingBase.class,
                    new AxisAlignedBB(oldPos, currentPos)); // check for entities that could have been hit in the BB
                                                            // between old and new pos
            if (!potentialHitEntities.isEmpty()) {
                for (EntityLivingBase potentialHitEntity : potentialHitEntities) {
                    if (potentialHitEntity != shooter || i > 1) { // prevent hitting yourself
                        RayTraceResult entityRTR = potentialHitEntity.getEntityBoundingBox().calculateIntercept(oldPos,
                                currentPos);
                        // this is used to check if the entity was actually hit and find the exact hit position
                        if (entityRTR != null) {
                            // check for obstructing blocks to prevent shooting through walls
                            if (world.rayTraceBlocks(oldPos, entityRTR.hitVec) == null) {

                                // this is kinda dumb :skull:
                                List<EntityLivingBase> hitEntities = world.getEntitiesWithinAABB(EntityLivingBase.class,
                                        new AxisAlignedBB(
                                                new Vec3d(entityRTR.hitVec.x - 0.0001, entityRTR.hitVec.y - 0.0001,
                                                        entityRTR.hitVec.z - 0.0001),
                                                new Vec3d(entityRTR.hitVec.x + 0.0001,
                                                        entityRTR.hitVec.y + 0.0001, entityRTR.hitVec.z + 0.0001)));

                                for (EntityLivingBase elb : hitEntities) {
                                    ammoType.onEntityHit(world, elb, entityRTR.hitVec);
                                    elb.attackEntityFrom(SuSyDamageSources.BULLET,
                                            (float) (10 * ammoType.damageMultiplier));
                                }

                            } else {
                                ammoType.onBlockHit(world, rayTraceResult,
                                        world.rayTraceBlocks(oldPos, currentPos, false, true, true).getBlockPos());
                                break;
                            }
                        }
                    }
                }
            }
            if (i > 2) {
                ammoType.spawnParticles(world, oldPos, currentPos, i / 10);
            }
            if (rayTraceResult != null) {
                ammoType.onBlockHit(world, rayTraceResult, rayTraceResult.getBlockPos().offset(rayTraceResult.sideHit));
                break;
            }
            oldPos = currentPos;
            i++;
        }
    }

    public SuSyAmmoType getCurrentAmmoType(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey(NBT_AMMO_TYPE)) {
            String name = tag.getString(NBT_AMMO_TYPE);
            for (SuSyAmmoType type : ammoTypes) {
                if (name.equals(type.name)) {
                    return type;
                }
            }
        }
        return null;
    }

    public GunFireMode getSelectedMode(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey(NBT_FIRE_MODE)) {
            int index = tag.getInteger(NBT_FIRE_MODE);
            if (index >= 0 && index < fireModes.size()) {
                return fireModes.get(index);
            }
        }
        return fireModes.getFirst();
    }

    public void cycleMode(EntityPlayer player, ItemStack stack) {
        if (fireModes.size() <= 1) return;
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        int current = tag.getInteger(NBT_FIRE_MODE);
        int next = (current + 1) % fireModes.size();
        tag.setInteger(NBT_FIRE_MODE, next);
        player.sendMessage(new TextComponentString("susy.gun.fire_mode." + fireModes.get(next).name().toLowerCase()));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ActionResult<ItemStack> success = ActionResult.newResult(EnumActionResult.SUCCESS, stack);
        ActionResult<ItemStack> fail = ActionResult.newResult(EnumActionResult.FAIL, stack);

        if (player.world.isRemote) {
            return fail;
        }

        if (player.isSneaking() && fireModes.size() > 1) {
            cycleMode(player, stack);
            return success;
        }

        boolean fireSuccess = tryFire(player, hand, stack);
        return fireSuccess ? success : fail;
    }

    private int burstDuration() {
        return (burstCount - 1) * burstIntervalTicks + 1;
    }

    private int cooldownFor(GunFireMode mode) {
        return mode == GunFireMode.BURST ? Math.max(cooldownTicks, burstDuration()) : cooldownTicks;
    }

    private boolean tryFire(EntityPlayer player, EnumHand hand, ItemStack stack) {
        long now = player.world.getTotalWorldTime();
        NBTTagCompound data = player.getEntityData();

        if (now < data.getLong(NBT_NEXT_SHOT)) return false;

        GunFireMode mode = getSelectedMode(stack);
        data.setLong(NBT_NEXT_SHOT, now + cooldownFor(mode));

        SuSyAmmoType ammoType = getCurrentAmmoType(stack);
        ammoType = (ammoType == null ? FULL_METAL_JACKET : ammoType);

        if (stack.getItem() == SuSyMetaItems.PISTOL.getStackForm().getItem() &&
                stack.getMetadata() == SuSyMetaItems.PISTOL.getStackForm().getMetadata()) {
            ammoType = RED_TRACER;
        } else if (stack.getItem() == SuSyMetaItems.RIFLE.getStackForm().getItem() &&
                stack.getMetadata() == SuSyMetaItems.RIFLE.getStackForm().getMetadata()) {
                    ammoType = GREEN_TRACER;
                } else
            if (stack.getItem() == SuSyMetaItems.SMG.getStackForm().getItem() &&
                    stack.getMetadata() == SuSyMetaItems.SMG.getStackForm().getMetadata()) {
                        ammoType = BLUE_TRACER;
                    } else
                if (stack.getItem() == SuSyMetaItems.SHOTGUN.getStackForm().getItem() &&
                        stack.getMetadata() == SuSyMetaItems.SHOTGUN.getStackForm().getMetadata()) {
                            ammoType = BUCKSHOT;
                        }
        shootOnce(player, hand, ammoType);

        if (mode == GunFireMode.BURST && burstCount > 1) {
            PENDING_BURSTS
                    .add(new PendingBurst(this, player, hand, stack, burstCount - 1, burstIntervalTicks, ammoType));
        }
        return true;
    }

    private void shootOnce(EntityPlayer player, EnumHand hand, SuSyAmmoType ammoType) {
        double yaw = player.rotationYaw % 360;
        // since the player shoots from the hand
        double handXOffset = -0.35 * MathHelper.cos((float) (yaw * 0.0174532922));
        double handZOffset = -0.35 * MathHelper.sin((float) (yaw * 0.0174532922));

        if (player.getPrimaryHand() == EnumHandSide.LEFT) {
            handXOffset *= -1;
            handZOffset *= -1;
        }
        if (hand == EnumHand.OFF_HAND) {
            handXOffset *= -1;
            handZOffset *= -1;
        }
        Random random = player.getRNG();
        for (int i = 0; i < ammoType.numPellets; i++) {
            double yawOffset = Math.clamp(random.nextGaussian(), -2, 2) * spread / 2;
            double pitchOffset = Math.clamp(random.nextGaussian(), -2, 2) * spread / 2;
            fire(player.getEntityWorld(), player,
                    new Vec3d(player.posX + handXOffset, player.posY + 1.45, player.posZ + handZOffset),
                    player.rotationYaw + yawOffset, player.rotationPitch + pitchOffset, ammoType);
        }
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("susy.gun.tooltip.damage", damage));
        lines.add(I18n.format("susy.gun.fire_mode." + getSelectedMode(itemStack).name().toLowerCase(Locale.ROOT)));
        if (fireModes.size() > 1) {
            lines.add(I18n.format("susy.gun.tooltip.switch_mode"));
        }
    }

    private static final class PendingBurst {

        final SuSyGunBehavior gun;
        final EntityPlayer player;
        final EnumHand hand;
        final Item item;
        final int meta;
        int shotsLeft;
        int ticksLeft;
        final SuSyAmmoType ammoType;

        PendingBurst(SuSyGunBehavior gun, EntityPlayer player, EnumHand hand, ItemStack stack,
                     int shotsLeft, int interval, SuSyAmmoType ammoType) {
            this.gun = gun;
            this.player = player;
            this.hand = hand;
            this.item = stack.getItem();
            this.meta = stack.getMetadata();
            this.shotsLeft = shotsLeft;
            this.ticksLeft = interval;
            this.ammoType = ammoType;
        }
    }

    public static final class BurstHandler {

        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || PENDING_BURSTS.isEmpty()) return;

            Iterator<PendingBurst> it = PENDING_BURSTS.iterator();
            while (it.hasNext()) {
                PendingBurst burst = it.next();
                ItemStack held = burst.player.getHeldItem(burst.hand);

                if (burst.player.isDead || held.isEmpty() || held.getItem() != burst.item ||
                        held.getMetadata() != burst.meta) {
                    it.remove();
                    continue;
                }

                if (--burst.ticksLeft > 0) continue;

                burst.gun.shootOnce(burst.player, burst.hand, burst.ammoType);
                if (--burst.shotsLeft <= 0) {
                    it.remove();
                } else {
                    burst.ticksLeft = burst.gun.burstIntervalTicks;
                }
            }
        }

        @SubscribeEvent
        public void onWorldUnload(WorldEvent.Unload event) {
            if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
                PENDING_BURSTS.clear();
            }
        }
    }

    public static final class Builder {

        private final double muzzleVelocity;
        private final double spread;
        private final float damage;
        private final int cooldownTicks;
        private int burstCount = 1;
        private int burstIntervalTicks = 1;
        private final List<GunFireMode> modes = new ArrayList<>();

        private Builder(double muzzleVelocity, double spread, float damage, int cooldownTicks) {
            this.muzzleVelocity = muzzleVelocity;
            this.spread = spread;
            this.damage = damage;
            this.cooldownTicks = cooldownTicks;
        }

        public Builder burst(int count, int intervalTicks) {
            this.burstCount = count;
            this.burstIntervalTicks = intervalTicks;
            return this;
        }

        public Builder modes(GunFireMode... fireModes) {
            this.modes.clear();
            Collections.addAll(this.modes, fireModes);
            return this;
        }

        public SuSyGunBehavior build() {
            List<GunFireMode> resolvedModes = new ArrayList<>(modes);
            if (resolvedModes.isEmpty()) {
                resolvedModes.add(burstCount > 1 ? GunFireMode.BURST : GunFireMode.SEMI_AUTO);
            }
            return new SuSyGunBehavior(muzzleVelocity, spread, damage,
                    cooldownTicks, resolvedModes, burstCount, burstIntervalTicks);
        }
    }

    @SideOnly(Side.CLIENT)
    public void spawnWorkingParticles(World world, Vec3d pos) { // debug
        SusyParticleFlame spark = new SusyParticleFlame(world,
                pos.x, pos.y, pos.z,
                0, 0, 0);
        Minecraft.getMinecraft().effectRenderer.addEffect(spark);
        // Minecraft.getMinecraft().effectRenderer.addEffect(new SusyParticleFlareSmoke(world, pos.x, pos.y, pos.z, 1,
        // 0, 1));
    }

    public static final SuSyAmmoType FULL_METAL_JACKET = new SuSyAmmoType(1.0, 1.0, 0.5, 1, "full_metal_jacket");
    public static final SuSyAmmoType HOLLOW_POINT = new SuSyAmmoType(1.1, 1.0, 1.0, 1, "hollow_point");
    public static final SuSyAmmoType ARMOR_PIERCING = new SuSyAmmoType(1.0, 3.0, 0.3, 1, "armor_piercing");
    public static final SuSyAmmoType SABOT = new SuSyAmmoType(0.1, 10.0, 0.95, 1, "sabot");
    public static final SuSyAmmoType BUCKSHOT = new SuSyAmmoType(0.5, 0.2, 1.0, 30, "buckshot");
    public static final SuSyAmmoType RED_TRACER = new TracerBullet(1.0, 1.0, 0.5, 1, 0.1f, 0.1f, "red_tracer");
    public static final SuSyAmmoType GREEN_TRACER = new TracerBullet(1.0, 1.0, 0.5, 0.1f, 1, 0.1f, "green_tracer");
    public static final SuSyAmmoType BLUE_TRACER = new TracerBullet(1.0, 1.0, 0.5, 0.1f, 0.1f, 1, "blue_tracer");
    public static final SuSyAmmoType INCENDIARY = new IncendiaryBullet(1.5, 0.9, "incendiary");
    public static final SuSyAmmoType ARMOR_PIERCING_INCENDIARY = new IncendiaryBullet(1.5, 2.5,
            "armor_piercing_incendiary");
    public static final SuSyAmmoType ARMOR_PIERCING_EXPLOSIVE = new ExplosiveBullet(1.0, 2.5,
            "armor_piercing_explosive");

    public static final List<SuSyAmmoType> ammoTypes = new ArrayList<>(List.of(
            FULL_METAL_JACKET,
            HOLLOW_POINT,
            ARMOR_PIERCING,
            SABOT,
            BUCKSHOT,
            RED_TRACER,
            GREEN_TRACER,
            BLUE_TRACER,
            INCENDIARY,
            ARMOR_PIERCING_INCENDIARY,
            ARMOR_PIERCING_EXPLOSIVE));
}
