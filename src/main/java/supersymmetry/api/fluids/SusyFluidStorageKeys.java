package supersymmetry.api.fluids;

import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.fluids.store.FluidStorageKey;

import java.util.function.UnaryOperator;

import supersymmetry.api.util.SuSyUtility;
import supersymmetry.api.unification.material.info.SuSyMaterialIconType;

public final class SusyFluidStorageKeys {

    public static final FluidStorageKey SLURRY = new FluidStrageKey(susyId("slurry"),
        SuSyMaterialIconType.slurry,
        UnaryOperator.identity(),
        m -> "susy.fluid.slurry");

    public static final FluidStorageKey IMPURE_SLURRY = new FluidStrageKey(susyId("impure_slurry"),
        SuSyMaterialIconType.slurry,
        UnaryOperator.identity(),
        m -> "susy.fluid.impure_slurry");

    private SusyFluidStorageKeys() {}
    
}
