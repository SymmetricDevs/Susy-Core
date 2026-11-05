package supersymmetry.mixins.minecraft;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.client.utils.ToolChargeBarRenderer;
import supersymmetry.common.item.ItemToolbelt;

/**
 * Draws the selected stack's own bars on the belt icon, in every slot that renders it.
 * <p>
 * GT draws tool bars from RenderItemHooks.renderElectricBar, which its ASM transformer injects into
 * this very method. That hook asks the stack in the slot, and a belt is neither electric nor has a
 * durability of its own, so a belt only ever got the generic durability bar from
 * getDurabilityForDisplay.
 * This runs after GT's pass, so the belt gets exactly the bars the selected stack would draw itself.
 * The hook is on RenderItem rather than on GT's bar classes because GT's transformer loads
 * gregtech.asm.hooks.RenderItemHooks during renderer construction, before the late mixin configs
 * exist, so a mixin on that class is never applied.
 */
@Mixin(RenderItem.class)
public class RenderItemToolbeltMixin {

    @Inject(method = "renderItemOverlayIntoGUI", at = @At("TAIL"))
    private void susy$toolbeltBars(FontRenderer fontRenderer, ItemStack stack, int x, int y, String text,
                                   CallbackInfo ci) {
        if (!(stack.getItem() instanceof ItemToolbelt belt)) return;
        ItemStack selected = belt.getSelectedTool(stack);
        if (selected.isEmpty()) return;
        if (selected.getItem() instanceof IGTTool tool) {
            // GT's own pass already drew the durability bar from getDurabilityForDisplay; only the
            // charge bar is missing, and renderBarsTool draws both, so it is drawn again on top.
            if (!tool.isElectric()) return;
            ToolChargeBarRenderer.renderBarsTool(tool, selected, x, y);
        } else if (selected.getItem() instanceof MetaItem<?> meta) {
            // Spray cans and other metaitems carry their uses bar with their own colours.
            ToolChargeBarRenderer.renderBarsItem(meta, selected, x, y);
        }
    }
}
