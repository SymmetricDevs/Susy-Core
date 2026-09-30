package supersymmetry.common.event;

import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import supersymmetry.Supersymmetry;
import supersymmetry.api.SusyLog;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class MobHordePostAssigner {

    /**
    * this is here in case we summon new mobs during an invasion (such as reinforcements)
    * currently used for helicopters spawned in the air cavalry riad, more soon to come
    **/

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        if (!(event.getEntity() instanceof EntityLivingBase)) return;

        EntityLivingBase entity = (EntityLivingBase) event.getEntity();
        NBTTagCompound entityData = entity.getEntityData();

        if (!entityData.hasKey("susy")) return;

        NBTTagCompound susy = entityData.getCompoundTag("susy");

        if (!susy.hasKey("invasionOwner")) return;


        String ownerUUIDString = susy.getString("invasionOwner");
        UUID ownerUUID;
        try {
            ownerUUID = UUID.fromString(ownerUUIDString);
        } catch (IllegalArgumentException e) {
            SusyLog.logger.warn("Invalid invasionOwner UUID on entity {}: {}", entity.getName(), ownerUUIDString);
            return;
        }

        WorldServer world = (WorldServer) event.getWorld();
        EntityPlayerMP player = (EntityPlayerMP) world.getPlayerEntityByUUID(ownerUUID);
        if (player == null) return;

        MobHordeWorldData worldData = MobHordeWorldData.get(world);
        MobHordePlayerData playerData = worldData.getPlayerData(player.getPersistentID());

        if (!playerData.hasActiveInvasion) {
            SusyLog.logger.warn("Entity {} joined with invasionOwner {} but that player has no active invasion",
                    entity.getName(), ownerUUIDString);
            return;
        }

        playerData.addEntity(entity.getPersistentID());
        //to avoid spam
        //SusyLog.logger.info("Registered command-spawned entity {} to invasion {} for player {}", entity.getName(), playerData.currentInvasion, player.getName());
    }
}
