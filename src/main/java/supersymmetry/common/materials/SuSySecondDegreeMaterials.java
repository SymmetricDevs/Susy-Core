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
                .components(Labradorite, 5, Bytownite, 3, Olivine, 2, Augite, 1, Biotite, 1)
                .build();

        Gneiss = new Material.Builder(27201, "gneiss")
                .dust()
                .color(0x643631).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Albite, 4, SiliconDioxide, 3, Biotite, 1, Muscovite, 1)
                .build();

        Limestone = new Material.Builder(27202, "limestone")
                .dust()
                .color(0xa9a9a9).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Calcite, 4, Dolomite, 1)
                .build();

        Phyllite = new Material.Builder(27203, "phyllite")
                .dust()
                .color(0x716f71).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Albite, 3, SiliconDioxide, 3, Muscovite, 4)
                .build();

        Shale = new Material.Builder(27204, "shale")
                .dust()
                .color(0x3f2e2f).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Calcite, 6, Clay, 2, SiliconDioxide, 1, Fluorite, 1)
                .build();

        Slate = new Material.Builder(27205, "slate")
                .dust()
                .color(0x756869).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 5, Muscovite, 2, Clinochlore, 2, Albite, 1)
                .build();

    }
}
