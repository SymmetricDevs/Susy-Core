package supersymmetry.api.rocketry;

import net.minecraft.inventory.IInventory;

import supersymmetry.api.items.CargoItemStackHandler;

public interface ICargoInventory extends IInventory {

    void setInventory(CargoItemStackHandler cargoItemStackHandler);
}
