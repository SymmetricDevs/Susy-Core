package supersymmetry.common.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import supersymmetry.api.event.MobHordeEvent;

import java.util.*;
import java.util.stream.Collectors;

public class MobHordePlayerData implements INBTSerializable<NBTTagCompound> {
    // Player cooldown for all events.
    public int ticksUntilCanSpawn;
    public int gracePeriod;
    public int ticksActive;
    public int timeoutPeriod;
    public int[] invasionTimers;
    public boolean hasActiveInvasion = false;
    public List<UUID> invasionEntitiesUUIDs = new ArrayList<>();
    public String currentInvasion = "";

    public MobHordePlayerData(int gracePeriod) {
        this.ticksUntilCanSpawn = gracePeriod;
        this.gracePeriod = gracePeriod;
        this.invasionTimers = new int[MobHordeEvent.EVENTS.size()];
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound result = new NBTTagCompound();
        result.setInteger("ticksUntilCanSpawn", ticksUntilCanSpawn);
        result.setIntArray("invasionTimers", invasionTimers);
        result.setBoolean("hasActiveInvasion", hasActiveInvasion);
        if(this.hasActiveInvasion && !this.invasionEntitiesUUIDs.isEmpty()) {
            result.setString("currentInvasion", currentInvasion);
            result.setInteger("timeoutPeriod", this.timeoutPeriod);
            result.setInteger("ticksActive", this.ticksActive);
            NBTTagList tagList = new NBTTagList();
            invasionEntitiesUUIDs.stream()
                    .forEach(uuid -> tagList.appendTag(NBTUtil.createUUIDTag(uuid)));
            result.setTag("invasionEntitiesUUIDs", tagList);
        }
        return result;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        ticksUntilCanSpawn = nbt.getInteger("ticksUntilCanSpawn");
        invasionTimers = Arrays.copyOf(nbt.getIntArray("invasionTimers"), MobHordeEvent.EVENTS.size());
        hasActiveInvasion = nbt.getBoolean("hasActiveInvasion");
        if (hasActiveInvasion) {
            invasionEntitiesUUIDs.clear();
            this.currentInvasion = nbt.getString("currentInvasion");
            this.timeoutPeriod = nbt.getInteger("timeoutPeriod");
            this.ticksActive = nbt.getInteger("ticksActive");
            NBTTagList tagList = nbt.getTagList("invasionEntitiesUUIDs", Constants.NBT.TAG_COMPOUND);
            tagList.forEach(compound -> invasionEntitiesUUIDs.add(NBTUtil.getUUIDFromTag((NBTTagCompound) compound)));
        }
    }

    public void update(EntityPlayerMP player) {
        if (hasActiveInvasion) {
            ++ticksActive;
            if (this.ticksActive > this.timeoutPeriod) {
                this.finishInvasion();
            } else return;
        }
        ticksUntilCanSpawn--;
        for (int i = 0; i < invasionTimers.length; i++) {
            invasionTimers[i]--;
        }
        if (ticksUntilCanSpawn <= 0 && Math.random() < 0.001) {
            List<Integer> doableEvents = new ArrayList<>();
            List<MobHordeEvent> events = MobHordeEvent.EVENTS.values().stream()
                    .collect(Collectors.toList());
            MobHordeEvent event;
            for (int i = 0; i < MobHordeEvent.EVENTS.values().size(); i++) {
                event = events.get(i);
                if (event.canRun(player) && invasionTimers[i] <= 0) {
                    doableEvents.add(i);
                }
            }
            if (!doableEvents.isEmpty()) {
                ticksUntilCanSpawn = gracePeriod;
                int index = doableEvents.get((int) (Math.random() * doableEvents.size()));
                event = events.get(index);
                if (event.run(player, this::addEntity)) {
                    invasionTimers[index] = event.getNextDelay();

                    this.setCurrentInvasion(event);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        EntityLivingBase deadEntity = event.getEntityLiving();
        UUID deadEntityUUID = deadEntity.getPersistentID();

        if (invasionEntitiesUUIDs.contains(deadEntityUUID)) {
            removeDeadEntity(deadEntityUUID);

            // Check if all spawned entities are dead
            if (invasionEntitiesUUIDs.isEmpty()) {
                this.finishInvasion();
            }
        }
    }

    public void setCurrentInvasion(MobHordeEvent event) {
        this.currentInvasion = event.KEY;
        this.timeoutPeriod = event.timeoutPeriod;
        this.hasActiveInvasion = true;
        this.ticksActive = 0;
    }

    public void addEntity(UUID uuid) {
        this.invasionEntitiesUUIDs.add(uuid);
    }

    private void removeDeadEntity(UUID deadEntityUUID) {
        this.invasionEntitiesUUIDs.remove(deadEntityUUID);
    }

    public void finishInvasion() {
        this.hasActiveInvasion = false;
        this.currentInvasion = "";
        this.ticksActive = 0;
    }

    public void stopInvasion(EntityPlayerMP player) {
        if(this.hasActiveInvasion) {
            WorldServer world = player.getServerWorld();
            this.invasionEntitiesUUIDs.stream()
                    .map(uuid -> world.getEntityFromUuid(uuid))
                    .filter(Objects::nonNull)
                    .forEach(entity -> entity.setDead());
            // Will get called implicitly from onEntityDeath, but I am doing it again just to be sure
            this.finishInvasion();
        }
    }
}
