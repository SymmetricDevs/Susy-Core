package supersymmetry.common.mui.widget;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import supersymmetry.api.SusyLog;
import supersymmetry.api.gui.SusyGuiTextures;

public class RocketComponentEntryWidget extends AbstractWidgetGroup {
  private class WidgetIntSelector extends AbstractWidgetGroup {
    protected ClickButtonWidget decreaseButton;
    protected ClickButtonWidget increaseButton;
    protected TextFieldWidget2 amountTextField;
    protected int[] validValues;
    int selectedIndex = 0;

    public WidgetIntSelector(int[] validValues, Position position, Size size) {
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
              .setButtonTexture(SusyGuiTextures.SPACEFLIGHT_SIMULATOR_BUTTON_LEFT);
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
              .setButtonTexture(SusyGuiTextures.SPACEFLIGHT_SIMULATOR_BUTTON_RIGHT);
      amountTextField =
          new TextFieldWidget2(
              (int) ((size.width / 5) * 2.5),
              0,
              (size.width / 5) * 3,
              size.height,
              () -> {
                return Integer.toString(this.getValue());
              },
              (something) -> {});

      this.addWidget(amountTextField);

      this.addWidget(increaseButton);
      increaseButton.setVisible(validValues.length > 1);
      increaseButton.setActive(validValues.length > 1);
      this.addWidget(decreaseButton);
      decreaseButton.setVisible(validValues.length > 1);
      decreaseButton.setActive(validValues.length > 1);
    }

    public int getValue() {
      return validValues[
          ((selectedIndex % validValues.length) + validValues.length) % validValues.length];
    }
  }

  public WidgetIntSelector selector;
  public boolean shortView = false;
  public HorizontalScrollableListWidget itemList;
  public ImageCycleButtonWidget checkmark;
  public ImageWidget checkboxBackground;

  Size previousStateSize = new Size(18 * 5 + 2, 28);
  boolean previousStateSlider = false;

  public RocketComponentEntryWidget(
      Position pos,
      Size size,
      ImageCycleButtonWidget button,
      HorizontalScrollableListWidget itemList,
      WidgetIntSelector selector) {
    super(pos, size);
    this.checkmark = button;
    this.itemList = itemList;
    this.selector = selector;
    if (itemList != null) this.addWidget(itemList);
    if (button != null) this.addWidget(button);
    this.resetBackgroundWidget();
    if (selector != null) this.addWidget(selector);
    if (checkboxBackground != null) this.addWidget(checkboxBackground);
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
    if (this.shortView == state) return;
    SusyLog.logger.info("shortview set to {}", state);
    this.selector.setActive(state);
    this.selector.setVisible(state);

    if (state) {
      this.previousStateSize = this.itemList.getSize();
      this.previousStateSlider = this.itemList.sliderActive;
      this.itemList.setSize(new Size(18, 18));
      this.itemList.setSliderActive(false);
      this.itemList.setSliderOffset(0f);
      // SusyLog.logger.info("turned off the itemlist behaviours");
    } else {
      this.itemList.setSize(this.previousStateSize);
      this.itemList.setSliderActive(this.previousStateSlider);
      // SusyLog.logger.info(
      //     "enabled back the item list. size:{} slider:{}",
      //     this.previousStateSize,
      //     this.previousStateSlider);
    }
    shortView = state;
  }
}
