package supersymmetry.api.space.reentry;

import java.util.*;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import supersymmetry.common.entities.EntityDropPod;

public class ReEntryMountScheduler {

    private static final List<PendingMount> PENDING = new ArrayList<>();
    private static boolean registered = false;

    public static void schedule(EntityPlayerMP player, EntityDropPod pod) {
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(new ReEntryMountScheduler());
            registered = true;
        }
        PENDING.add(new PendingMount(player, pod, 2)); // 2-tick delay
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Iterator<PendingMount> it = PENDING.iterator();
        while (it.hasNext()) {
            PendingMount pm = it.next();
            pm.ticksRemaining--;
            if (pm.ticksRemaining <= 0) {
                if (!pm.pod.isDead && pm.player.isEntityAlive()) {
                    pm.player.startRiding(pm.pod, true);
                }
                it.remove();
            }
        }
    }

    private static class PendingMount {

        final EntityPlayerMP player;
        final EntityDropPod pod;
        int ticksRemaining;

        PendingMount(EntityPlayerMP player, EntityDropPod pod, int delay) {
            this.player = player;
            this.pod = pod;
            this.ticksRemaining = delay;
        }
    }
}
