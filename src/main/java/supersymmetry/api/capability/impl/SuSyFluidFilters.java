package supersymmetry.api.capability.impl;

import gregtech.api.capability.IFilter;
import supersymmetry.common.materials.SusyMaterials;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import static gregtech.api.capability.impl.CommonFluidFilters.matchesFluid;

public enum SuSyFluidFilters implements IFilter<FluidStack> {
    
    HOT_GAS {
        @Override
        public boolean test(@NotNull FluidStack fluid) {
            return matchesFluid(fluid, SusyMaterials.PreheatedAir);
        }

        @Override
        public int getPriority() {
            return IFilter.whitelistPriority(1);
        }
    };
}
