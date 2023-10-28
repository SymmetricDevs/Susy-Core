package supersymmetry.api.fluids;

import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.fluids.fluidType.FluidTypeLiquid;

public class SusyFluidTypes {
    public static final FluidType IMPURE_SLURRY = new FluidTypeLiquid("impure_slurry", "impure_", "_slurry", "fluid.slurry.impure");
    public static final FluidType PURE_SLURRY = new FluidTypeLiquid("pure_slurry", null, "_slurry", "fluid.slurry.pure");
}
