package supersymmetry.common.item.behavior;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import lombok.Getter;
import supersymmetry.api.guns.SuSyAmmoType;
import supersymmetry.api.input.SuSyKeyBind;
import supersymmetry.api.util.SuSyDamageSources;
import supersymmetry.client.renderer.particles.SusyParticleFlame;
import supersymmetry.common.guns.bullettypes.ExplosiveBullet;
import supersymmetry.common.guns.bullettypes.IncendiaryBullet;
import supersymmetry.common.guns.bullettypes.TracerBullet;
import supersymmetry.common.item.SuSyMetaItems;

public class SuSyGunBehavior implements IItemBehaviour, IItemDurabilityManager {

    private final double muzzleVelocity;
    private final double spread;
    private final float damage;
    private final int cooldownTicks;
    private final List<GunFireMode> fireModes;
    @Getter
    private final int burstCount;
    @Getter
    private final int burstIntervalTicks;
    @Getter
    private final int maxMagazineSize;
    @Getter
    private final int reloadTicks;
    @Getter
    private final String caliber;
    @Getter
    private final boolean beltFed;
    @Getter
    private final boolean integralMagazine;
    @Getter
    private final float recoilStrength;

    private static final String NBT_FIRE_MODE = "SuSyGunFireMode";
    public static final String NBT_AMMO = "SuSyGunAmmo";
    public static final String NBT_AMMO_TYPE = "SuSyGunAmmoType";
    public static final String NBT_CALIBER = "SuSyCaliber";
    public static final String NBT_MAGAZINE_SIZE = "SuSyMagazineSize";

    private static final boolean DEBUG_TRAJECTORY = false;

    public SuSyGunBehavior(double muzzleVelocity, double spread, float damage,
                           int cooldownTicks, List<GunFireMode> fireModes, int burstCount,
                           int burstIntervalTicks, int maxMagazineSize, int reloadTicks, String caliber,
                           boolean beltFed, boolean integralMagazine, float recoilStrength) {
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
        if (reloadTicks < 0) {
            throw new IllegalArgumentException("reloadTicks must be >= 0");
        }

        this.muzzleVelocity = muzzleVelocity;
        this.spread = spread;
        this.damage = damage;
        this.cooldownTicks = Math.max(cooldownTicks, 0);
        this.fireModes = Collections.unmodifiableList(modes);
        this.burstCount = burstCount;
        this.burstIntervalTicks = Math.max(burstIntervalTicks, 1);
        this.maxMagazineSize = Math.max(maxMagazineSize, 0);
        this.reloadTicks = reloadTicks;
        this.caliber = caliber;
        this.beltFed = beltFed;
        this.integralMagazine = integralMagazine;
        this.recoilStrength = recoilStrength;
    }

    public static void init() {}

    public static Builder gun(double muzzleVelocity, double spread, float damage, int cooldownTicks, String caliber) {
        return new Builder(muzzleVelocity, spread, damage, cooldownTicks, caliber);
    }

    public int getCooldownTicks(GunFireMode mode) {
        return mode == GunFireMode.BURST ? Math.max(cooldownTicks, burstDuration()) : cooldownTicks;
    }

    private int burstDuration() {
        return (burstCount - 1) * burstIntervalTicks + 1;
    }

    // magazine

