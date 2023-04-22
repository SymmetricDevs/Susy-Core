package supersymmetry.api.unification.ore;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.ore.OrePrefix;
import supersymmetry.api.unification.material.info.SuSyMaterialIconType;

public class SusyOrePrefix {
    public static OrePrefix oreGabbro;
    public static OrePrefix oreGneiss;
    public static OrePrefix oreLimestone;
    public static OrePrefix orePhyllite;
    public static OrePrefix oreQuartzite;
    public static OrePrefix oreShale;
    public static OrePrefix oreSlate;
    public static OrePrefix oreSoapstone;
    public static OrePrefix oreKimberlite;
    public static OrePrefix catalystBed;

    public SusyOrePrefix(){
    }

    public static void  init(){
        oreGabbro = new OrePrefix("oreGabbro", -1L,  null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        oreGneiss = new OrePrefix("oreGneiss", -1L,  null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        oreLimestone = new OrePrefix("oreLimestone", -1L, null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        orePhyllite = new OrePrefix("orePhyllite", -1L, null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        oreQuartzite = new OrePrefix("oreQuartzite", -1L, null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        oreShale = new OrePrefix("oreShale", -1L, null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        oreSlate = new OrePrefix("oreSlate", -1L, null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        oreSoapstone = new OrePrefix("oreSoapstone", -1L, null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        oreKimberlite = new OrePrefix("oreKimberltie", -1L, null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        catalystBed = new OrePrefix("catalystBed", GTValues.M * 4, null, SuSyMaterialIconType.catalystBed, 1L, OrePrefix.Conditions.hasDustProperty);
    }
}
