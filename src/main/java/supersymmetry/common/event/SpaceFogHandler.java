package supersymmetry.common.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import supersymmetry.api.space.dimension.WorldProviderSpace;

@SideOnly(Side.CLIENT)
public class SpaceFogHandler {

    @SubscribeEvent
    public void onFogColors(EntityViewRenderEvent.FogColors event) {
        WorldProvider provider = Minecraft.getMinecraft().world.provider;
        if (provider instanceof WorldProviderSpace) {
            event.setRed(0f);
            event.setGreen(0f);
            event.setBlue(0f);
        }
    }

    @SubscribeEvent
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        WorldProvider provider = Minecraft.getMinecraft().world.provider;
        if (provider instanceof WorldProviderSpace) {
            event.setDensity(0f);
            event.setCanceled(true);
        }
    }
}
