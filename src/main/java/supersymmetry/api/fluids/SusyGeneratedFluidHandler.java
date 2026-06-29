package supersymmetry.api.fluids;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import gregicality.multiblocks.api.fluids.GCYMFluidStorageKeys;
import gregtech.api.GregTechAPI;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import supersymmetry.api.unification.material.info.SuSyMaterialFlags;

public class SusyGeneratedFluidHandler {

    public static final List<Material> CAST_MATERIALS = new ObjectArrayList<>();

    public static void init() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            createMoltenFluid(material);
        }
    }

    public static void createMoltenFluid(@NotNull Material material) {
        FluidProperty fluidProperty = material.getProperty(PropertyKey.FLUID);
        if (fluidProperty == null) return;

        if (material.hasFlag(SuSyMaterialFlags.CONTINUOUSLY_CAST)) {
            CAST_MATERIALS.add(material);
            fluidProperty.enqueueRegistration(GCYMFluidStorageKeys.MOLTEN, new FluidBuilder()
                    .temperature(material.getBlastTemperature() + 1000));
        }
    }
}
