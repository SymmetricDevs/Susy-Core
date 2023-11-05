package supersymmetry.common.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import supersymmetry.api.event.MobHordeEvent;

import java.util.*;
import java.util.stream.Collectors;

public class MobHordePlayerData implements INBTSerializable<NBTTagCompound> {
    // Player cooldown for all events.
    public int ticksUntilCanSpawn;
    public int gracePeriod;
    public int[] invasionTimers;

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
        return result;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        ticksUntilCanSpawn = nbt.getInteger("ticksUntilCanSpawn");
        invasionTimers = nbt.getIntArray("invasionTimers");
    }

    public void update(EntityPlayerMP player) {
        ticksUntilCanSpawn--;
        for (int i = 0; i < invasionTimers.length; i++) {
            invasionTimers[i]--;
        }
        if (ticksUntilCanSpawn <= 0 && Math.random() < 0.001) {
            List<Integer> doableEvents = new ArrayList<>();
            for (int i = 0; i < MobHordeEvent.EVENTS.size(); i++) {
                MobHordeEvent event = MobHordeEvent.EVENTS.get(i);
                if (event.canRun(player) && invasionTimers[i] <= 0) {
                    doableEvents.add(i);
                }
            }
            if (!doableEvents.isEmpty()) {
                ticksUntilCanSpawn = gracePeriod;
                int index = doableEvents.get((int) (Math.random() * doableEvents.size()));
                MobHordeEvent event = MobHordeEvent.EVENTS.get(index);
                if (event.run(player)) {
                    invasionTimers[index] = event.getNextDelay();
                }
            }
        }
    }
}
