package supersymmetry.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import supersymmetry.Supersymmetry;
import supersymmetry.api.SusyLog;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class ServerModHandler {

    public static final String[][] REQUIRED_MODS = {
            { "srparasites", "Scape and Run Parasites" },
            { "openglasses", "OpenGlasses" },
            { "rendertoolkit", "Commons0815 (renderToolkit)" },
            { "guitoolkit", "Commons0815 (guiToolkit)" }
    };

    @SubscribeEvent
    public static void onClientConnect(net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        List<String> missing = new ArrayList<>();
        for (String[] mod : REQUIRED_MODS) {
            if (!Loader.isModLoaded(mod[0])) {
                missing.add(mod[1]);
            }
        }

        SusyLog.logger.info("SUSY MOD CHECK: found {} missing mods: {}", missing.size(), missing);

        if (!missing.isEmpty()) {
            event.getManager().closeChannel(
                    new net.minecraft.util.text.TextComponentString(
                            "§cThis server is missing required mods:\n§e" + String.join("\n", missing) +
                                    "\n§7Please contact the server owner."));
        }
    }
}
