package supersymmetry.common.mui.widget;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.utils.RenderUtil;
import net.minecraft.util.math.MathHelper;

/** basically ScrollableListWidget but horizontal, mostly a copy */
public class HorizontalScrollableListWidget extends AbstractWidgetGroup {
  public static final int scrollPaneWidth = 10;

  protected int totalListWidth;
  protected int slotWidth;
  protected int scrollOffset;
  protected int lastMouseX;
  protected int lastMouseY;
  protected boolean draggedOnScrollBar;
  public boolean sliderActive = true;

  public HorizontalScrollableListWidget(int xPosition, int yPosition, int width, int height) {
    super(new Position(xPosition, yPosition), new Size(width, height));
  }

  public void setSliderOffset(float percent) {
    scrollOffset = (int) (getMaxScrollOffset() * percent);
  }

  public void setSliderActive(boolean state) {
    sliderActive = state;
  }

  @Override
  public void addWidget(Widget widget) {
    super.addWidget(widget);
  }

  @Override
  protected boolean recomputeSize() {
    updateElementPositions();
    return false;
  }

  private void addScrollOffset(int offset) {
    this.scrollOffset =
        MathHelper.clamp(scrollOffset + offset, 0, totalListWidth - getSize().width);
    updateElementPositions();
  }

  private boolean isOnScrollPane(int mouseX, int mouseY) {
    Position pos = getPosition();
    Size size = getSize();
    return isMouseOver(pos.x, pos.y - scrollPaneWidth, size.width, scrollPaneWidth, mouseX, mouseY)
        && this.sliderActive;
  }

  @Override
  protected void onPositionUpdate() {
    updateElementPositions();
  }

  private void updateElementPositions() {
    Position position = getPosition();
    int currentPosX = position.x - scrollOffset;
    int totalListWidth = 0;
    for (Widget widget : widgets) {
      Position childPosition = new Position(currentPosX, position.y);
      widget.setParentPosition(childPosition);
      currentPosX += widget.getSize().getHeight();
      totalListWidth += widget.getSize().getHeight();
    }
    this.totalListWidth = totalListWidth;
    this.slotWidth = widgets.isEmpty() ? 0 : totalListWidth / widgets.size();
  }

  @Override
  public void drawInForeground(int mouseX, int mouseY) {
    if (!isPositionInsideScissor(mouseX, mouseY)) {
      mouseX = Integer.MAX_VALUE;
      mouseY = Integer.MAX_VALUE;
    }
    super.drawInForeground(mouseX, mouseY);
  }

  private int getMaxScrollOffset() {
    return totalListWidth - getSize().width;
  }

  @Override
  public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
    if (!isPositionInsideScissor(mouseX, mouseY)) {
      mouseX = Integer.MAX_VALUE;
      mouseY = Integer.MAX_VALUE;
    }
    int finalMouseX = mouseX;
    int finalMouseY = mouseY;
    Position position = getPosition();
    Size size = getSize();
    int paneSize =
        scrollPaneWidth; //  ---##------- <- that slidey bit width, along with the bar itself
    if (sliderActive) {
      GuiTextures.SLIDER_BACKGROUND_VERTICAL.draw(
          position.x, position.y - paneSize, size.width, paneSize);
      int scrollSliderY = position.y - paneSize;

      int maxScrollOffset = getMaxScrollOffset();
      float scrollPercent = maxScrollOffset == 0 ? 0 : scrollOffset / (maxScrollOffset * 1.0f);
      int scrollSliderX = Math.round(position.x + size.width * scrollPercent);
      GuiTextures.SLIDER_ICON.draw(scrollSliderX, scrollSliderY + 1, paneSize - 4, paneSize - 2);
    } else {
      paneSize = 0;
    }
    RenderUtil.useScissor(
        position.x,
        position.y,
        size.width,
        size.height + paneSize,
        () -> super.drawInBackground(finalMouseX, finalMouseY, partialTicks, context));
  }

  @Override
  public boolean isWidgetClickable(final Widget widget) {
    if (!super.isWidgetClickable(widget)) {
      return false;
    }
    return isWidgetOverlapsScissor(widget);
  }

  private boolean isPositionInsideScissor(int mouseX, int mouseY) {
    return isMouseOverElement(mouseX, mouseY) && !isOnScrollPane(mouseX, mouseY);
  }

  private boolean isWidgetOverlapsScissor(Widget widget) {
    final Position position = widget.getPosition();
    final Size size = widget.getSize();
    final int x0 = position.x;
    final int y0 = position.y;
    final int x1 = position.x + size.width - 1;
    final int y1 = position.y + size.height - 1;
    return isPositionInsideScissor(x0, y0)
        || isPositionInsideScissor(x0, y1)
        || isPositionInsideScissor(x1, y0)
        || isPositionInsideScissor(x1, y1);
  }

  @Override
  public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
    if (isMouseOverElement(mouseX, mouseY)) {
      int direction = -MathHelper.clamp(wheelDelta, -1, 1);
      int moveDelta = direction * (slotWidth / 2);
      addScrollOffset(moveDelta);
      return true;
    }
    return false;
  }

  @Override
  public boolean mouseClicked(int mouseX, int mouseY, int button) {
    this.lastMouseX = mouseX;
    this.lastMouseY = mouseY;
    if (isOnScrollPane(mouseX, mouseY)) {
      this.draggedOnScrollBar = true;
    }
    if (isPositionInsideScissor(mouseX, mouseY)) {
      return super.mouseClicked(mouseX, mouseY, button);
    }
    return false;
  }

  @Override
  public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
    int mouseDelta = (mouseX - lastMouseX);
    this.lastMouseX = mouseX;
    this.lastMouseY = mouseY;
    if (draggedOnScrollBar) {
      addScrollOffset(mouseDelta);
      return true;
    }
    if (isPositionInsideScissor(mouseX, mouseY)) {
      return super.mouseDragged(mouseX, mouseY, button, timeDragged);
    }
    return false;
  }

  @Override
  public boolean mouseReleased(int mouseX, int mouseY, int button) {
    this.draggedOnScrollBar = false;
    if (isPositionInsideScissor(mouseX, mouseY)) {
      return super.mouseReleased(mouseX, mouseY, button);
    }
    return false;
  }
}
