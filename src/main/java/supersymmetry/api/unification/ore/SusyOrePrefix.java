package supersymmetry.api.unification.ore;

import gregtech.api.GTValues;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.ore.OrePrefix;
import supersymmetry.api.unification.material.info.SuSyMaterialFlags;
import supersymmetry.api.unification.material.info.SuSyMaterialIconType;

public class SusyOrePrefix {
    public static OrePrefix oreGabbro = new OrePrefix("oreGabbro", -1L,  null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix oreGneiss = new OrePrefix("oreGneiss", -1L,  null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix oreLimestone = new OrePrefix("oreLimestone", -1L, null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix orePhyllite = new OrePrefix("orePhyllite", -1L, null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix oreQuartzite = new OrePrefix("oreQuartzite", -1L, null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix oreShale = new OrePrefix("oreShale", -1L, null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix oreSlate = new OrePrefix("oreSlate", -1L, null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix oreSoapstone = new OrePrefix("oreSoapstone", -1L, null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix oreKimberlite = new OrePrefix("oreKimberltie", -1L, null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix catalystBed = new OrePrefix("catalystBed", GTValues.M * 4, null, SuSyMaterialIconType.catalystBed, 1L, mat -> mat.hasFlag(SuSyMaterialFlags.GENERATE_CATALYST_BED));
    public static OrePrefix catalystPellet = new OrePrefix("catalystPellet", GTValues.M / 4, null, SuSyMaterialIconType.catalystPellet, 1L, mat -> mat.hasFlag(SuSyMaterialFlags.GENERATE_CATALYST_PELLET));


    // Tiered Catalysts Beds

    public static OrePrefix catalystBedReduction = new OrePrefix("catalystBedReduction", GTValues.M, null, SuSyMaterialIconType.catalystBed, 1L, mat -> false);
    public static OrePrefix catalystBedOxidation = new OrePrefix("catalystBedOxidation", GTValues.M, null, SuSyMaterialIconType.catalystBed, 1L, mat -> false);
    public static OrePrefix catalystBedCracking = new OrePrefix("catalystBedCracking", GTValues.M, null, SuSyMaterialIconType.catalystBed, 1L, mat -> false);
    public static OrePrefix catalystBedZieglerNatta = new OrePrefix("catalystBedZieglerNatta", GTValues.M, null, SuSyMaterialIconType.catalystBed, 1L, mat -> false);


    // Tiered Catalyst Pellets

    public static OrePrefix catalystPelletReduction = new OrePrefix("catalystPelletReduction", GTValues.M * 4, null, SuSyMaterialIconType.catalystPellet, 1L, mat -> false);
    public static OrePrefix catalystPelletOxidation = new OrePrefix("catalystPelletOxidation", GTValues.M * 4, null, SuSyMaterialIconType.catalystPellet, 1L, mat -> false);
    public static OrePrefix catalystPelletCracking = new OrePrefix("catalystPelletCracking", GTValues.M * 4, null, SuSyMaterialIconType.catalystPellet, 1L, mat -> false);
    public static OrePrefix catalystPelletZieglerNatta = new OrePrefix("catalystPelletZieglerNatta", GTValues.M * 4, null, SuSyMaterialIconType.catalystPellet, 1L, mat -> false);

}
