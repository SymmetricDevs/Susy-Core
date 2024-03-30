package supersymmetry.integration.immersiverailroading.gui;

import cam72cam.immersiverailroading.gui.container.BaseContainer;
import cam72cam.mod.gui.container.IContainerBuilder;
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

        int rows = this.getBatteryTrackRows();
        int batteriesPerRow = this.stock.getBatterySlots() / this.getBatteryTrackRows();
        int tracksPerRow = this.getSlotsX() - 1 - this.getBatteryTrackRows();

        ItemStack battery = new ItemStack(MetaItems.BATTERY_HULL_LV.getStackForm());
        ItemStack trackSegment = new ItemStack(SuSyMetaItems.TRACK_SEGMENT.getStackForm());



        for (int i = 0; i < rows; i++) {

            for (int j = 0; j < batteriesPerRow; j++) {
                container.drawSlotOverlay(battery, 18 * j, currY);
            }

            for (int j = 0; j < tracksPerRow; j++) {
                container.drawSlotOverlay(trackSegment, batteriesPerRow * 18 + 18 + 18 * j, currY);
            }

            container.drawSlotRow(this.stock.cargoItems, batteriesPerRow * i, batteriesPerRow, 0, currY);
            currY = container.drawSlotRow(this.stock.cargoItems, this.stock.getBatterySlots() + tracksPerRow * i, tracksPerRow, batteriesPerRow * 18 + 18, currY);
        }

        container.drawBottomBar(0, currY, batteriesPerRow);
        currY = container.drawBottomBar(batteriesPerRow * 18 + 18, currY, tracksPerRow);
        currY = container.drawTopBar(0, currY, this.getSlotsX());


        currY = container.drawSlotBlock(this.stock.cargoItems, this.stock.getBatterySlots() + this.stock.getTrackSlots(), this.stock.getInventoryWidth(), 0, currY);
        currY = container.drawPlayerInventoryConnector(0, currY, this.stock.getInventoryWidth());
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
