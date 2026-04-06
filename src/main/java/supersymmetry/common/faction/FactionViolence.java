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
    private static final double radius = 128.0;

    // violence
    //checks every mob every tick, probably not the best way to do this
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity().world.isRemote) return; // server only
        if (!FactionViolenceManager.isEnabled()) return;
        if (!(event.getEntity() instanceof EntityLiving)) return;

        EntityLiving mob = (EntityLiving) event.getEntity();
        NBTTagCompound tag = mob.getEntityData();
        if (!tag.hasKey("susy.faction")) return;

        String mobFaction = tag.getString("susy.faction");
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
        if (mob.getAttackTarget() == null) {
            for (EntityLiving target : mob.world.getEntitiesWithinAABB(EntityLiving.class, mob.getEntityBoundingBox().grow(radius))) {

                // Must be visible, skips a lot of the loop
                if (!mob.canEntityBeSeen(target)) continue;

                if (target == mob) continue;

                String targetFaction = target.getEntityData().getString("susy.faction");
                boolean isUnaligned = targetFaction == null || targetFaction.isEmpty();
                boolean isOpposingFaction = !isUnaligned && !mobFaction.equals(targetFaction);

                // Case 1: Unaligned get attacked only if hostile
                if (isUnaligned) {
                    if (target instanceof net.minecraft.entity.monster.IMob) {
                        mob.setAttackTarget(target);
                        break;
                    }
                }

                // Case 2: attack everything from other factions
                else if (isOpposingFaction) {
                    mob.setAttackTarget(target);
                    break;
                }
            }
        }
    }
}
