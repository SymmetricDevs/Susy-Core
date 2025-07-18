package supersymmetry.common.mui.widget;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import supersymmetry.api.SusyLog;

public class RocketSimulatorComponentContainerWidget extends AbstractWidgetGroup {
  public int rowSkip; // to keep track of the distance between widgets
  public static int rowSeparation = 18; // mc slot size hopefully
  public Map<String, listEntry> linkedWidgets = new HashMap<>();
  private EntityPlayer activePlayer;

  public RocketSimulatorComponentContainerWidget(
      Position position, Size size, EntityPlayer player) {
    super(position, size);
    this.setSize(size);
    this.setSelfPosition(position);
    this.setParentPosition(Position.ORIGIN);
    this.activePlayer = player;
  }

  public void addSlotList(
      String text, HorizontalScrollableListWidget scrollableListWidget, int[] validValues) {
    SusyLog.logger.info("addslot event, will break if it doesnt run on a server");
    int scrollbarPadding =
        scrollableListWidget.sliderActive ? HorizontalScrollableListWidget.scrollPaneWidth : 0;

    LabelWidget textWidget =
        new LabelWidget(
            0,
            0,
            text,
            () -> {
              return 0x404040;
            });

    textWidget.setSelfPosition(new Position(0, rowSkip + rowSeparation + scrollbarPadding));

    this.addWidget(textWidget);

    scrollableListWidget.setSelfPosition(
        new Position(
            this.getSize().width
                - scrollableListWidget.getSize().width
                - 20, // 20 is some space for the checkbox
            0));

    intSelector selector =
        new intSelector(validValues, new Position(this.getSize().width - 50, 0), new Size(60, 18));
    selector.setActive(false); // turn it off by default, only show when the button is clicked
    selector.setVisible(false);
    listEntry entry =
        new listEntry(
            new Position(0, rowSkip + rowSeparation + scrollbarPadding),
            new Size(this.getSize().width, rowSeparation + scrollbarPadding),
            null,
            scrollableListWidget,
            selector);

    ImageCycleButtonWidget checkmark =
        new ImageCycleButtonWidget(
            this.getSize().width - 20,
            0,
            18,
            18,
            GuiTextures.BUTTON_MACHINE,
            () -> {
              return entry.shortView;
            },
            (bool) -> {
              SusyLog.logger.info("setShortView call");
              entry.setShortView(bool);
            });

    entry.checkmark = checkmark;
    entry.setWidgetIndex(checkmark);
    entry.selector = selector;
    entry.setWidgetIndex(selector);
    entry.resetBackgroundWidget();
    this.addWidget(entry);
    // this is rather bad, but im not sure how to do it better :c

    rowSkip += rowSeparation + scrollbarPadding;
  }

  public void RemoveSlotLists() {
    this.clearAllWidgets();
    this.linkedWidgets.clear();
    rowSkip = 0;
  }

  private class listEntry extends AbstractWidgetGroup {
    public intSelector selector;
    public boolean shortView = false;
    public HorizontalScrollableListWidget itemList;
    public ImageCycleButtonWidget checkmark;
    public ImageWidget checkboxBackground;

    Size previousStateSize = new Size(18 * 5 + 2, 28);
    boolean previousStateSlider = false;

    public listEntry(
        Position pos,
        Size size,
        ImageCycleButtonWidget button,
        HorizontalScrollableListWidget itemList,
        intSelector selector) {
      super(pos, size);
      SusyLog.logger.info(
          "pos {} size {} checkmark {} button {} list {}", pos, size, checkmark, button, itemList);
      this.checkmark = button;
      this.itemList = itemList;
      this.selector = selector;
      // the lsp format messes up how it looks, sorrie
      if (itemList != null) {
        this.addWidget(itemList);
      }
      if (button != null) {
        this.addWidget(button);
        this.resetBackgroundWidget();
      }
      if (selector != null) {
        this.addWidget(selector);
      }

      if (checkboxBackground != null) {
        this.addWidget(checkboxBackground);
      }
      SusyLog.logger.info(
          "weird shit, itemlist count: {}, dimensions of the itemlist pos {} size {}, dimensions of"
              + " the thing: pos {} size {}",
          itemList.widgets.size(),
          itemList.getPosition(),
          itemList.getSize(),
          this.getPosition(),
          this.getSize());
    }

    public void setWidgetIndex(Widget widget) {
      if (!this.widgets.contains(widget)) {
        this.addWidget(widget);
      }
    }

    public void resetBackgroundWidget() {
      checkboxBackground =
          new ImageWidget(
              checkmark.getSelfPosition().x - 1,
              checkmark.getSelfPosition().y - 1,
              checkmark.getSize().width + 2,
              checkmark.getSize().height + 2,
              GuiTextures.BUTTON_POWER_DETAIL);
      setWidgetIndex(checkboxBackground);
    }

    // true == shortened version is to be displayed, with only 1 slot visible
    public void setShortView(boolean state) {
      if (shortView == state) return;
      SusyLog.logger.info("shortview set to {}", state);
      this.selector.setActive(state);
      this.selector.setVisible(state);

      if (state) {
        this.previousStateSize = this.itemList.getSize();
        this.previousStateSlider = this.itemList.sliderActive;
        this.itemList.setSize(new Size(18, 18));
        this.itemList.setSliderActive(false);
        this.itemList.setSliderOffset(0f);
        SusyLog.logger.info("turned off the itemlist behaviours");
      } else {
        this.itemList.setSize(this.previousStateSize);
        this.itemList.setSliderActive(this.previousStateSlider);
        SusyLog.logger.info(
            "enabled back the item list. size:{} slider:{}",
            this.previousStateSize,
            this.previousStateSlider);
      }
      shortView = state;
    }
  }

  private class intSelector extends AbstractWidgetGroup {
    protected ClickButtonWidget decreaseButton;
    protected ClickButtonWidget increaseButton;
    protected TextFieldWidget2 amountTextField;
    protected int[] validValues;
    int selectedIndex = 0;

    public intSelector(int[] validValues, Position position, Size size) {
      super(position, size);
      this.validValues = validValues;
      decreaseButton =
          new ClickButtonWidget(
                  0,
                  0,
                  size.width / 5,
                  size.height,
                  "<",
                  (clickdata) -> {
                    this.selectedIndex--;
                  })
              .setShouldClientCallback(true)
              .setButtonTexture(GuiTextures.BUTTON_LEFT);
      increaseButton =
          new ClickButtonWidget(
                  (size.width / 5) * 4,
                  0,
                  size.width / 5,
                  size.height,
                  ">",
                  (clickdata) -> {
                    this.selectedIndex++;
                  })
              .setShouldClientCallback(true)
              .setButtonTexture(GuiTextures.BUTTON_RIGHT);
      amountTextField =
          new TextFieldWidget2(
              size.width / 5,
              0,
              (size.width / 5) * 3,
              size.height,
              () -> {
                SusyLog.logger.info("text field value requested");
                return Integer.toString(this.getValue());
              },
              (something) -> {});
    }

    public int getValue() {
      return validValues[
          ((selectedIndex % validValues.length) + validValues.length) % validValues.length];
    }
  }
}
