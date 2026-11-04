package supersymmetry.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import supersymmetry.common.item.ItemToolbelt;

/**
 * Sneak + scroll selects a toolbelt slot and consumes the scroll, mirroring the fork's handler.
 */
@SideOnly(Side.CLIENT)
public class ToolbeltInputHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMouseEvent(MouseEvent event) {
        if (event.getDwheel() == 0) return;

        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null || !player.isSneaking()) return;

        ItemStack stack = player.getHeldItemMainhand();
        if (!(stack.getItem() instanceof ItemToolbelt belt)) return;

        // vanilla stores the highlighting stack without copying, so the HUD needs a fresh instance
        stack = stack.copy();
        belt.changeSelectedToolMousewheel(event.getDwheel(), stack);
        InventoryPlayer inv = player.inventory;
        inv.mainInventory.set(inv.currentItem, stack);
        event.setCanceled(true);
    }
}
