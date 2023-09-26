package supersymmetry.common.entities;

import cam72cam.immersiverailroading.entity.Locomotive;
import cam72cam.immersiverailroading.util.FluidQuantity;
import cam72cam.immersiverailroading.util.Speed;
import cam72cam.mod.fluid.Fluid;

import java.util.ArrayList;
import java.util.List;

public class EntityTunnelBore extends Locomotive {

    private int length = 50;
    public FluidQuantity getTankCapacity() {
        return FluidQuantity.ZERO;
    }

    public List<Fluid> getFluidFilter() {
        return new ArrayList();
    }
    public int getInventoryWidth() {
        return 2;
    }
    public boolean providesElectricalPower() {
        return false;
    }

    // I just stole this from hand cars, I have no idea what it does - MTBO
    // TODO: Electrical locomotives?
    public double getAppliedTractiveEffort(Speed speed) {
        double maxPower_W = (double)this.getDefinition().getHorsePower(this.gauge) * 745.7;
        double efficiency = 0.82;
        double speed_M_S = Math.abs(speed.metric()) / 3.6;
        double maxPowerAtSpeed = maxPower_W * efficiency / speed_M_S;
        return maxPowerAtSpeed * (double)this.getThrottle() * (double)this.getReverser();
    }
}

