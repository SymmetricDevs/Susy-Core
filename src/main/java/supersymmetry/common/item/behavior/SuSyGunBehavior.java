package supersymmetry.common.item.behavior;

import static supersymmetry.common.metatileentities.multi.electric.MetaTileEntityCargoDronePad.*;

import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
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
    private final GunFireMode fireMode;
    private final int burstCount;
    private final int burstIntervalTicks;
    public final double bulletCheckSpeed = 1; // not meant to be set by the constructor

    public SuSyGunBehavior(double muzzleVelocity, double spread, int pellets, float damage,
                           int cooldownTicks, GunFireMode fireMode, int burstCount, int burstIntervalTicks) {
        this.muzzleVelocity = Math.max(muzzleVelocity, 0.1);
        this.spread = spread;
        this.pellets = Math.max(pellets, 1); // projectiles fired at the same instant per shot
        this.damage = damage;
        this.fireMode = java.util.Objects.requireNonNull(fireMode, "fire mode must not be null");
        this.burstCount = Math.max(burstCount, 1);
        this.burstIntervalTicks = Math.max(burstIntervalTicks, 1);
        // The cooldown can never be shorter than the burst 
        int burstDuration = (this.burstCount - 1) * this.burstIntervalTicks + 1;
        this.cooldownTicks = Math.max(Math.max(cooldownTicks, 0), burstDuration);
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

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        ActionResult<ItemStack> result = ActionResult.newResult(EnumActionResult.SUCCESS, stack);

        if (player.world.isRemote) {
            return result;
        }

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
        Random random = new Random();
        for (int i = 0; i < pellets; i++) {
            double yawOffset = Math.clamp(random.nextGaussian(), -2, 2) * spread / 2;
            double pitchOffset = Math.clamp(random.nextGaussian(), -2, 2) * spread / 2;
            fire(world, new Vec3d(player.posX + handXOffset, player.posY + 1.45, player.posZ + handZOffset),
                    player.rotationYaw + yawOffset, player.rotationPitch + pitchOffset);
        }
        return result;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) { // to be changed
        if (itemStack.hasTagCompound()) {
            NBTTagCompound tag = itemStack.getSubCompound(TAG_ROOT);
            if (tag.hasKey(TAG_X) && tag.hasKey(TAG_Y) && tag.hasKey(TAG_Z)) {
                lines.add(I18n.format("susy.location_card.tooltip.coords") + "(" + tag.getInteger(TAG_X) + ", " +
                        tag.getInteger(TAG_Y) + ", " + tag.getInteger(TAG_Z) + ")");
            } else {
                lines.add(I18n.format("susy.location_card.tooltip.no_coords"));
            }
        } else {
            lines.add(I18n.format("susy.location_card.tooltip.no_coords"));
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
