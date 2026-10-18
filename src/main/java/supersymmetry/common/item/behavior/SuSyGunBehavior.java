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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import supersymmetry.api.util.SuSyDamageSources;
import supersymmetry.client.renderer.particles.SusyParticleFlame;

public class SuSyGunBehavior implements IItemBehaviour {

    private final double muzzleVelocity;
    private final double spread;
    private final int pellets;
    private final float damage;
    private final int cooldownTicks;
    private final List<GunFireMode> fireModes;
    private final int burstCount;
    private final int burstIntervalTicks;

    private final double bulletCheckSpeed = 1.0; // not meant to be set in constructor
    private static final String NBT_NEXT_SHOT = "SuSyGunNextShot";
    private static final String NBT_FIRE_MODE = "SuSyGunFireMode";
    private static final boolean DEBUG_TRAJECTORY = false;

    private static final List<PendingBurst> PENDING_BURSTS = new ArrayList<>();
    private static boolean initialized = false;

    public SuSyGunBehavior(double muzzleVelocity, double spread, int pellets, float damage,
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
        this.pellets = pellets;
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
    public boolean fire(World world, Vec3d firePos, double yaw, double pitch) {
        Vec3d currentPos = firePos;
        Vec3d oldPos = currentPos;
        yaw = yaw % 360;
        // projections of the total speed onto the X, Y and Z axes
        double motionY = MathHelper.sin((float) (-pitch * 0.0174532922)) * bulletCheckSpeed;
        double motionX = -MathHelper.sin((float) (yaw * 0.0174532922)) *
                MathHelper.cos((float) (-pitch * 0.0174532922)) * bulletCheckSpeed;
        double motionZ = MathHelper.cos((float) (yaw * 0.0174532922)) *
                MathHelper.cos((float) (-pitch * 0.0174532922)) * bulletCheckSpeed;
        int i = 0;
        // total number of iterations is limited
        while (i < 100000) {
            currentPos = new Vec3d(currentPos.x + motionX, currentPos.y + motionY, currentPos.z + motionZ);
            motionX *= 0.99900000095367432D; // air resistance (tm)
            motionY -= 0.002348776121; // gravity dropoff, random number yay
            motionZ *= 0.99900000095367432D;
            spawnWorkingParticles(world, currentPos); // debug, spawns particles in checked spots
            // check if we hit any blocks
            RayTraceResult rayTraceResult = world.rayTraceBlocks(oldPos, currentPos, false, true, false);
            List<EntityLivingBase> potentialHitEntities = world.getEntitiesWithinAABB(EntityLivingBase.class,
                    new AxisAlignedBB(oldPos, currentPos)); // check for entities that could have been hit in the BB between old and new pos
            if (!potentialHitEntities.isEmpty()) {
                for (EntityLivingBase potentialHitEntity : potentialHitEntities) {
                    RayTraceResult entityRTR = potentialHitEntity.getEntityBoundingBox().calculateIntercept(oldPos,
                            currentPos); // this is used to find the exact hit position
                    if (entityRTR != null) {
                        if (world.rayTraceBlocks(oldPos, entityRTR.hitVec) == null) { // check for obstructing blocks to
                                                                                      // prevent shooting through walls

                            // this is kinda dumb :skull:
                            List<EntityLivingBase> hitEntities = world.getEntitiesWithinAABB(EntityLivingBase.class,
                                    new AxisAlignedBB(entityRTR.hitVec, new Vec3d(entityRTR.hitVec.x + 0.0001,
                                            entityRTR.hitVec.y + 0.0001, entityRTR.hitVec.z + 0.0001)));

                            for (EntityLivingBase elb : hitEntities) {
                                elb.attackEntityFrom(SuSyDamageSources.BULLET, 10f);
                            }
                        }
                    }
                }
            }
            if (rayTraceResult != null) {
                break; // if we hit a block
            }
            oldPos = currentPos;
            i++;
        }
        return true;
    }

    public GunFireMode getSelectedMode(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey(NBT_FIRE_MODE)) {
            int index = tag.getInteger(NBT_FIRE_MODE);
            if (index >= 0 && index < fireModes.size()) {
                return fireModes.get(index);
            }
        }
        return fireModes.get(0);
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
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ActionResult<ItemStack> result = ActionResult.newResult(EnumActionResult.SUCCESS, stack);

        if (player.world.isRemote) {
            return result;
        }

        if (player.isSneaking() && fireModes.size() > 1) {
            cycleMode(player, stack);
            return result;
        }

        tryFire(player, hand, stack);
        return result;
    }

    private int burstDuration() {
        return (burstCount - 1) * burstIntervalTicks + 1;
    }

    private int cooldownFor(GunFireMode mode) {
        return mode == GunFireMode.BURST ? Math.max(cooldownTicks, burstDuration()) : cooldownTicks;
    }

    private void tryFire(EntityPlayer player, EnumHand hand, ItemStack stack) {
        long now = player.world.getTotalWorldTime();
        NBTTagCompound data = player.getEntityData();

        if (now < data.getLong(NBT_NEXT_SHOT)) return;

        GunFireMode mode = getSelectedMode(stack);
        data.setLong(NBT_NEXT_SHOT, now + cooldownFor(mode));

        shootOnce(player, hand);

        if (mode == GunFireMode.BURST && burstCount > 1) {
            PENDING_BURSTS.add(new PendingBurst(this, player, hand, stack, burstCount - 1, burstIntervalTicks));
        }
    }

    private void shootOnce(EntityPlayer player, EnumHand hand) {
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
        for (int i = 0; i < pellets; i++) {
            double yawOffset = MathHelper.clamp(random.nextGaussian(), -2.0, 2.0) * spread / 2;
            double pitchOffset = MathHelper.clamp(random.nextGaussian(), -2.0, 2.0) * spread / 2;
            fire(player.world, new Vec3d(player.posX + handXOffset, player.posY + 1.45, player.posZ + handZOffset),
                    player.rotationYaw + yawOffset, player.rotationPitch + pitchOffset);
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

        PendingBurst(SuSyGunBehavior gun, EntityPlayer player, EnumHand hand, ItemStack stack,
                     int shotsLeft, int interval) {
            this.gun = gun;
            this.player = player;
            this.hand = hand;
            this.item = stack.getItem();
            this.meta = stack.getMetadata();
            this.shotsLeft = shotsLeft;
            this.ticksLeft = interval;
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

                burst.gun.shootOnce(burst.player, burst.hand);
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
        private int pellets = 1;
        private int burstCount = 1;
        private int burstIntervalTicks = 1;
        private final List<GunFireMode> modes = new ArrayList<>();

        private Builder(double muzzleVelocity, double spread, float damage, int cooldownTicks) {
            this.muzzleVelocity = muzzleVelocity;
            this.spread = spread;
            this.damage = damage;
            this.cooldownTicks = cooldownTicks;
        }

        public Builder pellets(int pellets) {
            this.pellets = pellets;
            return this;
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
            return new SuSyGunBehavior(muzzleVelocity, spread, pellets, damage,
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
}
