package supersymmetry.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import supersymmetry.common.item.ItemToolbelt;

/**
 * Second hotbar above the vanilla one showing the toolbelt's slots and its selection.
 * <p>
 * Ported from GTCEu-Reloaded 43140b63f (LGPL-3.0), adapted to render items through
 * RenderItem instead of the access-transformed GuiIngame helper.
 */
@SideOnly(Side.CLIENT)
public class ToolbeltHudRenderer {

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) return;

        ItemStack stack = player.getHeldItemMainhand();
        if (!(stack.getItem() instanceof ItemToolbelt belt)) return;

        ScaledResolution resolution = event.getResolution();
        int offset = 31;
        int slots = Math.min(9, belt.getSlotCount(stack));
        GuiIngameForge.left_height += offset - 6;
        if (slots > 4) GuiIngameForge.right_height += offset - 6;

        Gui gui = mc.ingameGUI;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(Gui.ICONS);
        int center = resolution.getScaledWidth() / 2;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        gui.drawTexturedModalRect(center - 91, resolution.getScaledHeight() - 22 - offset, 0, 0, slots * 20 - 18, 22);
        gui.drawTexturedModalRect(center - 91 + slots * 20 - 18, resolution.getScaledHeight() - 22 - offset, 162, 0, 20,
                22);

        int selected = belt.getSelectedSlot(stack);
        if (selected != -1) {
            gui.drawTexturedModalRect(center - 91 - 1 + selected * 20, resolution.getScaledHeight() - 22 - 1 - offset,
                    0,
                    22, 24, 24);
        }

        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();

        RenderItem renderItem = mc.getRenderItem();
        for (int i = 0; i < slots; ++i) {
            ItemStack tool = belt.getToolInSlot(stack, i);
            if (tool.isEmpty()) continue;
            int x = center - 90 + i * 20 + 2;
            int y = resolution.getScaledHeight() - 16 - 3 - offset;
            renderItem.renderItemAndEffectIntoGUI(tool, x, y);
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
    }
}