    public int getAmmo(ItemStack stack) {
        if (maxMagazineSize <= 0) return Integer.MAX_VALUE;
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(NBT_AMMO)) return 0;
        return tag.getInteger(NBT_AMMO);
    }

    public boolean isFull(ItemStack stack) {
        if (!stack.hasTagCompound()) return false;
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey(NBT_AMMO) || !tag.hasKey(NBT_MAGAZINE_SIZE)) return false;
        return tag.getInteger(NBT_MAGAZINE_SIZE) <= 0 || getAmmo(stack) >= tag.getInteger(NBT_MAGAZINE_SIZE);
    }

    public boolean consumeRound(ItemStack stack) {
        if (maxMagazineSize <= 0) return true;
        int ammo = getAmmo(stack);
        if (ammo <= 0) return false;
        getOrCreateTag(stack).setInteger(NBT_AMMO, ammo - 1);
        return true;
    }

    public void reload(EntityPlayer player, ItemStack gun, ItemStack magazine) {
        unload(player, gun);
        if (!magazine.hasTagCompound()) return;
        NBTTagCompound magTag = magazine.getTagCompound();
        SuSyGunBehavior gunBehavior = SuSyMetaItems.getGunBehaviour(gun);
        if (!gunBehavior.isIntegralMagazine()) {
            if (!magTag.hasKey(NBT_AMMO) || !magTag.hasKey(NBT_AMMO_TYPE) || !magTag.hasKey(NBT_CALIBER)) return;
            if (GunMagazineBehavior.getMagBehavior(magazine) == null) return;
            GunMagazineBehavior magBehavior = GunMagazineBehavior.getMagBehavior(magazine);
            int ammo = magTag.getInteger(NBT_AMMO);
            String caliber = magTag.getString(NBT_CALIBER);
            if (SuSyMetaItems.getGunBehaviour(gun) == null) return;
            if (!SuSyMetaItems.getGunBehaviour(gun).getCaliber().equals(caliber)) return;
            String ammoType = magTag.getString(NBT_AMMO_TYPE);
            NBTTagCompound gunTag = getOrCreateTag(gun);
            gunTag.setInteger(NBT_AMMO, ammo);
            gunTag.setString(NBT_AMMO_TYPE, ammoType);
            gunTag.setInteger(NBT_MAGAZINE_SIZE, magBehavior.getMaxCapacity());
            magazine.shrink(1);
        } else {
            if (!magTag.hasKey(NBT_CALIBER)) return;
            if (!magTag.getString(NBT_CALIBER).equals(gunBehavior.getCaliber())) return;
            NBTTagCompound gunTag = getOrCreateTag(gun);
            int currentAmmo = gunBehavior.getAmmo(gun);
            int ammo = magazine.getCount() + currentAmmo;
            if (ammo > gunBehavior.getMaxMagazineSize()) ammo = gunBehavior.getMaxMagazineSize();
            gunTag.setInteger(NBT_AMMO, ammo);
            gunTag.setString(NBT_AMMO_TYPE, FULL_METAL_JACKET.name);
            gunTag.setInteger(NBT_MAGAZINE_SIZE, gunBehavior.getMaxMagazineSize());
            magazine.shrink(gunBehavior.getMaxMagazineSize() - currentAmmo);
        }
    }

    public void unload(EntityPlayer player, ItemStack gun) {
        if (!gun.hasTagCompound()) return;
        NBTTagCompound gunTag = gun.getTagCompound();
        SuSyGunBehavior gunBehavior = SuSyMetaItems.getGunBehaviour(gun);
        if (gunBehavior == null) return;
        if (!gunBehavior.isIntegralMagazine()) {
            if (!gunTag.hasKey(NBT_AMMO) || !gunTag.hasKey(NBT_AMMO_TYPE) || !gunTag.hasKey(NBT_MAGAZINE_SIZE)) return;
            String caliber = gunBehavior.getCaliber();
            int ammo = gunTag.getInteger(NBT_AMMO);
            String ammoType = gunTag.getString(NBT_AMMO_TYPE);
            int magSize = gunTag.getInteger(NBT_MAGAZINE_SIZE);
            if (!gunBehavior.isBeltFed() || ammo > 0) {
                ItemStack mag = GunMagazineBehavior.getMagFromSize(magSize);
                if (mag == null) return;
                NBTTagCompound magTag = getOrCreateTag(mag);
                if (ammo > 0) {
                    magTag.setString(NBT_CALIBER, caliber);
                    magTag.setString(NBT_AMMO_TYPE, ammoType);
                }
                magTag.setInteger(NBT_AMMO, ammo);

                if (!player.inventory.addItemStackToInventory(mag)) {
                    EntityItem dropped = new EntityItem(player.world, player.posX, player.posY, player.posZ, mag);
                    player.world.spawnEntity(dropped);
                }
            }
            gunTag.removeTag(NBT_AMMO_TYPE);
            gunTag.removeTag(NBT_AMMO);
            gunTag.removeTag(NBT_MAGAZINE_SIZE);
        } else {
            if (!gunTag.hasKey(NBT_AMMO)) return;
            String caliber = gunBehavior.getCaliber();
            int ammo = gunTag.getInteger(NBT_AMMO);
            if (ammo == 0) return;
            ItemStack bullet = GunMagazineBehavior.getBulletFromCaliber(caliber, ammo);
            if (bullet == null) return;
            if (!player.inventory.addItemStackToInventory(bullet)) {
                EntityItem dropped = new EntityItem(player.world, player.posX, player.posY, player.posZ, bullet);
                player.world.spawnEntity(dropped);
            }
            gunTag.removeTag(NBT_AMMO_TYPE);
            gunTag.removeTag(NBT_AMMO);
            gunTag.removeTag(NBT_MAGAZINE_SIZE);
        }
    }

    public static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        if (!getOrCreateTag(itemStack).hasKey(NBT_AMMO) || !getOrCreateTag(itemStack).hasKey(NBT_MAGAZINE_SIZE))
            return 0;
        return (double) getOrCreateTag(itemStack).getInteger(NBT_AMMO) /
                getOrCreateTag(itemStack).getInteger(NBT_MAGAZINE_SIZE);
    }

    /// Fires a single bullet from the specified position
    public void fire(World world, Entity shooter, Vec3d firePos, double yaw, double pitch, SuSyAmmoType ammoType) {
        if (world.isRemote) return;
        Vec3d currentPos = firePos;
        Vec3d oldPos = currentPos;
        yaw = yaw % 360;

        double bulletCheckSpeed = 1.0; // distance between checked spots

        // projections of the total speed onto the X, Y and Z axes
        double motionY = MathHelper.sin((float) (-pitch * 0.0174532922)) * bulletCheckSpeed;
        double motionX = -MathHelper.sin((float) (yaw * 0.0174532922)) *
                MathHelper.cos((float) (-pitch * 0.0174532922)) * bulletCheckSpeed;
        double motionZ = MathHelper.cos((float) (yaw * 0.0174532922)) *
                MathHelper.cos((float) (-pitch * 0.0174532922)) * bulletCheckSpeed;
        int i = 0;
        // total number of iterations is limited
        while (i < 2000) {
            currentPos = new Vec3d(currentPos.x + motionX, currentPos.y + motionY, currentPos.z + motionZ);
            motionX *= (1 - muzzleVelocity * 0.000001); // air resistance (tm)
            motionY -= 2 * (bulletCheckSpeed / muzzleVelocity); // gravity dropoff (tm)
            motionZ *= (1 - muzzleVelocity * 0.000001);
            spawnWorkingParticles(world, currentPos); // debug, spawns particles in checked spots
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

    public void shootOnce(EntityPlayer player, EnumHand hand, ItemStack stack, boolean aim) {
        SuSyAmmoType ammoType = getCurrentAmmoType(stack);
        if (ammoType == null)
            ammoType = FULL_METAL_JACKET;

        double yaw = player.rotationYaw % 360;

        double handXOffset = 0.0;
        double handZOffset = 0.0;

        if (!aim) {
            handXOffset = -0.35 * MathHelper.cos((float) (yaw * 0.0174532922));
            handZOffset = -0.35 * MathHelper.sin((float) (yaw * 0.0174532922));

            if (player.getPrimaryHand() == EnumHandSide.LEFT) {
                handXOffset *= -1;
                handZOffset *= -1;
            }
            if (hand == EnumHand.OFF_HAND) {
                handXOffset *= -1;
                handZOffset *= -1;
            }
        }

        Random random = player.getRNG();
        double currentSpeed = Math.sqrt(Math.pow(player.posX - player.prevPosX, 2) +
                Math.pow(player.posY - player.prevPosY, 2) + Math.pow(player.posZ - player.prevPosZ, 2));
        double spreadModifier = 100 * currentSpeed;

        if (aim) {
            spreadModifier -= 0.5;
        } else if (player.isSneaking()) {
            spreadModifier -= 0.15;
        }

        if (player.isSprinting()) {
            spreadModifier += 1;
        }

        for (int i = 0; i < ammoType.numPellets; i++) {
            double yawOffset = Math.clamp(random.nextGaussian(), -2, 2) * spread / 2 *
                    Math.max(0.1, 1 + spreadModifier);
            double pitchOffset = Math.clamp(random.nextGaussian(), -2, 2) * spread / 2 *
                    Math.max(0.1, 1 + spreadModifier);

            fire(player.getEntityWorld(), player,
                    new Vec3d(player.posX + handXOffset, player.posY + 1.45, player.posZ + handZOffset),
                    player.rotationYaw + yawOffset, player.rotationPitch + pitchOffset, ammoType);
        }
    }

    public SuSyAmmoType getCurrentAmmoType(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey(NBT_AMMO_TYPE)) {
            String name = tag.getString(NBT_AMMO_TYPE);
            for (SuSyAmmoType type : ammoTypes) {
                if (name.equals(type.name.toLowerCase(Locale.ROOT))) {
                    return type;
                }
            }
        }
        return null;
    }

    // fire modes

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
        NBTTagCompound tag = getOrCreateTag(stack);
        int current = tag.getInteger(NBT_FIRE_MODE);
        int next = (current + 1) % fireModes.size();
        tag.setInteger(NBT_FIRE_MODE, next);
        player.sendMessage(new TextComponentTranslation(
                "susy.gun.fire_mode." + fireModes.get(next).name().toLowerCase(Locale.ROOT)));
    }

    // item behaviour

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("susy.gun.tooltip.damage", damage));
        lines.add(I18n.format("susy.gun.tooltip.caliber", I18n.format("susy.caliber." + caliber)));
        if (itemStack.hasTagCompound()) {
            NBTTagCompound tag = itemStack.getTagCompound();
            if (!tag.hasKey(NBT_AMMO) || tag.getInteger(NBT_AMMO) <= 0) {
                lines.add(I18n.format("susy.gun.tooltip.empty"));
            } else {
                if (tag.hasKey(NBT_MAGAZINE_SIZE)) {
                    lines.add(I18n.format(tag.getInteger(NBT_AMMO) + "/" + tag.getInteger(NBT_MAGAZINE_SIZE)));
                }
                if (tag.hasKey(NBT_AMMO_TYPE)) {
                    lines.add(I18n.format("susy.gun.tooltip.bullet_type",
                            I18n.format("susy.ammo_type." + tag.getString(NBT_AMMO_TYPE))));
                }
            }
        }
        lines.add(I18n.format("susy.gun.fire_mode." + getSelectedMode(itemStack).name().toLowerCase(Locale.ROOT)));
        if (fireModes.size() > 1) {
            lines.add(I18n.format("susy.gun.tooltip.switch_mode",
                    SuSyKeyBind.CYCLE_FIRE_MODE.toMinecraft().getDisplayName()));
        }
    }

    public static final class Builder {

        private final double muzzleVelocity;
        private final double spread;
        private final float damage;
        private final int cooldownTicks;
        private int burstCount = 1;
        private int burstIntervalTicks = 1;
        private int maxMagazineSize = 0;
        private int reloadTicks = 0;
        private final String caliber;
        private boolean beltFed = false;
        private boolean integralMagazine = false;
        private float recoilStrength = 0;
        private final List<GunFireMode> modes = new ArrayList<>();

        private Builder(double muzzleVelocity, double spread, float damage, int cooldownTicks, String caliber) {
            this.muzzleVelocity = muzzleVelocity;
            this.spread = spread;
            this.damage = damage;
            this.cooldownTicks = cooldownTicks;
            this.caliber = caliber;
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

        public Builder magazine(int maxSize, int reloadTicks) {
            this.maxMagazineSize = maxSize;
            this.reloadTicks = reloadTicks;
            return this;
        }

        public Builder beltFed() {
            this.beltFed = true;
            return this;
        }

        public Builder integralMagazine() {
            this.integralMagazine = true;
            return this;
        }

        public Builder setRecoil(float recoil) {
            this.recoilStrength = recoil;
            return this;
        }

        public SuSyGunBehavior build() {
            List<GunFireMode> resolvedModes = new ArrayList<>(modes);
            if (resolvedModes.isEmpty()) {
                resolvedModes.add(burstCount > 1 ? GunFireMode.BURST : GunFireMode.SEMI_AUTO);
            }
            return new SuSyGunBehavior(muzzleVelocity, spread, damage, cooldownTicks, resolvedModes,
                    burstCount, burstIntervalTicks, maxMagazineSize, reloadTicks, caliber, beltFed, integralMagazine,
                    recoilStrength);
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

    public static final List<SuSyAmmoType> ammoTypes = new ArrayList<>(java.util.Arrays.asList(
            FULL_METAL_JACKET, // gray
            HOLLOW_POINT, // white
            ARMOR_PIERCING, // black
            SABOT, // purple
            BUCKSHOT, // aqua
            RED_TRACER, // pink
            GREEN_TRACER, // green
            BLUE_TRACER, // blue
            INCENDIARY, // yellow
            ARMOR_PIERCING_INCENDIARY, // orange
            ARMOR_PIERCING_EXPLOSIVE // red
    ));

    public static final String CALIBER_PAPER_CARTRIDGE = "paper_cartridge"; // generic umbrella for all pre-metal
                                                                            // cartridge firearms
    public static final String CALIBER_11x24_BP = "11x24mm_bp"; // .45 ACP (black powder)
    public static final String CALIBER_13x45_BP = "13x45mmR_bp"; // .50-70 Government (black powder)
    public static final String CALIBER_18_SHOTGUN_BP = "18mm_shotgun_bp"; // 12-gauge shotgun (black powder)
    public static final String CALIBER_11x24 = "11x24mm"; // .45 ACP
    public static final String CALIBER_7_5x55 = "7.5x55mm"; // 7.62x51mm NATO, .308 Winchester, .30-06 Springfield,
                                                            // 7.62x54mmR, etc
    public static final String CALIBER_18_SHOTGUN = "18mm_shotgun"; // 12-gauge shotgun
    public static final String CALIBER_5_5x30 = "5.5x30mm"; // FN 5.7x28
    public static final String CALIBER_5_5x45 = "5.5x45mm"; // 5.56x45mm NATO, 5.45x39mm
    public static final String CALIBER_8_6x70 = "8.6x70mm"; // .338 Lapua Magnum
    public static final String CALIBER_13x30 = "13x30mm"; // .50 AE
    public static final String CALIBER_13x100 = "13x100mm"; // .50 BMG
    public static final String CALIBER_6_8x50 = "6.8x50mm"; // .277 Fury

    public static final List<String> calibers = new ArrayList<>(Arrays.asList(
            CALIBER_PAPER_CARTRIDGE,
            CALIBER_11x24_BP,
            CALIBER_13x45_BP,
            CALIBER_18_SHOTGUN_BP,
            CALIBER_11x24,
            CALIBER_7_5x55,
            CALIBER_18_SHOTGUN,
            CALIBER_5_5x30,
            CALIBER_5_5x45,
            CALIBER_8_6x70,
            CALIBER_13x30,
            CALIBER_13x100,
            CALIBER_6_8x50));
}
