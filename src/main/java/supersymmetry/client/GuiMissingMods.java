package supersymmetry.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.io.IOException;
import java.util.List;

public class GuiMissingMods extends GuiScreen {

    private final List<String[]> missingMods;  // each entry: { modid, displayName }

    public GuiMissingMods(List<String[]> missingMods) {
        this.missingMods = missingMods;
    }

    @Override
    public void initGui() {
        // "Back to Main Menu" button — same placement as Forge's mismatch screen
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height - 38,
                "Back to Main Menu"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        // Title
        String title = TextFormatting.RED + "Missing Required Mods";
        this.drawCenteredString(fontRenderer, title, this.width / 2, 16, 0xFFFFFF);

        // Subtitle
        String subtitle = "This server requires the following mods to be installed:";
        this.drawCenteredString(fontRenderer, subtitle, this.width / 2, 30, 0xAAAAAA);

        // Separator line (drawn as a dark rect)
        drawRect(this.width / 2 - 150, 42, this.width / 2 + 150, 43, 0xFF555555);

        // Column headers
        int tableX = this.width / 2 - 150;
        int y = 50;
        this.drawString(fontRenderer, TextFormatting.YELLOW + "Mod Name", tableX, y, 0xFFFFFF);
        this.drawString(fontRenderer, TextFormatting.YELLOW + "Mod ID", tableX + 200, y, 0xFFFFFF);
        y += 12;
        drawRect(tableX, y, tableX + 300, y + 1, 0xFF555555);
        y += 4;

        // Mod rows
        for (String[] mod : missingMods) {
            this.drawString(fontRenderer, TextFormatting.WHITE + mod[1], tableX, y, 0xFFFFFF);
            this.drawString(fontRenderer, TextFormatting.GRAY  + mod[0], tableX + 200, y, 0xFFFFFF);
            y += 12;
        }

        // Warning footer
        String warn1 = TextFormatting.RED + "You cannot connect to this server without these mods.";
        String warn2 = TextFormatting.GRAY + "Install the missing mods and restart the game, then reconnect.";
        this.drawCenteredString(fontRenderer, warn1, this.width / 2, this.height - 60, 0xFFFFFF);
        this.drawCenteredString(fontRenderer, warn2, this.width / 2, this.height - 48, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            // Return to main menu, same as Forge's mismatch screen
            this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiMainMenu());
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
