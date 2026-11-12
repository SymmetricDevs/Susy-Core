package supersymmetry.client;

import java.util.function.BiConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

import supersymmetry.common.item.ItemToolbelt;

/**
 * Toolbelt input: sneak + scroll selects a slot, and three unbound keys select the previous or
 * next slot and hide the HUD row.
 * <p>
 * Every path runs the change on a copy of the held belt and puts the copy back into the hotbar
 * slot, because vanilla stores the highlighting stack without copying.
 */
@SideOnly(Side.CLIENT)
public class ToolbeltInputHandler {

    private static final KeyBinding PREVIOUS_SLOT = keybind("key.susy.toolbelt_previous");
    private static final KeyBinding NEXT_SLOT = keybind("key.susy.toolbelt_next");
    private static final KeyBinding TOGGLE_HUD = keybind("key.susy.toolbelt_hud");

    /** Unbound on purpose: these are shortcuts, and nothing else in the mod takes a key. */
    private static KeyBinding keybind(String description) {
        return new KeyBinding(description, KeyConflictContext.IN_GAME, Keyboard.KEY_NONE, "key.categories.susy");
    }

    public ToolbeltInputHandler() {
        ClientRegistry.registerKeyBinding(PREVIOUS_SLOT);
        ClientRegistry.registerKeyBinding(NEXT_SLOT);
        ClientRegistry.registerKeyBinding(TOGGLE_HUD);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMouseEvent(MouseEvent event) {
        if (event.getDwheel() == 0) return;

        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null || !player.isSneaking()) return;

        if (changeSelection(player, (belt, stack) -> belt.changeSelectedToolMousewheel(event.getDwheel(), stack))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        // the HUD toggle is harmless anywhere in game, but the bindings must not fire in a GUI
        if (mc.currentScreen != null || !mc.inGameHasFocus || mc.player == null) return;

        if (TOGGLE_HUD.isPressed()) {
            ToolbeltHudRenderer.toggleVisibility();
        }
        if (PREVIOUS_SLOT.isPressed()) {
            changeSelection(mc.player, (belt, stack) -> belt.changeSelectedToolStep(false, stack));
        }
        if (NEXT_SLOT.isPressed()) {
            changeSelection(mc.player, (belt, stack) -> belt.changeSelectedToolStep(true, stack));
        }
    }

    /**
     * Runs a selection change on a copy of the held belt and puts the copy back into the hotbar
     * slot. Does nothing when the main hand holds something other than a belt.
     *
     * @return whether a belt was held
     */
    private static boolean changeSelection(EntityPlayerSP player, BiConsumer<ItemToolbelt, ItemStack> change) {
        ItemStack held = player.getHeldItemMainhand();
        if (!(held.getItem() instanceof ItemToolbelt belt)) return false;

        ItemStack stack = held.copy();
        change.accept(belt, stack);
        InventoryPlayer inv = player.inventory;
        inv.mainInventory.set(inv.currentItem, stack);
        return true;
    }
}
