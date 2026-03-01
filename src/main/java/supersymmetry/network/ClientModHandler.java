package supersymmetry.network;

import static supersymmetry.network.ServerModHandler.REQUIRED_MODS;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiDisconnected;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import supersymmetry.Supersymmetry;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = Side.CLIENT)
public class ClientModHandler {

    @SubscribeEvent
    public static void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiDisconnected) {
            List<String> missing = new ArrayList<>();
            for (String[] mod : REQUIRED_MODS) {
                if (!Loader.isModLoaded(mod[0])) {
                    missing.add(mod[1]);
                }
            }
            if (!missing.isEmpty()) {
                event.setGui(new GuiMissingMods(missing));
            }
        }
    }
}
