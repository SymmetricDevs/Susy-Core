package supersymmetry.common.mui.widget;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.util.Tuple;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.util.DataStorageLoader;

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
                  "",
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
                  "",
                  (clickdata) -> {
                    this.selectedIndex++;
                  })
              .setShouldClientCallback(true)
              .setButtonTexture(SusyGuiTextures.SPACEFLIGHT_SIMULATOR_BUTTON_RIGHT);
      amountTextField =
          new TextFieldWidget2(
              (int) ((size.width / 5) * 2.5) - 5,
              3,
              (size.width / 5) * 3,
              size.height,
              () -> {
                return Integer.toString(this.getSelectedValue()) + "x";
              },
              this::trySelectValue);

      this.addWidget(amountTextField);

      this.addWidget(increaseButton);
      increaseButton.setVisible(validValues.length > 1);
      increaseButton.setActive(validValues.length > 1);
      this.addWidget(decreaseButton);
      decreaseButton.setVisible(validValues.length > 1);
      decreaseButton.setActive(validValues.length > 1);
    }

    public void trySelectValue(String value) {

      int val = 5;
      String cleaned = value.endsWith("x") ? value.substring(0, value.length() - 1) : value;
      try {
        val = Integer.parseInt(cleaned);
      } catch (NumberFormatException e) {
        return;
      }
      for (int i = 0; i < validValues.length; i++) {
        if (validValues[i] == val) {
          this.selectedIndex = i;
        }
      }
    }

    public int getSelectedValue() {
      return validValues[
          ((selectedIndex % validValues.length) + validValues.length) % validValues.length];
    }
  }

  public WidgetIntSelector selector;
  public boolean shortView = false;
  // public Consumer<Tuple<Boolean, Integer>> shortViewCB;
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
                12,
                12,
                SusyGuiTextures.SPACEFLIGHT_SIMULATOR_BUTTON_SHORTVIEW,
                () -> {
                  return !this.shortView;
                },
                (bool) -> {
                  this.setShortView(!bool);
                  this.resetBackgroundWidget();
                })
            .singleTexture();
    WidgetIntSelector selector =
        new WidgetIntSelector(
            validValues,
            new Position(itemList.getPosition().x + 20, 0),
            new Size((int) (itemList.getSize().width / 2 - 10), 18));
    this.checkmark = button;
    this.selector = selector;
    this.itemList = itemList;
    // this.shortViewCB = callback;
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
      WidgetIntSelector selector,
      Consumer<Tuple<Boolean, Integer>> callback) {
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

  public List<DataStorageLoader> getSlots() {
    // with a prayer
    return this.itemList.widgets.stream()
        .map(x -> (SlotWidget) x)
        .map(x -> (SlotWidget.WidgetSlotItemHandler) x.getHandle())
        .map(x -> (DataStorageLoader) x.getItemHandler())
        .collect(Collectors.toList());
  }

  @Override
  public void setActive(boolean active) {
    super.setActive(active);
    this.itemList.widgets.forEach(x -> x.setActive(active));
  }

  public int getAmount() {
    return this.selector.getSelectedValue();
  }

  public boolean isShortView() {
    return shortView;
  }

  public void resetBackgroundWidget() {
    buttonBackground =
        new ImageWidget(
            checkmark.getSelfPosition().x - 2,
            checkmark.getSelfPosition().y - 2,
            checkmark.getSize().width + 4,
            checkmark.getSize().height + 4,
            SusyGuiTextures.SPACEFLIGHT_SIMULATOR_SLIDER_BACKGROUND);
  }

  // true == shortened version is to be displayed, with only 1 slot visible
  public void setShortView(boolean state) {
    this.selector.setActive(state);
    this.selector.setVisible(state);
    if (this.shortView == state) return;
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
