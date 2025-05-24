package supersymmetry.api.fluids;

import gregicality.multiblocks.api.fluids.GCYMFluidStorageKeys;
import gregtech.api.GregTechAPI;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.unification.material.info.SuSyMaterialFlags;
import supersymmetry.api.unification.material.properties.SuSyPropertyKey;

public class SusyGeneratedFluidHandler {
    public static void init() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            createMoltenFluid(material);
        }
    }

    public static void createMoltenFluid(@NotNull Material material) {
        // ignore materials set not to be alloy blast handled
        FluidProperty fluidProperty = material.getProperty(PropertyKey.FLUID);
        if (fluidProperty == null) return;

        if (material.hasFlag(SuSyMaterialFlags.CONTINUOUSLY_CAST)) {
            fluidProperty.enqueueRegistration(GCYMFluidStorageKeys.MOLTEN, new FluidBuilder()
                    .temperature(material.getBlastTemperature() + 1000));
        }
        // if it is not hot enough to produce molten fluid, ABS Producer grabs normal liquid,
        // thus we don't need to do anything.
    }
}
