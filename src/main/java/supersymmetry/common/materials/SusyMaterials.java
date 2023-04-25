package supersymmetry.common.materials;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.*;
import supersymmetry.api.SusyLog;
import supersymmetry.api.unification.material.info.SuSyMaterialFlags;

import java.lang.reflect.Field;
import java.util.Map;

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
    public static Material Kimberlite;
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
    public static Material Forsterite;
    public static Material Lizardite;
    public static Material Fluorite;


    public static void init() {
        changeProperties();
        SuSyElementMaterials.init();
        SuSyFirstDegreeMaterials.init();
        SuSySecondDegreeMaterials.init();
        SuSyOrganicChemistryMaterials.init();
        SuSyHighDegreeMaterials.init();
        SuSyUnknownCompositionMaterials.init();

        Latex.getProperty(PropertyKey.FLUID).setFluidTemperature(293);
    }

    private static void changeProperties() {
        //removeProperty(PropertyKey.ORE, Materials.Graphite);
        removeProperty(PropertyKey.ORE, Materials.Soapstone);
        removeProperty(PropertyKey.ORE, Materials.Quartzite);
        removeProperty(PropertyKey.ORE, Materials.Mica);
        removeProperty(PropertyKey.FLUID_PIPE, Materials.Lead);
        Materials.Lead.setProperty(PropertyKey.FLUID_PIPE, new FluidPipeProperties(1200, 8, true, true, false, false));

        //Add dusts and fluids for elements that do not have them
        Materials.Iodine.setProperty(PropertyKey.DUST, new DustProperty());

	    Materials.Scandium.setProperty(PropertyKey.DUST, new DustProperty());

	    Materials.Germanium.setProperty(PropertyKey.DUST, new DustProperty());

	    Materials.Selenium.setProperty(PropertyKey.DUST, new DustProperty());

	    Materials.Bromine.setProperty(PropertyKey.FLUID, new FluidProperty());

	    Materials.Rubidium.setProperty(PropertyKey.DUST, new DustProperty());

	    Materials.Strontium.setProperty(PropertyKey.DUST, new DustProperty());

	    Materials.Zirconium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Technetium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Tellurium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Praseodymium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Promethium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Gadolinium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Terbium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Dysprosium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Holmium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Erbium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Thulium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Ytterbium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Hafnium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Rhenium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.CalciumChloride.setProperty(PropertyKey.FLUID, new FluidProperty());

        Materials.HydrochloricAcid.setFormula("(H2O)(HCl)", true);

        removeProperty(PropertyKey.FLUID, Materials.Dimethyldichlorosilane);
        Materials.Dimethyldichlorosilane.setProperty(PropertyKey.FLUID, new FluidProperty());

        Materials.Iron3Chloride.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Nitrochlorobenzene.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Dichlorobenzene.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Dichlorobenzidine.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.PhthalicAcid.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.DiphenylIsophtalate.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Diaminobenzidine.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.PolyvinylAcetate.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Platinum.addFlags(SuSyMaterialFlags.GENERATE_CATALYST_BED);

        Materials.BandedIron.addFlags(SuSyMaterialFlags.GENERATE_CATALYST_BED);

        Materials.Cobalt.addFlags(SuSyMaterialFlags.GENERATE_CATALYST_BED);

        Materials.Palladium.addFlags(SuSyMaterialFlags.GENERATE_CATALYST_BED);

        Materials.CupricOxide.addFlags(SuSyMaterialFlags.GENERATE_CATALYST_BED);

        Materials.Rhodium.addFlags(SuSyMaterialFlags.GENERATE_CATALYST_BED);

        Materials.Copper.addFlags(SuSyMaterialFlags.GENERATE_CATALYST_BED);

    }

    private static void removeProperty(PropertyKey<?> key, Material material) {
        Map<PropertyKey<? extends IMaterialProperty<?>>, IMaterialProperty<?>> map = null;
        try {
            Field field = MaterialProperties.class.getDeclaredField("propertyMap");
            field.setAccessible(true);
            //noinspection unchecked
            map = (Map<PropertyKey<? extends IMaterialProperty<?>>, IMaterialProperty<?>>) field.get(material.getProperties());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            SusyLog.logger.error("Failed to reflect material property map", e);
        }
        if (map != null) {
            map.remove(key);
        }
    }
}
