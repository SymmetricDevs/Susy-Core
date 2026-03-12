<<<<<<<< HEAD:src/main/java/supersymmetry/api/fluids/SuSyFluidTankHandler.java
package supersymmetry.api.fluids;
========
package supersymmetry.api.capability.impl;
>>>>>>>> 047d41e486c155818666d05d085d048ed13f146a:src/main/java/supersymmetry/api/capability/impl/SuSyFilteredFluidTank.java

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import gregtech.api.capability.impl.NotifiableFilteredFluidHandler;
import gregtech.api.metatileentity.MetaTileEntity;

public class SuSyFluidTankHandler extends NotifiableFilteredFluidHandler {

    public SuSyFluidTankHandler(int capacity, MetaTileEntity entityToNotify, boolean isExport) {
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
                        boolean result = fluidStack != null && SuSyFluidTankHandler.this.canFillFluidType(fluidStack);
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
