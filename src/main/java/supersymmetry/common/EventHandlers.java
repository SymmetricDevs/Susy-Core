package supersymmetry.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.jetbrains.annotations.NotNull;

import gregtech.api.GregTechAPI;
import gregtech.api.util.GTTeleporter;
import gregtech.api.util.TeleportHandler;
import gregtech.common.items.MetaItems;
import gregtechfoodoption.item.GTFOMetaItem;
import supersymmetry.Supersymmetry;
import supersymmetry.api.SusyLog;
import supersymmetry.common.entities.EntityDropPod;
import supersymmetry.common.entities.EntityLander;
import supersymmetry.common.event.DimensionBreathabilityHandler;
import supersymmetry.common.event.DimensionRidingSwapData;
import supersymmetry.common.event.MobHordeWorldData;
import supersymmetry.common.item.SuSyArmorItem;
import supersymmetry.common.network.SPacketFirstJoin;
import supersymmetry.common.rocketry.LanderSpawnEntry;
import supersymmetry.common.rocketry.LanderSpawnQueue;
import supersymmetry.common.world.WorldProviderPlanet;
import net.minecraftforge.items.ItemStackHandler;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class EventHandlers {

    public static final String FIRST_SPAWN = Supersymmetry.MODID + ".first_spawn";
    public static List<DimensionRidingSwapData> travellingPassengers = new ArrayList<>();

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        NBTTagCompound playerData = event.player.getEntityData();
        NBTTagCompound data = playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG) ?
                playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG) : new NBTTagCompound();

        if (!event.player.getEntityWorld().isRemote && !data.getBoolean(FIRST_SPAWN)) {

            data.setBoolean(FIRST_SPAWN, true);
            playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
            if (event.player.isCreative()) return;

            GregTechAPI.networkHandler.sendTo(new SPacketFirstJoin(), (EntityPlayerMP) event.player);

            EntityDropPod dropPod = new EntityDropPod(event.player.getEntityWorld(), event.player.posX,
                    event.player.posY + 256, event.player.posZ);

            GTTeleporter teleporter = new GTTeleporter((WorldServer) event.player.world, event.player.posX,
                    event.player.posY + 256, event.player.posZ);
            TeleportHandler.teleport(event.player, event.player.dimension, teleporter, event.player.posX,
                    event.player.posY + 256, event.player.posZ);

            event.player.getEntityWorld().spawnEntity(dropPod);
            event.player.startRiding(dropPod);

            event.player.addItemStackToInventory(GTFOMetaItem.EMERGENCY_RATIONS.getStackForm(32));
            event.player.addItemStackToInventory(MetaItems.PROSPECTOR_LV.getChargedStack(100000));
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        GameRules gameRules = event.getWorld().getGameRules();
        if (!gameRules.hasRule("doInvasions")) {
            gameRules.addGameRule("doInvasions", "true", GameRules.ValueType.BOOLEAN_VALUE);
        }
    }

    @SubscribeEvent
    public static void onTrySpawnPortal(BlockEvent.PortalSpawnEvent event) {
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;

        if (world.isRemote || !(world instanceof WorldServer server)) {
            return;
        }
        if (!travellingPassengers.isEmpty()) {
            handleEntityTransfer();
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        // Process lander spawn queue for all dimensions
        processLanderSpawnQueue(server);

        if (world.provider.getDimension() != 0) {
            return;
        }
        if (!world.getGameRules().getBoolean("doInvasions")) {
            return;
        }

        PlayerList list = server.getMinecraftServer().getPlayerList();
        MobHordeWorldData mobHordeWorldData = MobHordeWorldData.get(world);
        list.getPlayers().forEach(p -> mobHordeWorldData.getPlayerData(p.getPersistentID()).update(p));
        mobHordeWorldData.markDirty();
    }

    private static @NotNull void handleEntityTransfer() {
        List<DimensionRidingSwapData> toRemove = new ArrayList<>();
        for (DimensionRidingSwapData data : travellingPassengers) {
            Entity mount = data.mount;
            Entity passenger = data.passenger;
            if (mount.dimension != passenger.dimension && passenger.getServer() != null &&
                    mount.world.getTotalWorldTime() - data.time > 2) {
                WorldServer newWorld = passenger.getServer().getWorld(mount.dimension);

                passenger.setLocationAndAngles(mount.getPosition().getX(),
                        mount.getPosition().getY(),
                        mount.getPosition().getZ(),
                        mount.rotationYaw,
                        mount.rotationPitch);
                passenger.getServer().getPlayerList().transferPlayerToDimension((EntityPlayerMP) passenger,
                        mount.dimension,
                        new GTTeleporter(newWorld, mount.getPosition().getX(), mount.getPosition().getY(),
                                mount.getPosition().getZ()));
                Entity realMount = newWorld.getEntityFromUuid(mount.getPersistentID());
                if (realMount != null) {
                    passenger.startRiding(realMount);
                }
                toRemove.add(data);
            }

        }
        for (DimensionRidingSwapData data : toRemove) {
            travellingPassengers.remove(data);
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

    /**
     * Processes the lander spawn queue, decrementing timers and spawning landers when ready.
     * This method handles cross-dimensional spawning and ensures chunks are loaded.
     */
    private static void processLanderSpawnQueue(WorldServer world) {
        LanderSpawnQueue queue = LanderSpawnQueue.get(world);

        if (queue.isEmpty()) {
            return;
        }

        List<LanderSpawnEntry> toRemove = new ArrayList<>();

        for (LanderSpawnEntry entry : queue.getEntries()) {
            entry.decrementTicks();

            if (entry.isReadyToSpawn()) {
                spawnLander(world, entry);
                toRemove.add(entry);
            }
        }

        // Remove spawned entries from queue
        for (LanderSpawnEntry entry : toRemove) {
            queue.removeEntry(entry.getUuid());
        }

        if (!toRemove.isEmpty()) {
            queue.markDirty();
        }
    }

    /**
     * Spawns a lander entity based on the provided spawn entry.
     * Handles cross-dimensional spawning and inventory loading.
     */
    private static void spawnLander(WorldServer originWorld, LanderSpawnEntry entry) {
        try {
            // Get the target world (may be different dimension)
            WorldServer targetWorld = originWorld.getMinecraftServer().getWorld(entry.getDimensionId());

            if (targetWorld == null) {
                SusyLog.logger.error("Failed to spawn lander: dimension {} does not exist", entry.getDimensionId());
                return;
            }

            // Create the lander entity
            EntityLander lander = new EntityLander(targetWorld, entry.getX(), entry.getY(), entry.getZ());

            // Load inventory if present
            if (entry.getInventoryData() != null) {
                ItemStackHandler inventory = new ItemStackHandler(36);
                inventory.deserializeNBT(entry.getInventoryData());

                // Copy items to lander's inventory
                for (int i = 0; i < Math.min(inventory.getSlots(), lander.getInventory().getSlots()); i++) {
                    lander.getInventory().setStackInSlot(i, inventory.getStackInSlot(i));
                }
            }

            // Spawn the lander
            targetWorld.spawnEntity(lander);

            SusyLog.logger.info("Spawned lander at ({}, {}, {}) in dimension {}",
                    entry.getX(), entry.getY(), entry.getZ(), entry.getDimensionId());

        } catch (Exception e) {
            SusyLog.logger.error("Error spawning lander: {}", entry, e);
        }
    }
}
