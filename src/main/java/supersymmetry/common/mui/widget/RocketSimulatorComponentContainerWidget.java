package supersymmetry.common.mui.widget;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

public class RocketSimulatorComponentContainerWidget extends AbstractWidgetGroup {
  public int rows;
  public static int rowSeparation = 18; // mc slot size hopefully

  public RocketSimulatorComponentContainerWidget(Position position, Size size) {
    super(position, size);
  }

  public void addSlotList(LabelWidget text, HorizontalScrollableListWidget scrollableListWidget) {
    int scrollbarPadding =
        scrollableListWidget.sliderActive ? HorizontalScrollableListWidget.scrollPaneWidth : 0;
    var pos = this.getPosition();
    text.setSelfPosition(new Position(pos.x, rows * rowSeparation + pos.y));
    this.addWidget(text);
    scrollableListWidget.setSelfPosition(
        new Position(
            this.getSize().width - scrollableListWidget.getSize().width,
            rows * (rowSeparation + scrollbarPadding) + pos.y));
    this.addWidget(scrollableListWidget);
    rows++;
  }

  public void RemoveSlotLists() {
    this.clearAllWidgets();
    rows = 0;
  }

  @Override
  public void detectAndSendChanges() {
    super.detectAndSendChanges();
  }
}
