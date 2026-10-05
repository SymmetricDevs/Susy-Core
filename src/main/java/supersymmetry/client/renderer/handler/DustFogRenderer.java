package supersymmetry.client.renderer.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DustFogRenderer {

    private enum FogState { IDLE, RAMPING_UP, HOLDING, RAMPING_DOWN }
    private static FogState state       = FogState.IDLE;
    public  static float    fogStrength = 0.0f;

    private static final int   RAMP   = 80; //ticks
    private static final int   HOLD_TICKS      = 600; //30 sec, slightly more than the machine takes to cycle for seamless continuation

    private static int tickCounter = 0;
    public static void applyPacket() {
        switch (state) {
            case IDLE:
            case RAMPING_DOWN:
                state = FogState.RAMPING_UP;
                tickCounter = (int) (fogStrength * RAMP);
                break;

            case RAMPING_UP:
            case HOLDING:
                if (state == FogState.HOLDING) {
                    tickCounter = 0;
                }
                break;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getMinecraft().player == null) return;

        switch (state) {

            case IDLE:
                break;

            case RAMPING_UP:
                tickCounter++;
                fogStrength = Math.min((float) tickCounter / RAMP, 1.0f);
                if (tickCounter >= RAMP) {
                    fogStrength = 1.0f;
                    state       = FogState.HOLDING;
                    tickCounter = 0;
                }
                break;

            case HOLDING:
                tickCounter++;
                if (tickCounter >= HOLD_TICKS) {
                    state       = FogState.RAMPING_DOWN;
                    tickCounter = 0;
                }
                break;

            case RAMPING_DOWN:
                tickCounter++;
                fogStrength = 1.0f - ((float) tickCounter / RAMP);
                if (fogStrength <= 0.0f) {  // let the math decide when we're done + grace period for the game to actually catch up
                    fogStrength = 0.0f;
                    state       = FogState.IDLE;
                    tickCounter = 0;
                }
                System.out.println(fogStrength);
                break;
        }
    }

    @SubscribeEvent
    public static void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (state == FogState.IDLE) return;

        GlStateManager.setFog(GlStateManager.FogMode.EXP2);
        event.setDensity(fogStrength * 0.25f);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onFogColors(EntityViewRenderEvent.FogColors event) {
        if (state == FogState.IDLE) return;

        float multiplier = 1.0f - (fogStrength * 0.85f);
        event.setRed  (event.getRed()   * multiplier);
        event.setGreen(event.getGreen() * multiplier);
        event.setBlue (event.getBlue()  * multiplier);
    }
}
