package supersymmetry.common.materials;

import gregtech.api.GregTechAPI;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.*;
import supersymmetry.api.SusyLog;
import supersymmetry.api.unification.material.info.SuSyMaterialFlags;

import java.lang.reflect.Field;
import java.util.HashSet;
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
    public static Material Mud;
    public static Material Seawater;

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
        SuSyElementMaterials.init();
        SuSyFirstDegreeMaterials.init();
        SuSySecondDegreeMaterials.init();
        SuSyOrganicChemistryMaterials.init();
        SuSyHighDegreeMaterials.init();
        SuSyUnknownCompositionMaterials.init();
        changeProperties();
    }

    public static void removeFlags() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasFlag(MaterialFlags.DECOMPOSITION_BY_ELECTROLYZING)) removeFlag(MaterialFlags.DECOMPOSITION_BY_ELECTROLYZING, material);
        }
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

        Materials.Thallium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.CalciumChloride.setProperty(PropertyKey.FLUID, new FluidProperty());

        Materials.MagnesiumChloride.setProperty(PropertyKey.FLUID, new FluidProperty());

        Materials.RockSalt.setProperty(PropertyKey.FLUID, new FluidProperty());

        Materials.Salt.setProperty(PropertyKey.FLUID, new FluidProperty());

        Materials.SodiumHydroxide.setProperty(PropertyKey.FLUID, new FluidProperty());

        Materials.Sodium.setProperty(PropertyKey.FLUID, new FluidProperty());

        Materials.Phosphorus.setProperty(PropertyKey.INGOT, new IngotProperty());
        FluidProperty fluidProperty = new FluidProperty();
        fluidProperty.getStorage().enqueueRegistration(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(317));
        Materials.Phosphorus.setProperty(PropertyKey.FLUID, fluidProperty);
        Materials.Phosphorus.setMaterialRGB(0xfffed6);

        Materials.HydrochloricAcid.setFormula("(H2O)(HCl)", true);

        Materials.HydrofluoricAcid.setFormula("(H2O)(HF)", true);

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

        Materials.Cobalt.addFlags(SuSyMaterialFlags.GENERATE_CATALYST_BED);

        Materials.Palladium.addFlags(SuSyMaterialFlags.GENERATE_CATALYST_BED);

        Materials.Rhodium.addFlags(SuSyMaterialFlags.GENERATE_CATALYST_BED);

        Materials.Copper.addFlags(SuSyMaterialFlags.GENERATE_CATALYST_BED);

        Materials.Electrum.setProperty(PropertyKey.ORE, new OreProperty());

        Materials.Hydrogen.addFlags(MaterialFlags.FLAMMABLE);
    }

    private static void removeProperty(PropertyKey<?> key, Material material) {
        Map<PropertyKey<?>, IMaterialProperty> map = null;
        try {
            Field field = MaterialProperties.class.getDeclaredField("propertyMap");
            field.setAccessible(true);
            //noinspection unchecked
            map = (Map<PropertyKey<?>, IMaterialProperty>) field.get(material.getProperties());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            SusyLog.logger.error("Failed to reflect material property map", e);
        }
        if (map != null) {
            map.remove(key);
        }
    }

    private static void removeFlag(MaterialFlag flag, Material material) {
        HashSet<MaterialFlag> set = null;
        try {
            Field field = MaterialFlags.class.getDeclaredField("flags");
            field.setAccessible(true);

            Field field2 = Material.class.getDeclaredField("flags");
            field2.setAccessible(true);
            //noinspection unchecked
            set = (HashSet<MaterialFlag>) field.get(field2.get(material));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            SusyLog.logger.error("Failed to reflect material flag hashset", e);
        }
        if (set != null) {
            set.remove(flag);
        }
    }
}
