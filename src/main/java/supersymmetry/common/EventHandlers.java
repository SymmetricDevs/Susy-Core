package supersymmetry.common;

import gregtech.api.util.GTTeleporter;
import gregtech.api.util.TeleportHandler;
import gregtechfoodoption.item.GTFOMetaItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import supersymmetry.Supersymmetry;
import supersymmetry.common.entities.EntityDropPod;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class EventHandlers {

    private static final String FIRST_SPAWN = Supersymmetry.MODID + ".first_spawn";

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {

        NBTTagCompound playerData = event.player.getEntityData();
        NBTTagCompound data = playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG) ? playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG) : new NBTTagCompound();

        if(!event.player.getEntityWorld().isRemote && !data.getBoolean(FIRST_SPAWN)) {

            data.setBoolean(FIRST_SPAWN, true);
            playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);

            EntityDropPod dropPod = new EntityDropPod(event.player.getEntityWorld(), event.player.posX, event.player.posY + 256, event.player.posZ);

            GTTeleporter teleporter = new GTTeleporter((WorldServer) event.player.world, event.player.posX, event.player.posY + 256, event.player.posZ);
            TeleportHandler.teleport(event.player, event.player.dimension, teleporter, event.player.posX, event.player.posY + 256, event.player.posZ);

            event.player.getEntityWorld().spawnEntity(dropPod);
            event.player.startRiding(dropPod);

            event.player.addItemStackToInventory(GTFOMetaItem.EMERGENCY_RATIONS.getStackForm(10));
        }
    }

}
