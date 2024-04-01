package supersymmetry.integration.immersiverailroading.gui;

import cam72cam.immersiverailroading.gui.container.BaseContainer;
import cam72cam.mod.gui.container.IContainerBuilder;
import cam72cam.mod.gui.container.ServerContainerBuilder;
import cam72cam.mod.item.ItemStack;
import gregtech.common.items.MetaItems;
import supersymmetry.common.entities.EntityTunnelBore;
import supersymmetry.common.item.SuSyMetaItems;

public class TunnelBoreContainer extends BaseContainer {
    public EntityTunnelBore stock;

    public TunnelBoreContainer(EntityTunnelBore stock) {
        this.stock = stock;
    }

    public void draw(IContainerBuilder container) {
        int currY = 0;
        currY = container.drawTopBar(0, currY, this.stock.getInventoryWidth());
        currY = container.drawSlotBlock(this.stock.cargoItems, this.stock.getBatterySlots() + this.stock.getTrackSlots(), this.stock.getInventoryWidth(), 0, currY);
        currY = container.drawBottomBar(0, currY, this.getSlotsX());

        int rows = this.getBatteryTrackRows();
        int batteriesPerRow = this.stock.getBatterySlots() / this.getBatteryTrackRows();
        int tracksPerRow = this.getSlotsX() - 1 - this.getBatteryTrackRows();

        ItemStack battery = new ItemStack(MetaItems.BATTERY_HULL_LV.getStackForm());
        ItemStack trackSegment = new ItemStack(SuSyMetaItems.TRACK_SEGMENT.getStackForm());

        container.drawTopBar(2, currY, batteriesPerRow);
        container.drawTopBar(batteriesPerRow * 18 + 16, currY, tracksPerRow);
        // Very hacky
        // Won't bother explaining, it just tricks IR into doing the right thing
        currY += 7;
        if (container instanceof ServerContainerBuilder serverContainerBuilder) serverContainerBuilder.drawBottomBar(0, 0, 0);

        for (int i = 0; i < rows; i++) {

            for (int j = 0; j < batteriesPerRow; j++) {
                container.drawSlotOverlay(battery, 18 * j + 2, currY);
            }

            for (int j = 0; j < tracksPerRow; j++) {
                container.drawSlotOverlay(trackSegment, batteriesPerRow * 18 + 16 + 18 * j, currY);
            }

            container.drawSlotRow(this.stock.cargoItems, batteriesPerRow * i, batteriesPerRow, 2, currY);
            currY = container.drawSlotRow(this.stock.cargoItems, this.stock.getBatterySlots() + tracksPerRow * i, tracksPerRow, batteriesPerRow * 18 + 16, currY);
        }

        container.drawBottomBar(2, currY, batteriesPerRow);
        currY = container.drawBottomBar(batteriesPerRow * 18 + 16, currY, tracksPerRow);

        container.drawTopBar(0, currY, this.getSlotsX());
        // Still very hacky
        currY += 7;
        if (container instanceof ServerContainerBuilder serverContainerBuilder) serverContainerBuilder.drawBottomBar(0, 0, 0);

        container.drawPlayerInventory(currY, this.stock.getInventoryWidth());
        this.drawName(container, this.stock);
    }

    public int getSlotsX() {
        return this.stock.getInventoryWidth();
    }

    public int getSlotsY() {
        return (this.stock.getInventorySize() - this.stock.getTrackSlots() - this.stock.getBatterySlots()) /
                this.stock.getInventoryWidth();
    }

    public int getBatteryTrackRows() {
        return (this.stock.getTrackSlots() + this.stock.getBatterySlots() ) / (this.stock.getInventoryWidth() - 1);
    }
}
