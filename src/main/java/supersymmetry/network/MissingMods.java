package supersymmetry.network;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = "supersymmetry")
public class MissingMods {

    public static final String[][] REQUIRED_MODS = {
            { "srparasites", "Scape and Run Parasites" },
            { "openglasses", "OpenGlasses" },
            { "rendertoolkit", "Commons0815 (renderToolkit)" },
    };

    /**
     * Fires on the PHYSICAL CLIENT when the player connects to any server.
     * Warns the player if their client is missing any required mods.
     */
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onClientJoinServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        List<String> missingMods = getMissingMods();
        if (missingMods.isEmpty()) return;

        // Delay by 1 tick so the player is fully in-game when the message is sent
        net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
            EntityPlayerSP player = net.minecraft.client.Minecraft.getMinecraft().player;
            if (player == null) return;

            player.sendMessage(new TextComponentString(
                    "\u00a7c\u00a7l[WARNING] \u00a7r\u00a7cThis server requires mods that are \u00a7l\u00a7nmissing\u00a7r\u00a7c on your client:"));
            for (String modName : missingMods) {
                player.sendMessage(new TextComponentString("\u00a7c  - " + modName));
            }
            player.sendMessage(new TextComponentString(
                    "\u00a7c\u00a7lPlease install the missing mods to avoid issues!"));
        });
    }

    /**
     * Fires on the SERVER when any player logs in.
     * Warns the joining player if the server itself is missing any required mods.
     */
    @SubscribeEvent
    public static void onServerPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        if (player == null) return;

        List<String> missingMods = getMissingMods();
        if (missingMods.isEmpty()) return;

        player.sendMessage(new TextComponentString(
                "\u00a7e\u00a7l[SERVER WARNING] \u00a7r\u00a7eThis server is missing required mods:"));
        for (String modName : missingMods) {
            player.sendMessage(new TextComponentString("\u00a7e  - " + modName));
        }
        player.sendMessage(new TextComponentString(
                "\u00a7e\u00a7lSome features may not work correctly on this server."));
    }

    /**
     * Returns a list of friendly mod names from REQUIRED_MODS that are not currently loaded.
     * Works on both client and server since Loader is available on both sides.
     */
    private static List<String> getMissingMods() {
        List<String> missing = new ArrayList<>();
        for (String[] mod : REQUIRED_MODS) {
            String modId = mod[0];
            String modName = mod[1];
            if (!Loader.isModLoaded(modId)) {
                missing.add(modName);
            }
        }
        return missing;
    }
}
