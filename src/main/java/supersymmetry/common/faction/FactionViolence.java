package supersymmetry.common.faction;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import supersymmetry.Supersymmetry;
import supersymmetry.common.potion.PotionDropPodSickness;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class FactionViolence {

    private static final String TAG_ROOT = "susy";
    private static final String TAG_FACTION = "faction";
    private static final double radius = 32.0;

    // violence
    // checks every mob every tick, probably not the best way to do this
    @SubscribeEvent(priority = EventPriority.HIGH) //required HIGH, techguns will shit pant otherwise
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity().world.isRemote) return; // server only
        if (!FactionViolenceManager.isEnabled(event.getEntity().world)) return;
        if (!(event.getEntity() instanceof EntityLiving)) return;

        EntityLiving mob = (EntityLiving) event.getEntity();
        NBTTagCompound tag = mob.getEntityData();
        if (!tag.hasKey(TAG_ROOT)) return;

        NBTTagCompound susyTag = tag.getCompoundTag(TAG_ROOT);
        if (!susyTag.hasKey(TAG_FACTION)) return;

        String mobFaction = susyTag.getString(TAG_FACTION);
        if (mobFaction.isEmpty()) return;

        if (!(mob instanceof net.minecraft.entity.monster.IMob)) return; //skips logic for animals that belong to factions, speeds this logic long

        // Skip targeting during dismount grace period
        // technical explanation: mobs sometimes target the drop pod due to explosion damage or smth
        // tried to fix it before, didn't work. might be a techguns being dumb thing
        // lore explanation: droppod sickness, droppods can disorient people when they arrive from outer space.
        if (mob.isPotionActive(PotionDropPodSickness.INSTANCE)) return;

        // praying this won't tick too hard
        EntityLivingBase currentTarget = mob.getAttackTarget();
        if (currentTarget != null) {
            String targetFaction = getFaction(currentTarget);
            if (!targetFaction.isEmpty() && mobFaction.equals(targetFaction)) {
                mob.setAttackTarget(null);
                // Also clear revenge target to prevent HurtByTarget from re-setting it
                if (mob.getRevengeTarget() == currentTarget) {
                    mob.setRevengeTarget(null);
                }
            } else if (currentTarget.isDead || !currentTarget.isEntityAlive()) {
                mob.setAttackTarget(null);
            }
        }

        // Also check revenge target independently - HurtByTarget may set it without setting attackTarget
        // techguns moment
        EntityLivingBase revengeTarget = mob.getRevengeTarget();
        if (revengeTarget != null) {
            String revengeFaction = getFaction(revengeTarget);
            if (!revengeFaction.isEmpty() && mobFaction.equals(revengeFaction)) {
                mob.setRevengeTarget(null);
            }
        }

        if (mob.getAttackTarget() != null &&
                (!(mob.getAttackTarget() instanceof net.minecraft.entity.monster.IMob) &&
                        !(mob.getAttackTarget() instanceof net.minecraft.entity.player.EntityPlayer))) {
            mob.setAttackTarget(null);
        }

        // Only assign a new target if none exists
        if (mob.getAttackTarget() != null) return;

        EntityLivingBase bestTarget = null;
        double bestDistanceSq = Double.MAX_VALUE;

        for (EntityLivingBase target : mob.world.getEntitiesWithinAABB(EntityLivingBase.class,
                mob.getEntityBoundingBox().grow(radius))) {

            if (target == mob) continue;
            if (!mob.canEntityBeSeen(target)) continue;

            String targetFaction = getFaction(target);
            boolean isUnaligned = targetFaction.isEmpty();
            boolean isOpposingFaction = !isUnaligned && !mobFaction.equals(targetFaction);
            boolean shouldAttack = false;

            if (isUnaligned) {
                // Fix the mobs not attacking the player
                if (target instanceof net.minecraft.entity.monster.IMob ||
                        (target instanceof net.minecraft.entity.player.EntityPlayer &&
                                !((net.minecraft.entity.player.EntityPlayer) target).isCreative() &&
                                !((net.minecraft.entity.player.EntityPlayer) target).isSpectator())) {
                    shouldAttack = true;
                }
            } else if (isOpposingFaction) {
                shouldAttack = true;
            }

            if (!shouldAttack) continue;

            double distSq = mob.getDistanceSq(target);
            if (distSq < bestDistanceSq) {
                bestDistanceSq = distSq;
                bestTarget = target; // smart targetting
            }
        }

        if (bestTarget != null) {
            mob.setAttackTarget(bestTarget);
        }
    }

    private static String getFaction(EntityLivingBase entity) {
        NBTTagCompound tag = entity.getEntityData();
        if (!tag.hasKey(TAG_ROOT)) return "";
        NBTTagCompound susyTag = tag.getCompoundTag(TAG_ROOT);
        if (!susyTag.hasKey(TAG_FACTION)) return "";
        return susyTag.getString(TAG_FACTION);
    }
}
