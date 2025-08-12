package supersymmetry.common.mui.widget;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
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
  public ImageWidget buttonBackground;
  private Size previousStateSize = new Size(18 * 5 + 2, 28);

  private boolean previousStateSlider = false;

  public RocketComponentEntryWidget(
      Position pos, Size size, HorizontalScrollableListWidget itemList, int[] validValues) {
    super(pos, size);
    ImageCycleButtonWidget button =
        new ImageCycleButtonWidget(
            itemList.getPosition().x + itemList.getSize().width + 10,
            0,
            18,
            18,
            SusyGuiTextures.SPACEFLIGHT_SIMULATOR_BUTTON_LEFT,
            () -> {
              return !this.shortView;
            },
            (bool) -> {
              this.setShortView(!bool);
              this.resetBackgroundWidget();
            });
    WidgetIntSelector selector =
        new WidgetIntSelector(
            validValues,
            new Position(itemList.getPosition().x + 20, 0),
            new Size((int) (itemList.getSize().width / 2 - 10), 18));
    this.checkmark = button;
    this.selector = selector;
    this.itemList = itemList;
    this.selector.setVisible(false);
    this.selector.setActive(false);

    this.resetBackgroundWidget();
    this.addWidget(buttonBackground);
    this.addWidget(button);
    this.addWidget(selector);
    this.addWidget(itemList);
  }

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
    itemList.setSelfPosition(
        Position.ORIGIN); // the item list should be at the very beginning of the entry
    this.addWidget(itemList);
    if (button != null) {
      button.setSelfPosition(
          new Position(itemList.getPosition().x + itemList.getSize().width + 15, 0));
      this.addWidget(button);

      this.resetBackgroundWidget();
    }
    if (selector != null) {
      selector.setActive(false);
      selector.setVisible(false);
      // off by default since shortView is false by default
      selector.setSelfPosition(
          new Position(
              itemList.getPosition().x + itemList.getSize().width - selector.getSize().width - 5,
              /*should be hidden behind the item list by default*/
              0));

      this.addWidget(selector);
    }

    this.resetBackgroundWidget();
  }

  public boolean isShortView() {
    return shortView;
  }

  public void resetBackgroundWidget() {
    buttonBackground =
        new ImageWidget(
            checkmark.getSelfPosition().x - 1,
            checkmark.getSelfPosition().y - 1,
            checkmark.getSize().width + 1,
            checkmark.getSize().height + 1,
            SusyGuiTextures.SPACEFLIGHT_SIMULATOR_SLIDER_BACKGROUND);
  }

  // true == shortened version is to be displayed, with only 1 slot visible
  public void setShortView(boolean state) {
    if (this.shortView == state) return;
    this.selector.setActive(state);
    this.selector.setVisible(state);

    if (state) {
      this.previousStateSize = this.itemList.getSize();
      this.previousStateSlider = this.itemList.sliderActive;
      this.itemList.setSize(new Size(18, 18));
      this.itemList.setSliderActive(false);
      this.itemList.setSliderOffset(0f);
    } else {
      this.itemList.setSize(this.previousStateSize);
      this.itemList.setSliderActive(this.previousStateSlider);
    }
    shortView = state;
  }
}
