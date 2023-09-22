package supersymmetry.common.entities;

import cam72cam.immersiverailroading.entity.Locomotive;
import cam72cam.immersiverailroading.util.FluidQuantity;
import cam72cam.mod.fluid.Fluid;

import java.util.ArrayList;
import java.util.List;

public class EntityTunnelBore extends Locomotive {

    private int length = 50;
    protected int getAvailableHP() {
        return this.getDefinition().getHorsePower(this.gauge);
    }
    public FluidQuantity getTankCapacity() {
        return FluidQuantity.ZERO;
    }

    public List<Fluid> getFluidFilter() {
        return new ArrayList();
    }
    public int getInventoryWidth() {
        return 2;
    }
}

