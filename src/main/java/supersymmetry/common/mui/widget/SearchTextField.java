package supersymmetry.common.mui.widget;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.TextFieldWidget2;

/**
 * Search-bar text field used by the Extended Chisel Maker GUI.
 * <p>
 * Draws its own recessed dark panel behind the text (the same {@link GuiTextures#DISPLAY} screen
 * style as the item grid below) so the bar visibly reads as a clickable search field against the
 * light GT GUI background, with light text on top for contrast.
 */
public class SearchTextField extends TextFieldWidget2 {

    private static final int TEXT_COLOR = 0xFFFFFF;

    /** Extra space between the bar's left border and the text. */
    private static final int TEXT_PADDING = 3;

    public SearchTextField(int x, int y, int width, int height, Supplier<String> supplier, Consumer<String> setter) {
        super(x, y, width, height, supplier, setter);
        setTextColor(TEXT_COLOR);
    }

    @Override
    protected int getTextX() {
        return super.getTextX() + TEXT_PADDING;
    }

    @Override
    protected int toRenderTextIndex(int originalTextIndex) {
        return Math.min(originalTextIndex, getText().length());
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Widget.drawSolidRect(getPosition().x, getPosition().y, getSize().width, getSize().height, 0xFF2D2D2D);
        GuiTextures.DISPLAY.draw(getPosition().x, getPosition().y, getSize().width, getSize().height);
        // The base widget draws the text at the top of the box; shift the whole render (text, cursor,
        // selection) down so it sits vertically centered within the bar.
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int textYOffset = Math.max(0, (getSize().height - fontRenderer.FONT_HEIGHT) / 2);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, textYOffset, 0);
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        GlStateManager.popMatrix();
    }
}
