package supersymmetry.api.fluids;

import static supersymmetry.api.util.SuSyUtility.susyId;

import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.store.FluidStorageKey;
import supersymmetry.api.unification.material.info.SuSyMaterialIconType;

public final class SusyFluidStorageKeys {

    public static final FluidStorageKey SLURRY = new FluidStorageKey(susyId("slurry"),
            SuSyMaterialIconType.slurry,
            s -> s + "_slurry",
            m -> "susy.fluid.slurry",
            FluidState.LIQUID, -1);

    public static final FluidStorageKey IMPURE_SLURRY = new FluidStorageKey(susyId("impure_slurry"),
            SuSyMaterialIconType.slurry,
            s -> "impure_" + s + "_slurry",
            m -> "susy.fluid.impure_slurry",
            FluidState.LIQUID, -1);

    public static final FluidStorageKey SUPERCRITICAL = new FluidStorageKey(susyId("supercritical"),
            SuSyMaterialIconType.supercritical,
            s -> "supercritical_" + s,
            m -> "susy.fluid.supercritical",
            FluidState.GAS, -1);

    private SusyFluidStorageKeys() {}
}
