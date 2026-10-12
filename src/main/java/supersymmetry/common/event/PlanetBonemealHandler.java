package supersymmetry.common.event;

import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import supersymmetry.Supersymmetry;
import supersymmetry.common.util.PlanetGrowthRules;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public final class PlanetBonemealHandler {

    private PlanetBonemealHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBonemeal(BonemealEvent event) {
        World world = event.getWorld();
        // The client sends its block-use packet before predicting item use. Let the server decide.
        if (world.isRemote) {
            return;
        }
        if (PlanetGrowthRules.suppressBonemeal(event.getEntityPlayer(), world, event.getPos(), event.getBlock())) {
            // DENY is ignored by onApplyBonemeal; cancellation returns its stopping result.
            // Forge source:
            // https://github.com/MinecraftForge/MinecraftForge/blob/1.12.x/src/main/java/net/minecraftforge/event/ForgeEventFactory.java
            event.setCanceled(true);
        }
    }
}
