package supersymmetry.common.materials;

import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;

import static supersymmetry.common.materials.SusyMaterials.*;

public class SuSyUnknownCompositionMaterials {

    public static void init() {

        Latex = new Material.Builder(27050, GTUtility.gregtechId("latex"))
                .dust().fluid(FluidTypes.LIQUID)
                .color(0xFFFADA)
                .build();

        Mud = new Material.Builder(27051, GTUtility.gregtechId("mud"))
                .fluid(FluidTypes.LIQUID)
                .color(0x211b14)
                .build();

        Seawater = new Material.Builder(27052, GTUtility.gregtechId("sea_water"))
                .fluid()
                .color(0x3c5bc2)
                .build();
    }

}
