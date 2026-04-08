package supersymmetry.common.faction;


import net.minecraft.nbt.NBTTagCompound;


import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import supersymmetry.Supersymmetry;

import net.minecraft.entity.EntityLiving;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class FactionViolence {

    private static final String TAG_ROOT = "susy";
    private static final String TAG_FACTION = "faction";
    private static final double radius = 32.0;

    // violence
    //checks every mob every tick, probably not the best way to do this
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity().world.isRemote) return; // server only
        if (!FactionViolenceManager.isEnabled()) return;
        if (!(event.getEntity() instanceof EntityLiving)) return;

        EntityLiving mob = (EntityLiving) event.getEntity();
        NBTTagCompound tag = mob.getEntityData();
        if (!tag.hasKey(TAG_ROOT)) return;

        NBTTagCompound susyTag = tag.getCompoundTag(TAG_ROOT);
        if (!susyTag.hasKey(TAG_FACTION)) return;

        String mobFaction = susyTag.getString(TAG_FACTION);
        if (mobFaction.isEmpty()) return;

        // Clear attack target if dead or invalid
        // not sure if this is needed or not, but techguns will shoot at nothing if it isn't
        // they just built different like that
        if (mob.getAttackTarget() != null &&
                (mob.getAttackTarget().isDead ||
                        !mob.getAttackTarget().isEntityAlive() ||
                        !(mob.getAttackTarget() instanceof net.minecraft.entity.monster.IMob))) {
            mob.setAttackTarget(null);
        }

        // Only assign a new target if none exists
        EntityLiving bestTarget = null;
        double bestDistanceSq = Double.MAX_VALUE;

        for (EntityLiving target : mob.world.getEntitiesWithinAABB(EntityLiving.class, mob.getEntityBoundingBox().grow(radius))) {

            if (target == mob) continue;
            if (!mob.canEntityBeSeen(target)) continue;

            NBTTagCompound targetTag = target.getEntityData();
            String targetFaction = "";

            if (targetTag.hasKey(TAG_ROOT)) {
                NBTTagCompound targetSusy = targetTag.getCompoundTag(TAG_ROOT);
                if (targetSusy.hasKey(TAG_FACTION)) {
                    targetFaction = targetSusy.getString(TAG_FACTION);
                }
            }

            boolean isUnaligned = targetFaction.isEmpty();
            boolean isOpposingFaction = !isUnaligned && !mobFaction.equals(targetFaction);

            boolean shouldAttack = false;

            if (isUnaligned) {
                if (target instanceof net.minecraft.entity.monster.IMob) {
                    shouldAttack = true;
                }
            } else if (isOpposingFaction) {
                shouldAttack = true;
            }

            if (!shouldAttack) continue;

            double distSq = mob.getDistanceSq(target);
            if (distSq < bestDistanceSq) {
                bestDistanceSq = distSq;
                bestTarget = target; //smart targetting
            }
        }

        if (bestTarget != null) {
            mob.setAttackTarget(bestTarget);
        }
    }
}
