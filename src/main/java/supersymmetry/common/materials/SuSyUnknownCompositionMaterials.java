package supersymmetry.common.materials;

import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.unification.material.Material;
import supersymmetry.api.util.SusyUtility;

import static supersymmetry.common.materials.SusyMaterials.*;

public class SuSyUnknownCompositionMaterials {

    public static void init() {

        Latex = new Material.Builder(27050, SusyUtility.susyId("latex"))
                .dust().fluid(FluidTypes.LIQUID)
                .color(0xFFFADA)
                .build();

        Mud = new Material.Builder(27051, SusyUtility.susyId("mud"))
                .fluid(FluidTypes.LIQUID)
                .color(0x211b14)
                .build();

        Seawater = new Material.Builder(27052, SusyUtility.susyId("sea_water"))
                .fluid()
                .color(0x3c5bc2)
                .build();
    }

}
