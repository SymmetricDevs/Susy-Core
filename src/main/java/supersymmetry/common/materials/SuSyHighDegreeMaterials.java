package supersymmetry.common.materials;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.GENERATE_FOIL;
import static supersymmetry.common.materials.SusyMaterials.MetallizedBoPET;

import gregtech.api.unification.material.Material;
import supersymmetry.api.util.SuSyUtility;

public class SuSyHighDegreeMaterials {

    public static void init() {
        // The gold content is much less than this
        // Placed at the end of ThirdDegreeMaterials.groovy
        MetallizedBoPET = new Material.Builder(24999, SuSyUtility.susyId("metallized_bopet"))
                .polymer()
                .flags(GENERATE_FOIL)
                .components(Carbon, 10, Hydrogen, 6, Oxygen, 4, Gold, 1)
                .color(0x7e9e8e)
                .build();
    }
}
