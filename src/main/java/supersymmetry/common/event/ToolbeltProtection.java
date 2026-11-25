package supersymmetry.common.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import supersymmetry.Supersymmetry;
import supersymmetry.common.item.ItemToolbelt;

/**
 * Keeps the belt in hand when a tool inside it breaks.
 * <p>
 * GT's ToolEventHandlers.onPlayerDestroyItem hands the broken stack - a power unit, for an electric
 * tool - to the player by putting it into the hand that destroyed the item. A tool that breaks out of
 * a toolbelt was destroyed by the belt in the hand, so GT put the power unit in place of the belt:
 * the belt "broke" along with the tool. The event carries the destroyed tool, not the stack that was
 * really in the hand, so GT cannot tell the difference.
 * <p>
 * The belt is remembered before GT handles the event and put back afterwards; the broken stack is
 * moved to the inventory, or dropped, which is what GT itself does when there is no hand to fill.
 */
@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public final class ToolbeltProtection {

    private static final ThreadLocal<ItemStack> SUSY_HELD_BELT = new ThreadLocal<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void rememberHeldToolbelt(PlayerDestroyItemEvent event) {
        if (event.getHand() == null) return;
        ItemStack held = event.getEntityPlayer().getHeldItem(event.getHand());
        // A belt breaking is its own affair; only a tool that came out of one is redirected here.
        if (held.getItem() instanceof ItemToolbelt && event.getOriginal().getItem() != held.getItem()) {
            SUSY_HELD_BELT.set(held);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void restoreHeldToolbelt(PlayerDestroyItemEvent event) {
        ItemStack belt = SUSY_HELD_BELT.get();
        if (belt == null) return;
        SUSY_HELD_BELT.remove();
        if (event.getHand() == null) return;

        EntityPlayer player = event.getEntityPlayer();
        ItemStack after = player.getHeldItem(event.getHand());
        if (after == belt) return;
        player.setHeldItem(event.getHand(), belt);
        if (!after.isEmpty() && !player.addItemStackToInventory(after)) {
            player.dropItem(after, true);
        }
    }

    private ToolbeltProtection() {}
}
