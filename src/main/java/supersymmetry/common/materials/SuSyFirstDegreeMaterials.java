package supersymmetry.common.materials;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.BlastProperty;

import static gregtech.api.unification.material.Materials.*;
import static supersymmetry.common.materials.SusyMaterials.*;

public class SuSyFirstDegreeMaterials {

    public static void init() {

        ManganeseIronArsenicPhosphide = new Material.Builder(27100, "manganese_iron_arsenic_phosphide")
                .ingot()
                .color(0x03FCF0).iconSet(MaterialIconSet.METALLIC)
                .cableProperties(GTValues.V[4], 2, 4)
                .components(Manganese, 2, Iron, 2, Arsenic, 1, Phosphorus, 1)
                .blastTemp(2100, BlastProperty.GasTier.LOW)
                .build();

        PraseodymiumNickel = new Material.Builder(27101, "praseodymium_nickel")
                .ingot()
                .color(0x03BAFC).iconSet(MaterialIconSet.METALLIC)
                .cableProperties(GTValues.V[4], 2, 4)
                .components(Praseodymium, 5, Nickel, 1)
                .blastTemp(2100, BlastProperty.GasTier.MID)
                .build();

        GadoliniumSiliconGermanium = new Material.Builder(27102, "gadolinium_silicon_germanium")
                .ingot()
                .color(0x0388FC).iconSet(MaterialIconSet.SHINY)
                .cableProperties(GTValues.V[4], 2, 4)
                .components(Gadolinium, 5, Silicon, 2, Germanium, 2)
                .blastTemp(2100, BlastProperty.GasTier.HIGH)
                .build();
    }
}
