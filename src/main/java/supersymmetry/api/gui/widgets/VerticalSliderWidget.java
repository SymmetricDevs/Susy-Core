package supersymmetry.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.widgets.SliderWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.function.FloatConsumer;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.mixins.gregtech.SliderWidgetAccessor;

public class VerticalSliderWidget extends SliderWidget {

    public final SliderWidgetAccessor self;
    protected int sliderHeight = 8;

    public VerticalSliderWidget(String name, int xPosition, int yPosition, int width, int height, float min, float max, float currentValue, FloatConsumer responder) {
        super(name, xPosition, yPosition, width, height, min, max, currentValue, responder);
        this.setBackground(SusyGuiTextures.VERTICAL_SLIDER_BACKGROUND);
        this.setSliderIcon(SusyGuiTextures.VERTICAL_SLIDER_ICON);
        this.self = (SliderWidgetAccessor) this;
    }

    public VerticalSliderWidget setSliderHeight(int sliderHeight) {
        this.sliderHeight = sliderHeight;
        return this;
    }

    public void setSliderValue(float value) {
        self.setSliderPosition(value);
        if (self.getSliderPosition() < 0.0F) {
            self.setSliderPosition(0.0F);
        }

        if (self.getSliderPosition() > 1.0F) {
            self.setSliderPosition(1.0F);
        }

        self.setDisplayStringRaw(getDisplayString());
        this.writeClientAction(1, (buffer) -> buffer.writeFloat(self.getSliderPosition()));
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = this.getPosition();
        Size size = this.getSize();
        if (self.getBackgroundArea() != null) {
            self.getBackgroundArea().draw(pos.x, pos.y, size.width, size.height);
        }

        if (self.getDisplayStringRaw() == null) {
            self.setDisplayStringRaw(getDisplayString());
        }

        self.getSliderIcon().draw(pos.x, pos.y + (int) (self.getSliderPosition() * (float) (size.height - 8/*TODO: is this just sliderHeight?*/)), size.width, this.sliderHeight);

        //FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        //fontRenderer.drawString(this.displayString, pos.x + size.width / 2 - fontRenderer.getStringWidth(this.displayString) / 2, pos.y + size.height / 2 - fontRenderer.FONT_HEIGHT / 2, this.textColor);
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (this.isMouseDown) {
            Position pos = this.getPosition();
            Size size = this.getSize();
            this.setSliderValue((float) (mouseY - (pos.y + 4)) / (float) (size.height - 8));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            Position pos = this.getPosition();
            Size size = this.getSize();
            this.setSliderValue((float) (mouseY - (pos.y + 4)) / (float) (size.height - 8));
            this.isMouseDown = true;
            return true;
        } else {
            return false;
        }
    }
}
