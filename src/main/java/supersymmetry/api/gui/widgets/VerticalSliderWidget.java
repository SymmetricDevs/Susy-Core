package supersymmetry.api.gui.widgets;

import com.google.common.base.Preconditions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.function.FloatConsumer;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import supersymmetry.api.gui.SusyGuiTextures;

public class VerticalSliderWidget extends Widget {
    public static final BiFunction<String, Float, String> DEFAULT_TEXT_SUPPLIER = (name, value) -> {
        return I18n.format(name, new Object[]{value.intValue()});
    };
    private int sliderIconHeight = 8;

    private int the8 = 8;
    private int the4 = 4;

    private TextureArea backgroundArea;
    private TextureArea sliderIcon;
    private final BiFunction<String, Float, String> textSupplier;
    private int textColor;
    private final float min;
    private final float max;
    private final String name;
    private final FloatConsumer responder;
    private boolean isPositionSent;
    private String displayString;
    private float sliderPosition;
    public boolean isMouseDown;

    public VerticalSliderWidget(String name, int xPosition, int yPosition, int width, int height, float min, float max, float currentValue, FloatConsumer responder) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.backgroundArea = SusyGuiTextures.VERTICAL_SLIDER_BACKGROUND;
        this.sliderIcon = SusyGuiTextures.VERTICAL_SLIDER_ICON;
        this.textSupplier = DEFAULT_TEXT_SUPPLIER;
        this.textColor = 16777215;
        Preconditions.checkNotNull(responder, "responder");
        Preconditions.checkNotNull(name, "name");
        this.min = min;
        this.max = max;
        this.name = name;
        this.responder = responder;
        this.sliderPosition = (currentValue - min) / (max - min);
    }

    public VerticalSliderWidget setSliderIcon(@Nonnull TextureArea sliderIcon) {
        Preconditions.checkNotNull(sliderIcon, "sliderIcon");
        this.sliderIcon = sliderIcon;
        return this;
    }

    public VerticalSliderWidget setBackground(@Nullable TextureArea background) {
        this.backgroundArea = background;
        return this;
    }

    public VerticalSliderWidget setSliderHeight(int sliderHeight) {
        this.sliderIconHeight = sliderHeight;
        return this;
    }

    public VerticalSliderWidget setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public void detectAndSendChanges() {
        if (!this.isPositionSent) {
            this.writeUpdateInfo(1, (buffer) -> {
                buffer.writeFloat(this.sliderPosition);
            });
            this.isPositionSent = true;
        }

    }

    public float getSliderValue() {
        return this.min + (this.max - this.min) * this.sliderPosition;
    }

    protected String getDisplayString() {
        return (String)this.textSupplier.apply(this.name, this.getSliderValue());
    }

    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = this.getPosition();
        Size size = this.getSize();
        if (this.backgroundArea != null) {
            this.backgroundArea.draw((double)pos.x, (double)pos.y, size.width, size.height);
        }

        if (this.displayString == null) {
            this.displayString = this.getDisplayString();
        }

        this.sliderIcon.draw((double)pos.x, (double)(pos.y + (int)(this.sliderPosition * (float)(size.height - this.the8))), size.width, this.sliderIconHeight);

        //FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        //fontRenderer.drawString(this.displayString, pos.x + size.width / 2 - fontRenderer.getStringWidth(this.displayString) / 2, pos.y + size.height / 2 - fontRenderer.FONT_HEIGHT / 2, this.textColor);
    }

    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (this.isMouseDown) {
            Position pos = this.getPosition();
            Size size = this.getSize();
            this.sliderPosition = (float)(mouseY - (pos.y + this.the4)) / (float)(size.height - this.the8);
            if (this.sliderPosition < 0.0F) {
                this.sliderPosition = 0.0F;
            }

            if (this.sliderPosition > 1.0F) {
                this.sliderPosition = 1.0F;
            }

            this.displayString = this.getDisplayString();
            this.writeClientAction(1, (buffer) -> {
                buffer.writeFloat(this.sliderPosition);
            });
            return true;
        } else {
            return false;
        }
    }

    public void setSliderValue(float value) {
        this.sliderPosition = value;
        if (this.sliderPosition < 0.0F) {
            this.sliderPosition = 0.0F;
        }

        if (this.sliderPosition > 1.0F) {
            this.sliderPosition = 1.0F;
        }

        this.displayString = this.getDisplayString();
        this.writeClientAction(1, (buffer) -> {
            buffer.writeFloat(this.sliderPosition);
        });
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            Position pos = this.getPosition();
            Size size = this.getSize();
            this.sliderPosition = (float)(mouseY - (pos.y + this.the4)) / (float)(size.height - this.the8);
            if (this.sliderPosition < 0.0F) {
                this.sliderPosition = 0.0F;
            }

            if (this.sliderPosition > 1.0F) {
                this.sliderPosition = 1.0F;
            }

            this.displayString = this.getDisplayString();
            this.writeClientAction(1, (buffer) -> {
                buffer.writeFloat(this.sliderPosition);
            });
            this.isMouseDown = true;
            return true;
        } else {
            return false;
        }
    }

    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        this.isMouseDown = false;
        return false;
    }

    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.sliderPosition = buffer.readFloat();
            this.sliderPosition = MathHelper.clamp(this.sliderPosition, 0.0F, 1.0F);
            this.responder.apply(this.getSliderValue());
        }

    }

    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.sliderPosition = buffer.readFloat();
            this.sliderPosition = MathHelper.clamp(this.sliderPosition, 0.0F, 1.0F);
            this.displayString = this.getDisplayString();
        }

    }
}

