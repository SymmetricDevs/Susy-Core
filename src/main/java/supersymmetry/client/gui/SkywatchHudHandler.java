package supersymmetry.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import org.lwjgl.opengl.GL11;

import supersymmetry.Supersymmetry;
import supersymmetry.common.item.behavior.SkywatchChipBehaviour;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = Side.CLIENT)
public class SkywatchHudHandler {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null || mc.world == null || mc.gameSettings.hideGUI) return;

        NBTTagCompound tag = findActiveTag(player, mc.world.getTotalWorldTime());
        if (tag == null) return;

        double dx = tag.getDouble(SkywatchChipBehaviour.TAG_X) - player.posX;
        double dz = tag.getDouble(SkywatchChipBehaviour.TAG_Z) - player.posZ;

        float partial = event.getPartialTicks();
        float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partial;
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float relative = MathHelper.wrapDegrees(targetYaw - yaw);

        ScaledResolution res = event.getResolution();

        GlStateManager.pushMatrix();
        GlStateManager.translate(res.getScaledWidth() / 2.0F, 48.0F, 0.0F);
        GlStateManager.rotate(relative, 0.0F, 0.0F, 1.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(1.0F, 0.25F, 0.2F, 0.9F);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
        // head
        buf.pos(0, -20, 0).endVertex();
        buf.pos(-10, -4, 0).endVertex();
        buf.pos(10, -4, 0).endVertex();
        // shaft
        buf.pos(-3, -4, 0).endVertex();
        buf.pos(-3, 14, 0).endVertex();
        buf.pos(3, 14, 0).endVertex();
        buf.pos(-3, -4, 0).endVertex();
        buf.pos(3, 14, 0).endVertex();
        buf.pos(3, -4, 0).endVertex();
        tess.draw();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private static NBTTagCompound findActiveTag(EntityPlayer player, long worldTime) {
        for (ItemStack stack : player.inventory.mainInventory) {
            NBTTagCompound tag = activeTag(stack, worldTime);
            if (tag != null) return tag;
        }
        for (ItemStack stack : player.inventory.offHandInventory) {
            NBTTagCompound tag = activeTag(stack, worldTime);
            if (tag != null) return tag;
        }
        return null;
    }

    private static NBTTagCompound activeTag(ItemStack stack, long worldTime) {
        if (stack.isEmpty() || !SkywatchChipBehaviour.isAlert(stack)) return null;
        NBTTagCompound tag = stack.getSubCompound(SkywatchChipBehaviour.TAG);
        if (tag == null || tag.getLong(SkywatchChipBehaviour.TAG_ARROW_UNTIL) <= worldTime) return null;
        return tag;
    }
}
