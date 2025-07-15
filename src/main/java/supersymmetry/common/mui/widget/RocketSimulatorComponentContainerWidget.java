package supersymmetry.common.mui.widget;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import org.apache.commons.lang3.reflect.FieldUtils;
import supersymmetry.api.SusyLog;

public class RocketSimulatorComponentContainerWidget extends AbstractWidgetGroup {
  public int rowSkip; // to keep track of the distance between widgets
  public static int rowSeparation = 18; // mc slot size hopefully

  public RocketSimulatorComponentContainerWidget(Position position, Size size) {
    super(position, size);
    this.setSize(size);
    this.setSelfPosition(position);
    this.setParentPosition(Position.ORIGIN);
  }

  public void addSlotList(LabelWidget text, HorizontalScrollableListWidget scrollableListWidget) {
    try {
      SusyLog.logger.info(
          "---init--- text pos {}, list pos {}",
          text.getPosition(),
          scrollableListWidget.getPosition());
      int scrollbarPadding =
          scrollableListWidget.sliderActive ? HorizontalScrollableListWidget.scrollPaneWidth : 0;

      SusyLog.logger.info(
          "rowSkip: {} :scrollbarPadding: {} :rowSeparation: {}",
          rowSkip,
          scrollbarPadding,
          rowSeparation);
      text.setSelfPosition(new Position(0, rowSkip + rowSeparation + scrollbarPadding));
      SusyLog.logger.info(
          "text position before: g {} self {}", text.getPosition(), text.getSelfPosition());
      this.addWidget(text);
      SusyLog.logger.info(
          "text position after: g {} self {}", text.getPosition(), text.getSelfPosition());
      var barpos =
          new Position(
              this.getSize().width - scrollableListWidget.getSize().width,
              rowSkip + rowSeparation + scrollbarPadding);

      SusyLog.logger.info(
          "barpos before: {}, list info after global {} self {} parent {} actual parent {}",
          barpos,
          scrollableListWidget.getPosition(),
          scrollableListWidget.getSelfPosition(),
          FieldUtils.readField(scrollableListWidget, "parentPosition", true),
          this.getPosition());

      scrollableListWidget.setParentPosition(Position.ORIGIN);
      scrollableListWidget.setSelfPosition(barpos);

      this.addWidget(scrollableListWidget);

      SusyLog.logger.info(
          "barpos after: {}, list info after global {} self {} parent {} actual parent {}",
          barpos,
          scrollableListWidget.getPosition(),
          scrollableListWidget.getSelfPosition(),
          FieldUtils.readField(scrollableListWidget, "parentPosition", true),
          this.getPosition());
      rowSkip += rowSeparation + scrollbarPadding;

      SusyLog.logger.info(
          "the container possition info after this mess: {} {} {}",
          this.getPosition(),
          this.getSelfPosition(),
          FieldUtils.readField(scrollableListWidget, "parentPosition", true));
    } catch (Exception e) {
    }
  }

  public void RemoveSlotLists() {
    this.clearAllWidgets();
    rowSkip = 0;
  }

  // @Override
  // public void detectAndSendChanges() {
  //   super.detectAndSendChanges();
  // }
}
