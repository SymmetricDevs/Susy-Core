package supersymmetry.client.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = "susy")
public class ActiveFluidVisualHandler {

    private static final ResourceLocation SUBMERGED_OVERLAY = new ResourceLocation(
            "textures/misc/underwater.png");

    private static final Map<BlockPos, FluidInfo> activeFluids = new ConcurrentHashMap<>();

    public static class FluidInfo {

        public final int color;
        public final int displaySize;

        public FluidInfo(int color, int displaySize) {
            this.color = color;
            this.displaySize = displaySize;
        }
    }

    public static void registerFluid(BlockPos pos, int color, int displaySize) {
        activeFluids.put(pos.toImmutable(), new FluidInfo(color, displaySize));
    }

    public static void unregisterFluid(BlockPos pos) {
        activeFluids.remove(pos.toImmutable());
    }

    public static void clearAll() {
        activeFluids.clear();
    }

    private static double getEffectiveHeight(FluidInfo info) {
        return info.displaySize > 0 ? info.displaySize : 3.0 / 16.0;
    }

    private static FluidInfo getFluidAtPlayer(Entity entity) {
        if (activeFluids.isEmpty()) return null;
        if (!(entity instanceof EntityPlayer)) return null;
        EntityPlayer player = (EntityPlayer) entity;
        double x = player.posX;
        double z = player.posZ;

        for (Map.Entry<BlockPos, FluidInfo> entry : activeFluids.entrySet()) {
            BlockPos pos = entry.getKey();
            FluidInfo info = entry.getValue();
            double effectiveHeight = getEffectiveHeight(info);
            double fluidTop = pos.getY() + 1 + effectiveHeight;
            double y = info.displaySize > 0 ?
                    player.posY + player.getEyeHeight() : player.posY;

            if (x >= pos.getX() && x < pos.getX() + 1 &&
                    y >= pos.getY() + 1 && y < fluidTop &&
                    z >= pos.getZ() && z < pos.getZ() + 1) {
                return info;
            }
        }
        return null;
    }

    private static float[] colorToRGB(int color) {
        return new float[]{
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F
        };
    }

    @SubscribeEvent
    public static void onFOVModifier(EntityViewRenderEvent.FOVModifier event) {
        FluidInfo info = getFluidAtPlayer(event.getEntity());
        if (info == null) return;
        event.setFOV(event.getFOV() * 60.0F / 70.0F);
    }

    @SubscribeEvent
    public static void onBlockOverlayRender(RenderBlockOverlayEvent event) {
        if (event.getOverlayType() != RenderBlockOverlayEvent.OverlayType.WATER) return;

        EntityPlayer player = event.getPlayer();
        FluidInfo info = getFluidAtPlayer(player);
        if (info == null) return;

        float[] rgb = colorToRGB(info.color);
        float brightness = player.getBrightness();

        Minecraft.getMinecraft().getTextureManager().bindTexture(SUBMERGED_OVERLAY);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();

        GlStateManager.color(brightness * rgb[0], brightness * rgb[1], brightness * rgb[2], 0.5F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();

        float yaw = -player.rotationYaw / 64.0F;
        float pitch = player.rotationPitch / 64.0F;
        buf.begin(7, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-1.0D, -1.0D, -0.5D).tex(4.0F + yaw, 4.0F + pitch).endVertex();
        buf.pos(1.0D, -1.0D, -0.5D).tex(0.0F + yaw, 4.0F + pitch).endVertex();
        buf.pos(1.0D, 1.0D, -0.5D).tex(0.0F + yaw, 0.0F + pitch).endVertex();
        buf.pos(-1.0D, 1.0D, -0.5D).tex(4.0F + yaw, 0.0F + pitch).endVertex();
        tessellator.draw();

        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFogColor(EntityViewRenderEvent.FogColors event) {
        FluidInfo info = getFluidAtPlayer(event.getEntity());
        if (info == null) return;

        float[] rgb = colorToRGB(info.color);
        event.setRed(rgb[0]);
        event.setGreen(rgb[1]);
        event.setBlue(rgb[2]);
    }

    @SubscribeEvent
    public static void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        FluidInfo info = getFluidAtPlayer(event.getEntity());
        if (info == null) return;

        GlStateManager.setFog(GlStateManager.FogMode.EXP);
        event.setDensity(0.1F);
    }
}
