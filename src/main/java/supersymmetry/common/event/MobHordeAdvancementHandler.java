package supersymmetry.common.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import supersymmetry.Supersymmetry;
import supersymmetry.api.event.MobHordeEvent;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class MobHordeAdvancementHandler {

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent event) {
        if (!(event.getEntityPlayer() instanceof EntityPlayerMP)) return;

        EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
        ResourceLocation advancementID = event.getAdvancement().getId();

        MobHordeWorldData worldData = MobHordeWorldData.get(player.world);
        MobHordePlayerData playerData = worldData.getPlayerData(player.getPersistentID());

        for (MobHordeEvent mobEvent : MobHordeEvent.EVENTS.values()) {

            ResourceLocation required = mobEvent.getRequiredAdvancement();
            if (required == null) continue;

            if (!required.equals(advancementID)) continue;

            if (mobEvent.isRunOnce() && playerData.hasCompleted(mobEvent.KEY)) {
                continue;
            }

            try {
                if (mobEvent.run(player, playerData::addEntity)) {
                    playerData.setCurrentInvasion(mobEvent);

                    if (mobEvent.isRunOnce()) {
                        playerData.markCompleted(mobEvent.KEY);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
