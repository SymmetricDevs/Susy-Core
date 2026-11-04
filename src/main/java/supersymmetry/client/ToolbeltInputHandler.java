package supersymmetry.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Mouse;

import supersymmetry.common.item.ItemToolbelt;

/**
 * Scroll-wheel tool selection while holding a toolbelt, matching the fork's behaviour.
 */
@SideOnly(Side.CLIENT)
public class ToolbeltInputHandler {

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player == null || mc.currentScreen != null) return;

        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;

        ItemStack held = player.getHeldItemMainhand();
        if (held.getItem() instanceof ItemToolbelt belt) {
            belt.changeSelectedToolMousewheel(wheel, held);
        }
    }
}
