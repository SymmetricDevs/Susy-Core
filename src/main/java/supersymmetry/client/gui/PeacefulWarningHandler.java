package supersymmetry.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import supersymmetry.Supersymmetry;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class PeacefulWarningHandler {

    @SubscribeEvent
    public static void onButtonPressed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        GuiScreen gui = event.getGui();

        if (!(gui instanceof GuiOptions)) return;
        if (event.getButton().id == 108) {
            Minecraft mc = Minecraft.getMinecraft();
            EnumDifficulty current = mc.world.getDifficulty();
            EnumDifficulty next = EnumDifficulty.byId((current.getId() + 1) % 4);
            if (next == EnumDifficulty.PEACEFUL) {

                // cancel the original button press
                event.setCanceled(true);
                mc.displayGuiScreen(new GuiYesNo(
                        (result, id) -> {
                            if (result) {
                                mc.world.getWorldInfo().setDifficulty(EnumDifficulty.PEACEFUL);
                            }
                            mc.displayGuiScreen(gui);
                        },
                        "Change to Peaceful?",
                        "Supersymmetry is intended to be played at difficulty Easy or higher.\n" +
                                "If you do not like mobs attacking your base, use /gamerule doInvasions false instead.\n" +
                                "Only change this setting if you know what you're doing, as doing so may break parts of the game.",
                        0
                ));
            }
        }
    }
    @SubscribeEvent
    public static void onDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.getGui() instanceof net.minecraft.client.gui.GuiOptions)) return;

        Minecraft mc = Minecraft.getMinecraft();

        if (mc.world == null) return;

        if (mc.world.getDifficulty() == net.minecraft.world.EnumDifficulty.PEACEFUL) {

            // unfuck
            int x = event.getGui().width / 2 + 128;
            int y = event.getGui().height / 6 - 10;

            mc.fontRenderer.drawString("⚠", x, y, 0xFF5555);
        }
    }
}
