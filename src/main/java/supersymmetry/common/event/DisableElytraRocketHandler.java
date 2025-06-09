package supersymmetry.common.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


public class DisableElytraRocketHandler {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        
        EntityPlayer player = event.player;
        
        // Skip if not elytra flying or not holding fireworks
        if (!player.isElytraFlying() || 
            (!isHoldingFirework(player.getHeldItemMainhand()) && 
             !isHoldingFirework(player.getHeldItemOffhand()))) {
            return;
        }
        
        // Cancel firework boost by consuming without effect
        player.getHeldItemMainhand().shrink(1);
        player.getHeldItemOffhand().shrink(1);
    }
    
    private static boolean isHoldingFirework(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemFirework;
    }
}
