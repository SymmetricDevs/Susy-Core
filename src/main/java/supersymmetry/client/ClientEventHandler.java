package supersymmetry.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;

import supersymmetry.Supersymmetry;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = Side.CLIENT)
public class ClientEventHandler {

    private static final String[][] REQUIRED_MODS = {
            { "srparasites", "Scape and Run Parasites" },
            { "openglasses", "OpenGlasses" },
            { "rendertoolkit", "Commons0815 (renderToolkit)" },
            { "guitoolkit", "Commons0815 (guiToolkit)" }
    };

    @SubscribeEvent
    public static void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            for (String[] mod : REQUIRED_MODS) {
                if (!Loader.isModLoaded(mod[0])) {
                    TextComponentString msg = new TextComponentString(
                            TextFormatting.RED + "[Supersymmetry] WARNING: Missing required mod: " +
                                    TextFormatting.YELLOW + mod[1] +
                                    TextFormatting.RED + ". The server expects this mod — expect broken content!");
                    Minecraft.getMinecraft().player.sendMessage(msg);
                }
            }
        });
    }
}
