package supersymmetry.common.materials;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.properties.BlastProperty;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.CERTUS;
import static gregtech.api.unification.material.info.MaterialIconSet.ROUGH;
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

        //Minerals

        Anorthite = new Material.Builder(27103, "anorthite")
                .dust()
                .gem()
                .color(0x595853).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Calcium, 1, Aluminium, 2, Silicon, 2, Oxygen, 8)
                .build();

        Albite = new Material.Builder(27104, "albite")
                .dust()
                .gem()
                .color(0xc4a997).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Sodium, 1, Aluminium, 1, Silicon, 3, Oxygen, 8)
                .build();

        Oligoclase = new Material.Builder(27105, "oligoclase")
                .dust()
                .gem()
                .color(0xd5c4b8).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Albite, 4, Anorthite, 1)
                .build();

        Andesine = new Material.Builder(27106, "andesine")
                .dust()
                .gem()
                .color(0xe18e6f).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Albite, 3, Anorthite, 2)
                .build();

        Labradorite = new Material.Builder(27107, "labradorite")
                .dust()
                .gem()
                .color(0x5c7181).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Albite, 2, Anorthite, 3)
                .build();

        Bytownite = new Material.Builder(27108, "bytownite")
                .dust()
                .gem()
                .color(0xc99c67).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Albite, 1, Anorthite, 4)
                .build();

        Clinochlore = new Material.Builder(27109, "chlinochlore")
                .dust()
                .gem()
                .color(0x303e38).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Magnesium, 5, Aluminium, 2, Silicon, 3, Oxygen, 18, Hydrogen, 8)
                .build();

        Augite = new Material.Builder(27110, "augite")
                .dust()
                .gem()
                .color(0x1b1717).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Calcium, 2, Magnesium, 3, Iron, 3, Silicon, 8, Oxygen, 24)
                .build();

        Dolomite = new Material.Builder(27111, "dolomite")
                .dust()
                .gem()
                .color(0xbbb8b2).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Calcium, 1, Magnesium, 1, Carbon, 2, Oxygen, 6)
                .build();

        Muscovite = new Material.Builder(27112, "muscovite")
                .dust()
                .gem()
                .color(0x8b876a).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Potassium, 1, Aluminium, 3, Silicon, 3, Oxygen, 12, Hydrogen, 10)
                .build();

        Fluorite = new Material.Builder(27113, "fluorite")
                .dust()
                .gem()
                .color(0x276a4c).iconSet(CERTUS)
                .flags(NO_SMASHING, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Calcium, 1, Fluorine, 2)
                .build();

    }
}
