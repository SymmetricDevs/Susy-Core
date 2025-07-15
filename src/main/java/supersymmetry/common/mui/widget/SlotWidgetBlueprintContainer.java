package supersymmetry.common.mui.widget;

import gregtech.api.gui.widgets.SlotWidget;
import net.minecraftforge.items.IItemHandlerModifiable;

/** honestly shouldnt exist */
public class SlotWidgetBlueprintContainer extends SlotWidget {
  Runnable onSlotChanged;
  Runnable onDetectChanges;

  public SlotWidgetBlueprintContainer(
      IItemHandlerModifiable handler,
      int slotIndex,
      int xpos,
      int ypos,
      Runnable onSlotChanged,
      Runnable onDetectChanges) {
    super(handler, slotIndex, xpos, ypos);
    this.onSlotChanged = onSlotChanged;
    this.onDetectChanges = onDetectChanges;
  }

  @Override
  public void onSlotChanged() {
    super.onSlotChanged();
    this.onSlotChanged.run();
  }

  @Override
  public void detectAndSendChanges() {
    super.detectAndSendChanges();
    onDetectChanges.run();
  }
}
