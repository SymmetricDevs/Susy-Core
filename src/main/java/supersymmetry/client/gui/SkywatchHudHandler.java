package supersymmetry.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import org.lwjgl.opengl.GL11;

import supersymmetry.Supersymmetry;
import supersymmetry.common.item.behavior.SkywatchChipBehaviour;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = Side.CLIENT)
public class SkywatchHudHandler {

    private static final double DISTANCE = 3.0D;
    private static final double BELOW_CROSSHAIR = 0.6D;
    private static final float SCALE = 0.9F;


    @SubscribeEvent
    public static void onRenderWorld(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null || mc.world == null || mc.gameSettings.hideGUI) return;

        NBTTagCompound tag = findActiveTag(player, mc.world.getTotalWorldTime());
        if (tag == null) return;

        float pt = event.getPartialTicks();

        double px = player.lastTickPosX + (player.posX - player.lastTickPosX) * pt;
        double pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * pt;
        double dx = tag.getDouble(SkywatchChipBehaviour.TAG_X) - px;
        double dz = tag.getDouble(SkywatchChipBehaviour.TAG_Z) - pz;
        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

        float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * pt;
        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * pt;
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        double cosP = Math.cos(pitchRad);
        double sinP = Math.sin(pitchRad);
        double sinY = Math.sin(yawRad);
        double cosY = Math.cos(yawRad);

        double lx = -sinY * cosP;
        double ly = -sinP;
        double lz = cosY * cosP;

        double ux = -sinY * sinP;
        double uy = cosP;
        double uz = cosY * sinP;

        double bob = Math.sin((mc.world.getTotalWorldTime() + pt) * 0.15D) * 0.05D;

        GlStateManager.pushMatrix();
        GlStateManager.translate(
                lx * DISTANCE - ux * (BELOW_CROSSHAIR - bob),
                ly * DISTANCE - uy * (BELOW_CROSSHAIR - bob),
                lz * DISTANCE - uz * (BELOW_CROSSHAIR - bob));
        GlStateManager.rotate(-targetYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(SCALE, SCALE, SCALE);

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.color(1.0F, 0.25F, 0.2F, 0.85F);
        buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
        // head
        buf.pos(0.0D, 0.0D, 1.0D).endVertex();
        buf.pos(-0.5D, 0.0D, 0.35D).endVertex();
        buf.pos(0.5D, 0.0D, 0.35D).endVertex();
        // shaft
        buf.pos(-0.15D, 0.0D, 0.35D).endVertex();
        buf.pos(-0.15D, 0.0D, -0.6D).endVertex();
        buf.pos(0.15D, 0.0D, -0.6D).endVertex();
        buf.pos(-0.15D, 0.0D, 0.35D).endVertex();
        buf.pos(0.15D, 0.0D, -0.6D).endVertex();
        buf.pos(0.15D, 0.0D, 0.35D).endVertex();
        tess.draw();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
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
