package supersymmetry.common.event;

import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import supersymmetry.Supersymmetry;
import supersymmetry.api.space.PlanetGrowthPolicy;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public final class PlanetBonemealHandler {

    private PlanetBonemealHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBonemeal(BonemealEvent event) {
        World world = event.getWorld();
        PlanetGrowthPolicy policy = PlanetGrowthPolicy.forDimension(world.provider.getDimension());
        if (policy.suppressBonemeal(event.getEntityPlayer())) {
            // DENY is ignored by onApplyBonemeal; cancellation returns its stopping result.
            // Forge source:
            // https://github.com/MinecraftForge/MinecraftForge/blob/1.12.x/src/main/java/net/minecraftforge/event/ForgeEventFactory.java
            event.setCanceled(true);
        }
    }
}
