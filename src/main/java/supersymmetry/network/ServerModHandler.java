package supersymmetry.network;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import supersymmetry.Supersymmetry;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class ServerModHandler {

    private static final String[][] REQUIRED_MODS = {
            { "srparasites", "Scape and Run Parasites" },
            { "openglasses", "OpenGlasses" },
            { "rendertoolkit", "Commons0815 (renderToolkit)" },
            { "guitoolkit", "Commons0815 (guiToolkit)" }
    };

    @SubscribeEvent
    public static void onPlayerConnecting(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        StringBuilder missing = new StringBuilder();
        for (String[] mod : REQUIRED_MODS) {
            if (!Loader.isModLoaded(mod[0])) {
                if (missing.length() > 0) missing.append(", ");
                missing.append(mod[1]);
            }
        }

        if (missing.length() > 0) {
            event.getManager().closeChannel(new TextComponentString(
                    TextFormatting.RED +
                            "[Supersymmetry] This server is missing required mods and cannot accept connections.\n" +
                            TextFormatting.YELLOW + "Missing: " + missing));
        }
    }
}
