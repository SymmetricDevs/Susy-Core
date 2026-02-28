package supersymmetry.api.fluids;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import gregtech.api.gui.widgets.TankWidget;

public class FilteredTankWidget extends TankWidget {

    public FilteredTankWidget(IFluidTank fluidTank, int x, int y, int width, int height) {
        super(fluidTank, x, y, width, height);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && fluidTank instanceof IFluidHandler) {
            ItemStack held = gui.entityPlayer.inventory.getItemStack();
            if (!held.isEmpty()) {
                IFluidHandlerItem itemHandler = held.getCapability(
                        CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (itemHandler != null) {
                    FluidStack heldFluid = itemHandler.drain(Integer.MAX_VALUE, false);
                    if (heldFluid != null && heldFluid.amount > 0) {
                        // Simulate: would the tank accept this fluid?
                        if (((IFluidHandler) fluidTank).fill(heldFluid, false) <= 0) {
                            return false; // block the click entirely — no packet sent
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
