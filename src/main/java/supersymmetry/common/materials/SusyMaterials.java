package supersymmetry.common.materials;

import gregtech.api.unification.material.Material;

public class SusyMaterials {

    public static Material ManganeseIronArsenicPhosphide;
    public static Material PraseodymiumNickel;
    public static Material GadoliniumSiliconGermanium;
    public static Material Gabbro;
    public static Material Gneiss;
    public static Material Limestone;
    public static Material Phyllite;
    public static Material Shale;
    public static Material Slate;
    public static Material Latex;

    //Minerals

    public static Material Anorthite;
    public static Material Albite;
    public static Material Oligoclase;
    public static Material Andesine;
    public static Material Labradorite;
    public static Material Bytownite;
    public static Material Clinochlore;
    public static Material Augite;
    public static Material Dolomite;
    public static Material Muscovite;
    public static Material Fluorite;




    public static void init() {
        SuSyElementMaterials.init();
        SuSyFirstDegreeMaterials.init();
        SuSySecondDegreeMaterials.init();
        SuSyOrganicChemistryMaterials.init();
        SuSyHighDegreeMaterials.init();
        SuSyUnknownCompositionMaterials.init();
    }
}
