package supersymmetry.network;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextFormatting;

public class GuiMissingMods extends GuiScreen {

    private final List<String> missingModNames;

    public GuiMissingMods(List<String> missingModNames) {
        this.missingModNames = missingModNames;
    }

    @Override
    public void initGui() {
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height - 38, "I Understand"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        this.drawCenteredString(fontRenderer, TextFormatting.RED + "⚠ Server Missing Required Mods ⚠",
                this.width / 2, 20, 0xFFFFFF);

        this.drawCenteredString(fontRenderer, "This SUSY server is missing mods. Expect broken or missing content:",
                this.width / 2, 36, 0xAAAAAA);

        drawRect(this.width / 2 - 120, 46, this.width / 2 + 120, 47, 0xFF555555);

        int y = 54;
        for (String name : missingModNames) {
            this.drawCenteredString(fontRenderer, TextFormatting.YELLOW + "• " + TextFormatting.WHITE + name,
                    this.width / 2, y, 0xFFFFFF);
            y += 12;
        }

        this.drawCenteredString(fontRenderer,
                TextFormatting.GRAY + "You may continue playing, but some features will not work correctly.",
                this.width / 2, this.height - 52, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.mc.displayGuiScreen(null); // dismiss, return to game
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
