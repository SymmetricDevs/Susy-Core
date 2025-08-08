package supersymmetry.common;

import gregtech.api.GregTechAPI;
import gregtech.api.util.GTTeleporter;
import gregtech.api.util.TeleportHandler;
import gregtech.common.items.MetaItems;
import gregtechfoodoption.item.GTFOMetaItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import supersymmetry.Supersymmetry;
import supersymmetry.common.entities.EntityDropPod;
import supersymmetry.common.event.DimensionBreathabilityHandler;
import supersymmetry.common.event.MobHordeWorldData;
import supersymmetry.common.item.SuSyArmorItem;
import supersymmetry.common.network.SPacketFirstJoin;
import supersymmetry.common.world.WorldProviderPlanet;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class EventHandlers {

    public static final String FIRST_SPAWN = Supersymmetry.MODID + ".first_spawn";

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {

        NBTTagCompound playerData = event.player.getEntityData();
        NBTTagCompound data = playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG) ? playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG) : new NBTTagCompound();

        if (!event.player.getEntityWorld().isRemote && !data.getBoolean(FIRST_SPAWN)) {

            data.setBoolean(FIRST_SPAWN, true);
            playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
            if (event.player.isCreative()) return;

            GregTechAPI.networkHandler.sendTo(new SPacketFirstJoin(), (EntityPlayerMP) event.player);

            EntityDropPod dropPod = new EntityDropPod(event.player.getEntityWorld(), event.player.posX, event.player.posY + 256, event.player.posZ);

            GTTeleporter teleporter = new GTTeleporter((WorldServer) event.player.world, event.player.posX, event.player.posY + 256, event.player.posZ);
            TeleportHandler.teleport(event.player, event.player.dimension, teleporter, event.player.posX, event.player.posY + 256, event.player.posZ);

            event.player.getEntityWorld().spawnEntity(dropPod);
            event.player.startRiding(dropPod);

            event.player.addItemStackToInventory(GTFOMetaItem.EMERGENCY_RATIONS.getStackForm(32));
            event.player.addItemStackToInventory(MetaItems.PROSPECTOR_LV.getChargedStack(100000));
        }
    }

    @SubscribeEvent
    public static void onTrySpawnPortal(BlockEvent.PortalSpawnEvent event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {

        World world = event.world;

        if (world.isRemote) {
            return;
        }

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (world.provider.getDimension() != 0) {
            return;
        }

        if (world instanceof WorldServer server) {
            PlayerList list = server.getMinecraftServer().getPlayerList();
            MobHordeWorldData mobHordeWorldData = MobHordeWorldData.get(world);
            list.getPlayers().forEach(p -> mobHordeWorldData.getPlayerData(p.getPersistentID()).update(p));
            mobHordeWorldData.markDirty();
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.world.getTotalWorldTime() % 20 == 0 && event.phase == TickEvent.Phase.START) {
            DimensionBreathabilityHandler.tickPlayer(event.player);
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onEntityLivingFallEventStart(LivingFallEvent event) {
        Entity armor = event.getEntity();
        if (armor instanceof EntityPlayer player) {
            ItemStack boots = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            if (!boots.isEmpty() && boots.getItem() instanceof SuSyArmorItem) {
                if (player.fallDistance > 3.2F) {
                    player.fallDistance = 0;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityLivingFallEvent(LivingFallEvent event) {
        if (event.getEntity().world.provider instanceof WorldProviderPlanet provider) {
            event.setDistance((float) (event.getDistance() * provider.getPlanet().gravity));
        }

        Entity armor = event.getEntity();
        if (armor instanceof EntityPlayer player) {
            ItemStack boots = player.getItemStackFromSlot(EntityEquipmentSlot.FEET);
            if (!boots.isEmpty() && boots.getItem() instanceof SuSyArmorItem) {
                player.fallDistance = event.getDistance();
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlaceEvent(BlockEvent.EntityPlaceEvent event) {
        if (event.getWorld().provider instanceof WorldProviderPlanet provider && !provider.getPlanet().supportsFire) {
            Block block = event.getPlacedBlock().getBlock();
            if (block instanceof BlockTorch) {
                event.setCanceled(true);
            }
        }
    }
}
