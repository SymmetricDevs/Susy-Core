package supersymmetry.api.unification.ore;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.ore.OrePrefix;

public class SusyOrePrefix {
    public static OrePrefix oreGabbro;
    public static OrePrefix oreGneiss;
    public static OrePrefix oreGraphite;
    public static OrePrefix oreLimestone;
    public static OrePrefix oreMica;
    public static OrePrefix orePhyllite;
    public static OrePrefix oreQuartzite;
    public static OrePrefix oreShale;
    public static OrePrefix oreSlate;
    public static OrePrefix oreSoapstone;

    public SusyOrePrefix(){
    }

    public static void  init(){
        oreGabbro = new OrePrefix("oreGabbro", -1L, (Material) null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        oreGneiss = new OrePrefix("oreGneiss", -1L, (Material) null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        oreGraphite = new OrePrefix("oreGraphite", -1L, (Material) null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        oreLimestone = new OrePrefix("oreLimestone", -1L, (Material) null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        oreMica = new OrePrefix("oreMica", -1L, (Material) null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        orePhyllite = new OrePrefix("orePhyllite", -1L, (Material) null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        oreQuartzite = new OrePrefix("oreQuartzite", -1L, (Material) null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        oreShale = new OrePrefix("oreShale", -1L, (Material) null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        oreSlate = new OrePrefix("oreSlate", -1L, (Material) null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
        oreSoapstone = new OrePrefix("oreSoapstone", -1L, (Material) null, MaterialIconType.ore, 1L, OrePrefix.Conditions.hasOreProperty);
    }
}
