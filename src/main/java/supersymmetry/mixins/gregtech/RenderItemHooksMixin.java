package supersymmetry.mixins.gregtech;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.asm.hooks.RenderItemHooks;
import gregtech.client.utils.ToolChargeBarRenderer;
import supersymmetry.common.item.ItemToolbelt;

/**
 * GT draws durability and charge bars itself: its ASM transformer injects a call to
 * RenderItemHooks.renderElectricBar into vanilla's RenderItem, which asks ToolChargeBarRenderer for
 * the bars of the stack in the slot. A toolbelt is an IGTTool but is not electric and has no
 * durability of its own, so GT would ask the belt and draw nothing for the tool inside it.
 * <p>
 * Handing the selected stack to the same renderer gives the belt exactly the bars that stack would
 * draw in that slot - green durability, the blue charge bar, or a spray can's coloured uses bar -
 * everywhere GT's bars appear: the hotbar, the inventory, and the belt row above them.
 */
@Mixin(value = RenderItemHooks.class, remap = false)
public class RenderItemHooksMixin {

    @Inject(method = "renderElectricBar", at = @At("HEAD"), cancellable = true)
    private static void susy$renderToolbeltBars(ItemStack stack, int xPosition, int yPosition, CallbackInfo ci) {
        if (!(stack.getItem() instanceof ItemToolbelt belt)) return;
        ItemStack selected = belt.getSelectedTool(stack);
        if (selected.isEmpty()) return;
        if (selected.getItem() instanceof IGTTool tool) {
            ToolChargeBarRenderer.renderBarsTool(tool, selected, xPosition, yPosition);
            ci.cancel();
        } else if (selected.getItem() instanceof MetaItem<?> meta) {
            // Spray cans and other GT metaitems carry their own uses bar, with their own colours.
            ToolChargeBarRenderer.renderBarsItem(meta, selected, xPosition, yPosition);
            ci.cancel();
        }
    }
}
