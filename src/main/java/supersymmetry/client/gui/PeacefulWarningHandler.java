package supersymmetry.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import supersymmetry.Supersymmetry;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = net.minecraftforge.fml.relauncher.Side.CLIENT)
public class PeacefulWarningHandler {

    @SubscribeEvent
    public static void onButtonPressed(GuiScreenEvent.ActionPerformedEvent.Post event) {
        GuiScreen gui = event.getGui();

        if (!(gui instanceof GuiOptions)) return;
        if (event.getButton().id != 108) return;

        Minecraft mc = Minecraft.getMinecraft();

        if (mc.world == null) return;

        if (mc.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
            String raw = I18n.format("ui.susy.settings.peaceful_button.ln2");
            String formatted = raw.replace("\\n", "\n");

            mc.displayGuiScreen(new GuiYesNo(
                    (result, id) -> {
                        if (!result) {
                            mc.world.getWorldInfo().setDifficulty(EnumDifficulty.EASY);
                        }
                        mc.displayGuiScreen(gui);
                    },
                    I18n.format("ui.susy.settings.peaceful_button.ln1"),
                    formatted,
                    0));
        }
    }

    @SubscribeEvent
    public static void onDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.getGui() instanceof net.minecraft.client.gui.GuiOptions)) return;

        Minecraft mc = Minecraft.getMinecraft();

        if (mc.world == null) return;

        if (mc.world.getDifficulty() == net.minecraft.world.EnumDifficulty.PEACEFUL) {

            int x = event.getGui().width / 2 + 122;
            int y = event.getGui().height / 6 - 6;

            mc.fontRenderer.drawString("⚠", x, y, 0xFF5555);
        }
    }
}
