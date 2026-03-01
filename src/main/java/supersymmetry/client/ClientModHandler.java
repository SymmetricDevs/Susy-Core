package supersymmetry.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;

import supersymmetry.Supersymmetry;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = Side.CLIENT)
public class ClientModHandler {

    public static final String[][] REQUIRED_MODS = {
            { "srparasites",    "Scape and Run Parasites"     },
            { "openglasses",    "OpenGlasses"                 },
            { "rendertoolkit",  "Commons0815 (renderToolkit)" },
            { "guitoolkit",     "Commons0815 (guiToolkit)"    }
    };

    @SubscribeEvent
    public static void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        List<String[]> missing = new ArrayList<>();
        for (String[] mod : REQUIRED_MODS) {
            if (!Loader.isModLoaded(mod[0])) {
                missing.add(mod);
            }
        }

        if (!missing.isEmpty()) {
            // Kick the client from the connection immediately
            event.getManager().closeChannel(
                    new TextComponentString("[Supersymmetry] Connection refused: missing required mods.")
            );

            // Then show the blocking GUI
            Minecraft mc = Minecraft.getMinecraft();
            mc.addScheduledTask(() -> mc.displayGuiScreen(new GuiMissingMods(missing)));
        }
    }
}
