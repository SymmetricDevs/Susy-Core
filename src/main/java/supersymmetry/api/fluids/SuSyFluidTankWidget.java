package supersymmetry.api.fluids;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import gregtech.api.capability.impl.NotifiableFilteredFluidHandler;
import gregtech.api.metatileentity.MetaTileEntity;

public class SuSyFluidTankWidget extends NotifiableFilteredFluidHandler {

    public SuSyFluidTankWidget(int capacity, MetaTileEntity entityToNotify, boolean isExport) {
        super(capacity, entityToNotify, isExport);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || !canFillFluidType(resource)) {
            return 0;
        }
        return super.fill(resource, doFill);
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        boolean result = super.canFillFluidType(fluid);
        System.out.println("[SuSyFilteredFluidTank] canFillFluidType() called with: " +
                (fluid != null ? fluid.getFluid().getName() : "null") + ", result: " + result);
        return result;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        IFluidTankProperties[] properties = super.getTankProperties();
        return new IFluidTankProperties[] {
                new IFluidTankProperties() {

                    @Override
                    public FluidStack getContents() {
                        return properties[0].getContents();
                    }

                    @Override
                    public int getCapacity() {
                        return properties[0].getCapacity();
                    }

                    @Override
                    public boolean canFill() {
                        return properties[0].canFill();
                    }

                    @Override
                    public boolean canDrain() {
                        return properties[0].canDrain();
                    }

                    @Override
                    public boolean canFillFluidType(FluidStack fluidStack) {
                        boolean result = fluidStack != null && SuSyFluidTankWidget.this.canFillFluidType(fluidStack);
                        System.out.println("[SuSyFilteredFluidTank] getTankProperties().canFillFluidType() with: " +
                                (fluidStack != null ? fluidStack.getFluid().getName() : "null") + ", result: " +
                                result);
                        return result;
                    }

                    @Override
                    public boolean canDrainFluidType(FluidStack fluidStack) {
                        return properties[0].canDrainFluidType(fluidStack);
                    }
                }
                // gross
        };
    }
}
