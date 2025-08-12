package supersymmetry.common.mui.widget;

import gregtech.api.gui.widgets.SlotWidget;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandlerModifiable;

/** honestly shouldnt exist, im just bad at it :c */
public class SlotWidgetBlueprintContainer extends SlotWidget {
  public Runnable onSlotChanged;
  public Runnable onDetectChanges;

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

  public SlotWidgetBlueprintContainer(
      IItemHandlerModifiable handler, int slotIndex, int xpos, int ypos, Runnable onSlotChanged) {
    super(handler, slotIndex, xpos, ypos);
    this.onSlotChanged = onSlotChanged;
  }

  @Override
  public void onSlotChanged() {
    super.onSlotChanged();
    if (this.onSlotChanged != null) this.onSlotChanged.run();
    this.writeClientAction(1, (buf) -> {});
  }

  @Override
  public void detectAndSendChanges() {
    super.detectAndSendChanges();
    if (this.onDetectChanges != null) onDetectChanges.run();
  }

  @Override
  public void handleClientAction(int id, PacketBuffer buffer) {
    super.handleClientAction(id, buffer);
    if (id == 1) {
      this.onSlotChanged.run();
    }
  }
}
