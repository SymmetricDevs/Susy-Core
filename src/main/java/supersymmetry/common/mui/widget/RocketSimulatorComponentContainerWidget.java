package supersymmetry.common.mui.widget;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.Map;

public class RocketSimulatorComponentContainerWidget extends AbstractWidgetGroup {
  public int rowSkip; // to keep track of the distance between widgets
  public static int rowSeparation = 18; // mc slot size hopefully
  public Map<String, listEntry> linkedWidgets = new HashMap<>();
    private EntityPlayer activePlayer;


  public RocketSimulatorComponentContainerWidget(Position position, Size size,EntityPlayer player) {
    super(position, size);
    this.setSize(size);
    this.setSelfPosition(position);
    this.setParentPosition(Position.ORIGIN);
    this.activePlayer=player;
  }

  public void addSlotList(
      String text, HorizontalScrollableListWidget scrollableListWidget, int[] validValues) {
    int scrollbarPadding =
        scrollableListWidget.sliderActive ? HorizontalScrollableListWidget.scrollPaneWidth : 0;

    LabelWidget textWidget =
        new LabelWidget(
            0,
            rowSkip + rowSeparation + scrollbarPadding,
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
            rowSkip + rowSeparation + scrollbarPadding));

    listEntry entry = new listEntry(null, scrollableListWidget, null);
    intSelector selector =
        new intSelector(
            validValues,
            new Position(this.getSize().width - 10, rowSkip + rowSeparation + scrollbarPadding),
            new Size(60, 18));
    selector.setActive(false); // turn it off by default, only show when the button is clicked
    selector.setVisible(false);

    ImageCycleButtonWidget checkmark =
        new ImageCycleButtonWidget(
            this.getSize().width - 20,
            rowSkip + rowSeparation + scrollbarPadding,
            18,
            18,
            GuiTextures.BUTTON_MACHINE,
            () -> {
              return entry.shortView;
            },
            (bool) -> {
              entry.shortView = bool;
            });

    entry.checkmark = checkmark;
        entry.selector = selector;
    // this is rather bad, but im not sure how to do it better :c

    this.addWidget(scrollableListWidget);

    rowSkip += rowSeparation + scrollbarPadding;
  }

  public void RemoveSlotLists() {
    this.clearAllWidgets();
    this.linkedWidgets.clear();
    rowSkip = 0;
  }

  // @Override
  // public void detectAndSendChanges() {
  //   super.detectAndSendChanges();
  // }
  // doesnt actually contain the widgets, just stores the information. why? no reason.
  private class listEntry {
    public intSelector selector;
    public boolean shortView = false;
    public HorizontalScrollableListWidget itemList;
    public ImageCycleButtonWidget checkmark;
    public ImageWidget checkboxBackground =
        new ImageWidget(
            checkmark.getSelfPosition().x - 1,
            checkmark.getSelfPosition().y - 1,
            checkmark.getSize().width + 2,
            checkmark.getSize().height + 2,
            GuiTextures.BUTTON_POWER_DETAIL);
    
        Size previousStateSize = new Size(18*5+2, 28);
        boolean previousStateSlider = false;

    public listEntry(
        ImageCycleButtonWidget button,
        HorizontalScrollableListWidget itemList,
        intSelector selector) {
      this.checkmark = button;
      this.itemList = itemList;
      this.selector = selector;

    }
        public void setShortView(boolean state) {
            if (shortView==state) return;
            this.selector.setActive(state);
            this.selector.setVisible(state);
            if (state) {
            this.previousStateSize = this.itemList.getSize();
                this.previousStateSlider = this.itemList.sliderActive;
              this.itemList.setSize(new Size(18, 18));
                this.itemList.setSliderActive(false);
            } else {
                this.itemList.setSize(this.previousStateSize);
            }

        }


    public void AddAll(AbstractWidgetGroup group) {}
  }

  private class intSelector extends AbstractWidgetGroup {
    protected ClickButtonWidget decreaseButton;
    protected ClickButtonWidget increaseButton;
    protected TextFieldWidget2 amountTextField;
    protected int[] validValues;
    int selectedIndex = 0;

    public intSelector(int[] validValues, Position position, Size size) {
      super(position, size);
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
