package supersymmetry.common.materials;

import gregtech.api.unification.material.Material;

import static supersymmetry.common.materials.SusyMaterials.*;

public class SuSyUnknownCompositionMaterials {

    public static void init() {
        Latex = new Material.Builder(27050, "latex")
                .color(0xFFFADA)
                .fluid()
                .build();


    }
}
