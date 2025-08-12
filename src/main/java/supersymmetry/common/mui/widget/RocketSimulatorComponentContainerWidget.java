package supersymmetry.common.mui.widget;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.resources.I18n;

public class RocketSimulatorComponentContainerWidget extends AbstractWidgetGroup {
  public static int rowSeparation = 18; // mc slot size hopefully
  public int rowSkip; // to keep track of the distance between widgets
  // public Map<String, listEntry> linkedWidgets = new HashMap<>();
  public Map<String, RocketComponentEntryWidget> components = new HashMap<>();
  public int textColor = 0x404040;

  public RocketSimulatorComponentContainerWidget(Position position, Size size) {
    super(position, size);
    this.setSize(size);
    this.setSelfPosition(position);
    this.setParentPosition(Position.ORIGIN);
  }

  public int getTextColor() {
    return textColor;
  }

  public void setTextColor(int textColor) {
    this.textColor = textColor;
  }

  // note that you need the unlocalized text here
  public void addSlotList(
      // String text, HorizontalScrollableListWidget scrollableListWidget, int[] validValues) {

      String entryName, RocketComponentEntryWidget entry) {
    int scrollbarPadding =
        entry.itemList.sliderActive ? HorizontalScrollableListWidget.scrollPaneWidth : 0;

    LabelWidget textWidget =
        new LabelWidget(
            0,
            0,
            I18n.format(entryName),
            () -> {
              return 0x404040;
            });
    //
    // textWidget.setSelfPosition(new Position(0, rowSkip + rowSeparation + scrollbarPadding));

    // this.addWidget(textWidget);

    //   scrollableListWidget.setSelfPosition(
    //       new Position(
    //           this.getSize().width
    //               - scrollableListWidget.getSize().width
    //               - 20, // 20 is some space for the checkbox
    //           0));

    // WidgetIntSelector selector =
    //     new WidgetIntSelector(
    //         validValues, new Position(this.getSize().width - 80, 0), new Size(60, 18));
    // selector.setActive(false); // turn it off by default, only show when the button is clicked
    // selector.setVisible(false);
    // listEntry entry =
    //     new listEntry(
    //         new Position(0, rowSkip + rowSeparation + scrollbarPadding),
    //         new Size(this.getSize().width, rowSeparation + scrollbarPadding),
    //         null,
    //         scrollableListWidget,
    //         selector);
    //
    // ImageCycleButtonWidget checkmark =
    //     new ImageCycleButtonWidget(
    //         this.getSize().width - 20,
    //         0,
    //         18,
    //         18,
    //         GuiTextures.BUTTON_MACHINE,
    //         () -> {
    //           return entry.shortView;
    //         },
    //         (bool) -> {
    //           entry.setShortView(bool);
    //         });
    //
    // entry.checkmark = checkmark;
    // entry.setWidgetIndex(checkmark);
    // entry.selector = selector;
    // entry.setWidgetIndex(selector);
    // entry.resetBackgroundWidget();
    // this.linkedWidgets.put(text, entry);
    // this.addWidget(entry);
    // this is rather bad, but im not sure how to do it better :c

    rowSkip += rowSeparation + scrollbarPadding;
  }

  public void RemoveSlotLists() {
    this.clearAllWidgets();
    //   this.linkedWidgets.clear();
    rowSkip = 0;
  }
}
