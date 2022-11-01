package susycore;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.BlastProperty;

import static gregtech.api.unification.material.Materials.*;

public class SusyMaterials {
    public static Material ManganeseIronArsenicPhosphide;
    public static Material PraseodymiumNickel;
    public static Material GadoliniumSiliconGermanium;

    public static void init() {
        ManganeseIronArsenicPhosphide = new Material.Builder(27000, "manganese_iron_arsenic_phosphide")
                .ingot()
                .color(0xFFFFFF).iconSet(MaterialIconSet.METALLIC)
                .cableProperties(GTValues.V[4], 2, 4)
                .components(Manganese, 2, Iron, 2, Arsenic, 1, Phosphorus, 1)
                .blastTemp(2100, BlastProperty.GasTier.LOW, GTValues.VA[GTValues.MV], 120)
                .build();
        PraseodymiumNickel = new Material.Builder(27001, "praseodymium_nickel")
                .ingot()
                .color(0xFFFFFF).iconSet(MaterialIconSet.METALLIC)
                .cableProperties(GTValues.V[4], 2, 4)
                .components(Praseodymium, 5, Nickel, 1)
                .blastTemp(2100, BlastProperty.GasTier.LOW, GTValues.VA[GTValues.MV], 120)
                .build();
        GadoliniumSiliconGermanium = new Material.Builder(27002, "gadolinium_silicon_germanium")
                .ingot()
                .color(0xFFFFFF).iconSet(MaterialIconSet.METALLIC)
                .cableProperties(GTValues.V[4], 2, 4)
                .components(Gadolinium, 5, Silicon, 2, Germanium, 2)
                .blastTemp(2100, BlastProperty.GasTier.LOW, GTValues.VA[GTValues.MV], 120)
                .build();

        SusyMaterials.init();
    }
}
