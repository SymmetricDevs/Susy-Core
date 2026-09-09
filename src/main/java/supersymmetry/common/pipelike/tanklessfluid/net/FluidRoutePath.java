package supersymmetry.common.pipelike.tanklessfluid.net;

import java.util.function.Predicate;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import gregtech.api.pipenet.IRoutePath;
import gregtech.api.util.FacingPos;
import lombok.Getter;
import lombok.val;
import supersymmetry.api.unification.material.properties.TanklessFluidPipeProperties;
import supersymmetry.common.pipelike.tanklessfluid.tile.TileEntityTanklessFluidPipe;

public record FluidRoutePath(
                             @Getter TileEntityTanklessFluidPipe targetPipe,
                             @Getter EnumFacing targetFacing,
                             @Getter int distance,
                             @Getter TanklessFluidPipeProperties properties,
                             Predicate<FluidStack> filters)
        implements IRoutePath<TileEntityTanklessFluidPipe> {

    public FluidRoutePath(TileEntityTanklessFluidPipe targetPipe, EnumFacing facing, int distance,
                          TanklessFluidPipeProperties properties, Iterable<Predicate<FluidStack>> filters) {
        this(targetPipe, facing, distance, properties, stack -> {
            for (val filter : filters) {
                if (!filter.test(stack)) return false;
            }
            return true;
        });
    }

    public boolean matchesFilters(FluidStack stack) {
        return filters.test(stack);
    }

    public IFluidHandler getHandler() {
        return getTargetCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
    }

    public FacingPos toFacingPos() {
        return new FacingPos(getTargetPipePos(), targetFacing);
    }
}
