package supersymmetry.common.mui.widget;


import gregtech.api.gui.widgets.SlotWidget;
import net.minecraftforge.items.IItemHandlerModifiable;

/** honestly shouldnt exist */
public class SlotWidgetAdvanced extends SlotWidget {
  Runnable onSlotChanged;

  public SlotWidgetAdvanced(
      IItemHandlerModifiable handler, int slotIndex, int xpos, int ypos, Runnable onSlotChanged) {
    super(handler, slotIndex, xpos, ypos);
    this.onSlotChanged = onSlotChanged;
  }

  @Override
  public void onSlotChanged() {
    super.onSlotChanged();
    this.onSlotChanged.run();
  }
}
