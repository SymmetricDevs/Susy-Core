package supersymmetry.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import supersymmetry.Supersymmetry;
import supersymmetry.SusyConfig;
import supersymmetry.common.item.ItemToolbelt;

/**
 * Second hotbar above the vanilla one showing the toolbelt's slots and its selection, bumps status bars.
 * Each slot goes through the same two passes the vanilla hotbar uses. The overlay pass is where
 * GT's ASM hook draws tool bars: durability, and the blue charge bar for electric tools.
 */
@SideOnly(Side.CLIENT)
public class ToolbeltHudRenderer {

    private static final ResourceLocation WIDGETS = new ResourceLocation("textures/gui/widgets.png");
    private static final int OFFSET = 31;

    /** Hides or shows the row and remembers the choice. Client-only, so it does not touch the belt. */
    public static void toggleVisibility() {
        SusyConfig.hideToolbeltHud = !SusyConfig.hideToolbeltHud;
        // writes the changed field back to the .cfg and saves it
        ConfigManager.sync(Supersymmetry.MODID, Config.Type.INSTANCE);
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) return;
        // before the height bumps below: reserving the row and not drawing it leaves a gap
        if (SusyConfig.hideToolbeltHud) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) return;

        ItemStack stack = player.getHeldItemMainhand();
        if (!(stack.getItem() instanceof ItemToolbelt belt)) return;
        if (!(mc.ingameGUI instanceof GuiIngameForge gui)) return;

        ScaledResolution resolution = event.getResolution();
        int slots = Math.min(9, belt.getSlotCount(stack));
        GuiIngameForge.left_height += OFFSET - 6;
        if (slots > 4) GuiIngameForge.right_height += OFFSET - 6;

        int center = resolution.getScaledWidth() / 2;
        int top = resolution.getScaledHeight() - 22 - OFFSET;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(WIDGETS);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        gui.drawTexturedModalRect(center - 91, top, 0, 0, slots * 20 - 18, 22);
        gui.drawTexturedModalRect(center - 91 + slots * 20 - 18, top, 162, 0, 20, 22);

        int selected = belt.getSelectedSlot(stack);
        if (selected != -1) {
            gui.drawTexturedModalRect(center - 91 - 1 + selected * 20, top - 1, 0, 22, 24, 24);
        }

        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();

        RenderItem renderItem = mc.getRenderItem();
        for (int i = 0; i < slots; ++i) {
            ItemStack tool = belt.getToolInSlot(stack, i);
            if (tool.isEmpty()) continue;
            int x = center - 90 + i * 20 + 2;
            int y = resolution.getScaledHeight() - 19 - OFFSET;
            renderItem.renderItemAndEffectIntoGUI(tool, x, y);
            renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, tool, x, y, null);
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
    }
}
