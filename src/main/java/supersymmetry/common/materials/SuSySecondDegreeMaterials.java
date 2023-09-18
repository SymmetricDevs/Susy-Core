package supersymmetry.common.materials;

import gregtech.api.unification.material.Material;
import supersymmetry.api.util.SuSyUtility;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.ROUGH;

import static supersymmetry.common.materials.SusyMaterials.*;

public class SuSySecondDegreeMaterials {

    public static void init() {
        Gabbro = new Material.Builder(27200, SuSyUtility.susyId("gabbro"))
                .dust()
                .color(0x5C5C5C).iconSet(ROUGH)
                .flags(NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(Labradorite, 5, Bytownite, 3, Olivine, 2, Augite, 1, Biotite, 1)
                .build();

        Gneiss = new Material.Builder(27201, SuSyUtility.susyId("gneiss"))
                .dust()
                .color(0x643631).iconSet(ROUGH)
                .flags(NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(Albite, 4, SiliconDioxide, 3, Biotite, 1, Muscovite, 1)
                .build();

        Limestone = new Material.Builder(27202, SuSyUtility.susyId("limestone"))
                .dust()
                .color(0xa9a9a9).iconSet(ROUGH)
                .flags(NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(Calcite, 4, Dolomite, 1)
                .build();

        Phyllite = new Material.Builder(27203, SuSyUtility.susyId("phyllite"))
                .dust()
                .color(0x716f71).iconSet(ROUGH)
                .flags(NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(Albite, 3, SiliconDioxide, 3, Muscovite, 4)
                .build();

        Shale = new Material.Builder(27204, SuSyUtility.susyId("shale"))
                .dust()
                .color(0x3f2e2f).iconSet(ROUGH)
                .flags(NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(Calcite, 6, Clay, 2, SiliconDioxide, 1, Fluorite, 1)
                .build();

        Slate = new Material.Builder(27205, SuSyUtility.susyId("slate"))
                .dust()
                .color(0x756869).iconSet(ROUGH)
                .flags(NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(SiliconDioxide, 5, Muscovite, 2, Clinochlore, 2, Albite, 1)
                .build();

        Kimberlite = new Material.Builder(27206, SuSyUtility.susyId("kimberlite"))
                .dust()
                .color(0x201313).iconSet(ROUGH)
                .flags(NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(Forsterite, 3, Augite, 3, Andradite, 2, Lizardite, 1)
                .build();

    }
}
