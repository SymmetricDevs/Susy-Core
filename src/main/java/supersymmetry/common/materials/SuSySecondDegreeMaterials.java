package supersymmetry.common.materials;

import gregtech.api.unification.material.Material;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.DECOMPOSITION_BY_CENTRIFUGING;
import static gregtech.api.unification.material.info.MaterialFlags.NO_SMASHING;
import static gregtech.api.unification.material.info.MaterialIconSet.ROUGH;

import static supersymmetry.common.materials.SusyMaterials.*;

public class SuSySecondDegreeMaterials {

    public static void init() {
        Gabbro = new Material.Builder(27200, "gabbro")
                .dust()
                .color(0x5C5C5C).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Olivine, 6, Magnesia, 2, Calcium, 1, Oxygen, 1)
                .build();
    }
}
