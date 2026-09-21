package supersymmetry.common.materials;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.DISABLE_DECOMPOSITION;
import static gregtech.api.unification.material.info.MaterialFlags.GENERATE_FOIL;
import static gregtech.api.unification.material.info.MaterialIconSet.ROUGH;
import static supersymmetry.common.materials.SusyMaterials.*;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;
import supersymmetry.api.util.SuSyUtility;

public class SuSyHighDegreeMaterials {

    public static void init() {
        // The gold content is much less than this
        // Placed at the end of ThirdDegreeMaterials.groovy
        MetallizedBoPET = new Material.Builder(24999, SuSyUtility.susyId("metallized_bopet")).polymer()
                .flags(GENERATE_FOIL).components(Carbon, 10, Hydrogen, 6, Oxygen, 4, Gold, 1).color(0x7e9e8e).build();

        KreepAnorthosite = new Material.Builder(24998, SuSyUtility.susyId("kreep_anorthosite")).dust().colorAverage()
                .iconSet(ROUGH).flags(MaterialFlags.NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(Anorthosite, 8, Potassium, 1, RareEarth, 1, Phosphorus, 1).build();
    }
}
