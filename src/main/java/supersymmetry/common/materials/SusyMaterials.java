package supersymmetry.common.materials;

import gregtech.api.unification.material.Material;

public class SusyMaterials {

    public static Material ManganeseIronArsenicPhosphide;
    public static Material PraseodymiumNickel;
    public static Material GadoliniumSiliconGermanium;

    public static void init() {
        SuSyElementMaterials.init();
        SuSyFirstDegreeMaterials.init();
        SuSySecondDegreeMaterials.init();
        SuSyOrganicChemistryMaterials.init();
        SuSyHighDegreeMaterials.init();
        SuSyUnknownCompositionMaterials.init();
    }
}
