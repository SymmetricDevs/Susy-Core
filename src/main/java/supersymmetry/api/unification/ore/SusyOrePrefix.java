package supersymmetry.api.unification.ore;

import gregtech.api.GTValues;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.client.resources.I18n;
import supersymmetry.api.unification.material.info.SuSyMaterialFlags;
import supersymmetry.api.unification.material.info.SuSyMaterialIconType;
import supersymmetry.api.unification.material.properties.SuSyPropertyKey;

import java.util.Collections;

import static gregtech.api.unification.ore.OrePrefix.Flags.ENABLE_UNIFICATION;

public class SusyOrePrefix {
    public static OrePrefix oreGabbro = new OrePrefix("oreGabbro", -1L, null, MaterialIconType.ore, OrePrefix.Flags.ENABLE_UNIFICATION, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix oreGneiss = new OrePrefix("oreGneiss", -1L, null, MaterialIconType.ore, OrePrefix.Flags.ENABLE_UNIFICATION, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix oreLimestone = new OrePrefix("oreLimestone", -1L, null, MaterialIconType.ore, OrePrefix.Flags.ENABLE_UNIFICATION, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix orePhyllite = new OrePrefix("orePhyllite", -1L, null, MaterialIconType.ore, OrePrefix.Flags.ENABLE_UNIFICATION, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix oreQuartzite = new OrePrefix("oreQuartzite", -1L, null, MaterialIconType.ore, OrePrefix.Flags.ENABLE_UNIFICATION, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix oreShale = new OrePrefix("oreShale", -1L, null, MaterialIconType.ore, OrePrefix.Flags.ENABLE_UNIFICATION, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix oreSlate = new OrePrefix("oreSlate", -1L, null, MaterialIconType.ore, OrePrefix.Flags.ENABLE_UNIFICATION, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix oreSoapstone = new OrePrefix("oreSoapstone", -1L, null, MaterialIconType.ore, OrePrefix.Flags.ENABLE_UNIFICATION, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix oreKimberlite = new OrePrefix("oreKimberlite", -1L, null, MaterialIconType.ore, OrePrefix.Flags.ENABLE_UNIFICATION, OrePrefix.Conditions.hasOreProperty);
    public static OrePrefix catalystBed = new OrePrefix("catalystBed", GTValues.M * 4, null, SuSyMaterialIconType.catalystBed, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> mat.hasFlag(SuSyMaterialFlags.GENERATE_CATALYST_BED));
    public static OrePrefix catalystPellet = new OrePrefix("catalystPellet", GTValues.M / 4, null, SuSyMaterialIconType.catalystPellet, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> mat.hasFlag(SuSyMaterialFlags.GENERATE_CATALYST_PELLET));

    // Sheeted Frames (10 ingots of materials in, 12 sheeted frames out)
    //M *10 / 12 [5/6] = materialAmount (I do not know what M means), 1L = ENABLE_UNIFICATION
    //public static OrePrefix frameGt = new OrePrefix("frameGt", M * 2, null, null, ENABLE_UNIFICATION, material -> material.hasFlag(GENERATE_FRAME)); [for reference]
    public static OrePrefix sheetedFrame = new OrePrefix("sheetedFrame", (GTValues.M * 5) / 6, null, SuSyMaterialIconType.sheetedFrame, ENABLE_UNIFICATION, (material) -> material.hasFlag(MaterialFlags.GENERATE_FRAME));

    // Tiered Catalysts Beds

    public static OrePrefix catalystBedReduction = new OrePrefix("catalystBedReduction", GTValues.M, null, SuSyMaterialIconType.catalystBed, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> false);
    public static OrePrefix catalystBedOxidation = new OrePrefix("catalystBedOxidation", GTValues.M, null, SuSyMaterialIconType.catalystBed, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> false);
    public static OrePrefix catalystBedCracking = new OrePrefix("catalystBedCracking", GTValues.M, null, SuSyMaterialIconType.catalystBed, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> false);
    public static OrePrefix catalystBedZieglerNatta = new OrePrefix("catalystBedZieglerNatta", GTValues.M, null, SuSyMaterialIconType.catalystBed, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> false);

    // Tiered Catalyst Pellets

    public static OrePrefix catalystPelletReduction = new OrePrefix("catalystPelletReduction", GTValues.M * 4, null, SuSyMaterialIconType.catalystPellet, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> false);
    public static OrePrefix catalystPelletOxidation = new OrePrefix("catalystPelletOxidation", GTValues.M * 4, null, SuSyMaterialIconType.catalystPellet, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> false);
    public static OrePrefix catalystPelletCracking = new OrePrefix("catalystPelletCracking", GTValues.M * 4, null, SuSyMaterialIconType.catalystPellet, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> false);
    public static OrePrefix catalystPelletZieglerNatta = new OrePrefix("catalystPelletZieglerNatta", GTValues.M * 4, null, SuSyMaterialIconType.catalystPellet, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> false);

    // Ore Processing Intermediates

    public static final OrePrefix sifted = new OrePrefix("dustSifted", -1, null, SuSyMaterialIconType.sifted, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> mat.hasFlag(SuSyMaterialFlags.GENERATE_SIFTED));
    public static final OrePrefix flotated = new OrePrefix("dustFlotated", -1, null, SuSyMaterialIconType.flotated, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> mat.hasFlag(SuSyMaterialFlags.GENERATE_FLOTATED));
    public static final OrePrefix concentrate = new OrePrefix("dustConcentrate", -1, null, SuSyMaterialIconType.concentrate, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> mat.hasFlag(SuSyMaterialFlags.GENERATE_CONCENTRATE));

    // Fiber
    public static final OrePrefix fiber = new OrePrefix("fiber", GTValues.M / 8, null, SuSyMaterialIconType.fiber, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> mat.hasFlag(SuSyMaterialFlags.GENERATE_FIBER));
    public static final OrePrefix wetFiber = new OrePrefix("fiberWet", GTValues.M / 8, null, SuSyMaterialIconType.wetFiber, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> mat.hasFlag(SuSyMaterialFlags.GENERATE_WET_FIBER));
    public static final OrePrefix thread = new OrePrefix("thread", GTValues.M / 8, null, SuSyMaterialIconType.thread, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> mat.hasFlag(SuSyMaterialFlags.GENERATE_THREAD));

    // Wet dust
    public static final OrePrefix dustWet = new OrePrefix("dustWet", -1, null, SuSyMaterialIconType.dustWet, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> mat.hasFlag(SuSyMaterialFlags.GENERATE_WET_DUST));

    // Nuclear stuff, introduced by Zalgo and Bruberu
    public static final OrePrefix fuelRod = new OrePrefix("fuelRod", -1, null, SuSyMaterialIconType.fuelRod, 0,
            material -> material.hasProperty(SuSyPropertyKey.FISSION_FUEL),
            mat -> Collections.singletonList(I18n.format("metaitem.nuclear.tooltip.radioactive")));
    public static final OrePrefix fuelRodDepleted = new OrePrefix("fuelRodDepleted", -1, null,
            SuSyMaterialIconType.fuelRodDepleted, 0, material -> material.hasProperty(SuSyPropertyKey.FISSION_FUEL),
            mat -> Collections.singletonList(I18n.format("metaitem.nuclear.tooltip.radioactive")));
    public static final OrePrefix fuelRodHotDepleted = new OrePrefix("fuelRodHotDepleted", -1, null,
            SuSyMaterialIconType.fuelRodHotDepleted, 0, material -> material.hasProperty(SuSyPropertyKey.FISSION_FUEL),
            mat -> Collections.singletonList(I18n.format("metaitem.nuclear.tooltip.radioactive")));
    public static final OrePrefix fuelPellet = new OrePrefix("fuelPellet", -1, null,
            SuSyMaterialIconType.fuelPellet, 0, material -> material.hasProperty(SuSyPropertyKey.FISSION_FUEL),
            mat -> Collections.singletonList(I18n.format("metaitem.nuclear.tooltip.radioactive")));
    public static final OrePrefix fuelPelletDepleted = new OrePrefix("fuelPelletDepleted", -1, null,
            SuSyMaterialIconType.fuelPelletDepleted, 0, material -> material.hasProperty(SuSyPropertyKey.FISSION_FUEL),
            mat -> Collections.singletonList(I18n.format("metaitem.nuclear.tooltip.radioactive")));

    public static final OrePrefix dustSpentFuel = new OrePrefix("dustSpentFuel", -1, null,
            SuSyMaterialIconType.dustSpentFuel, 0, material -> material.hasProperty(SuSyPropertyKey.FISSION_FUEL));
    public static final OrePrefix dustBredFuel = new OrePrefix("dustBredFuel", -1, null,
            SuSyMaterialIconType.dustBredFuel, 0, material -> material.hasProperty(SuSyPropertyKey.FISSION_FUEL));
    public static final OrePrefix dustFissionByproduct = new OrePrefix("dustFissionByproduct", -1, null,
            SuSyMaterialIconType.dustFissionByproduct, 0, material -> material.hasProperty(SuSyPropertyKey.FISSION_FUEL));
}
