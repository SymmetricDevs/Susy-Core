package supersymmetry.api.fluids;

import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.store.FluidStorageKey;
import supersymmetry.api.unification.material.info.SuSyMaterialIconType;

import java.util.function.UnaryOperator;

import static supersymmetry.api.util.SuSyUtility.susyId;

public final class SusyFluidStorageKeys {

    public static final FluidStorageKey SLURRY = new FluidStorageKey(susyId("liquid"),
            SuSyMaterialIconType.slurry,
            UnaryOperator.identity(),
            m -> "susy.fluid.slurry",
            FluidState.LIQUID);

    public static final FluidStorageKey IMPURE_SLURRY = new FluidStorageKey(susyId("impure_slurry"),
            SuSyMaterialIconType.slurry,
            UnaryOperator.identity(),
            m -> "susy.fluid.impure_slurry",
            FluidState.LIQUID);

    private SusyFluidStorageKeys() {}
    
}
