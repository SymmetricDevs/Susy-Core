package supersymmetry.common.faction;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import supersymmetry.Supersymmetry;

import java.util.List;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class FactionLeader {

    private static final String TAG_ROOT     = "susy";
    private static final String TAG_LEADER   = "leader";
    private static final String TAG_FACTION  = "faction";
    private static final String TAG_SMART_AI = "smartAI";

    private static final double LEADER_RADIUS = 10.0;


    public static boolean isLeader(EntityLiving entity) {
        NBTTagCompound tag = entity.getEntityData();
        if (!tag.hasKey(TAG_ROOT)) return false;
        return tag.getCompoundTag(TAG_ROOT).getBoolean(TAG_LEADER);
    }

    public static String getFaction(EntityLivingBase entity) {
        NBTTagCompound tag = entity.getEntityData();
        if (!tag.hasKey(TAG_ROOT)) return "";
        NBTTagCompound susy = tag.getCompoundTag(TAG_ROOT);
        return susy.hasKey(TAG_FACTION) ? susy.getString(TAG_FACTION) : "";
    }

    private static void markSmartAI(EntityLiving entity) {
        NBTTagCompound tag = entity.getEntityData();
        NBTTagCompound susy = tag.hasKey(TAG_ROOT) ? tag.getCompoundTag(TAG_ROOT) : new NBTTagCompound();
        susy.setBoolean(TAG_SMART_AI, true);
        tag.setTag(TAG_ROOT, susy);
    }

    private static boolean hasSmartAI(EntityLiving entity) {
        NBTTagCompound tag = entity.getEntityData();
        if (!tag.hasKey(TAG_ROOT)) return false;
        return tag.getCompoundTag(TAG_ROOT).getBoolean(TAG_SMART_AI);
    }


    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity().world.isRemote) return;
        if (!(event.getEntity() instanceof EntityLiving)) return;

        EntityLiving mob = (EntityLiving) event.getEntity();
        if (!isLeader(mob)) return;


        if (!hasSmartAI(mob)) {
            markSmartAI(mob);
        }

        if (mob.ticksExisted % 20 != 0) return;

        String leaderFaction = getFaction(mob);
        if (leaderFaction.isEmpty()) return;

        List<EntityLiving> nearby = mob.world.getEntitiesWithinAABB(
                EntityLiving.class,
                mob.getEntityBoundingBox().grow(LEADER_RADIUS)
        );

        for (EntityLiving follower : nearby) {
            if (follower == mob) continue;
            if (hasSmartAI(follower)) continue;

            String followerFaction = getFaction(follower);
            if (!leaderFaction.equals(followerFaction)) continue;

            markSmartAI(follower);
        }
    }
}
