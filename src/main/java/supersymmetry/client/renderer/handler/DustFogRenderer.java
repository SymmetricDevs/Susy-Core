package supersymmetry.client.renderer.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


//WIP
//shit don't work but I'm getting close

@SideOnly(Side.CLIENT)
public class DustFogRenderer {

    public static float fogStrength = 0.0f;
    public static float targetFog = 0.0f;
    public static long lastUpdate = 0L;

    private static final long TIMEOUT_MS = 20000L;
    private static final float RAMP   = 0.15f;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getMinecraft().player == null) return;

        long now = System.currentTimeMillis();
        boolean receiving = (now - lastUpdate) <= TIMEOUT_MS;

        if (receiving) {
            System.out.println("rampup");
            fogStrength = Math.min(fogStrength + RAMP, targetFog);
        } else {
            targetFog    = Math.max(targetFog    - RAMP, 0.0f);
            fogStrength  = Math.max(fogStrength  - RAMP, 0.0f);
        }
    }

    public static void applyPacket(float strength) {
        if (strength > targetFog) targetFog = strength;
        lastUpdate = System.currentTimeMillis();
    }

    @SubscribeEvent
    public static void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (fogStrength <= 0.01f) return;

        GlStateManager.setFog(GlStateManager.FogMode.EXP);
        event.setDensity(0.02f + (fogStrength * 0.25f));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFogColors(EntityViewRenderEvent.FogColors event) {
        if (fogStrength <= 0.0f) return;

        float multiplier = 1.0f - (fogStrength * 0.85f);
        event.setRed(event.getRed()     * multiplier);
        event.setGreen(event.getGreen() * multiplier);
        event.setBlue(event.getBlue()   * multiplier);
    }
}
