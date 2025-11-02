package supersymmetry.api.capability.impl;

import gregtech.api.capability.IFilter;
import gregtech.api.unification.material.Materials;
import supersymmetry.common.materials.SusyMaterials;
import static gregtech.api.capability.impl.CommonFluidFilters.matchesFluid;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import gregtech.api.capability.IFilter;
import supersymmetry.common.materials.SusyMaterials;

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
    },

    LUBRICANT {
        @Override
        public boolean test(@NotNull FluidStack fluid) {
            return
                matchesFluid(fluid, Materials.Lubricant) ||
                matchesFluid(fluid, SusyMaterials.MidgradeLubricant) ||
                matchesFluid(fluid, SusyMaterials.PremiumLubricant) ||
                matchesFluid(fluid, SusyMaterials.SupremeLubricant);
        }

        @Override
        public int getPriority() { return IFilter.whitelistPriority(1); }
    },

    COOLANT {
        @Override
        public boolean test(@NotNull FluidStack fluid) {
            return
                matchesFluid(fluid, Materials.Water) ||
                matchesFluid(fluid, Materials.DistilledWater) ||
                matchesFluid(fluid, SusyMaterials.Coolant) ||
                matchesFluid(fluid, SusyMaterials.AdvancedCoolant);
        }

        @Override
        public int getPriority() { return IFilter.whitelistPriority(1); }
    }
}
