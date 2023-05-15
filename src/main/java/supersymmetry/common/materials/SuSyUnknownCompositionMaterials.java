package supersymmetry.common.materials;

import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.unification.material.Material;

import static supersymmetry.common.materials.SusyMaterials.*;

public class SuSyUnknownCompositionMaterials {

    public static void init() {

        Latex = new Material.Builder(27050, "latex")
                .dust().fluid(FluidTypes.LIQUID)
                .color(0xFFFADA)
                .build();

        Mud = new Material.Builder(27051, "mud")
                .fluid(FluidTypes.LIQUID)
                .color(0x211b14)
                .build();
    }

}
