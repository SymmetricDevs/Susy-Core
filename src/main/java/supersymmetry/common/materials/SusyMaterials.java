package supersymmetry.common.materials;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;

import gregtech.api.GregTechAPI;
import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.store.FluidStorageImpl;
import gregtech.api.fluids.store.FluidStorageKey;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.*;
import net.minecraftforge.fluids.Fluid;
import supersymmetry.api.SusyLog;
import supersymmetry.api.fluids.SusyFluidStorageKeys;
import supersymmetry.api.unification.material.info.SuSyMaterialFlags;

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
    public static Material Anorthosite;
    public static Material Latex;
    public static Material Mud;
    public static Material Seawater;
    public static Material MidgradeLubricant;
    public static Material PremiumLubricant;
    public static Material SupremeLubricant;
    public static Material Coolant;
    public static Material AdvancedCoolant;
    public static Material LubricatingOil;
    public static Material MetallizedBoPET;
    public static Material AluminiumAlloy6061;
    public static Material AluminiumAlloy7075;

    public static Material RefractoryGunningMixture;

    // Minerals
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

    // Thermodynamic materials
    public static Material PreheatedAir;

    public static Material RP_1;

    // Fluorinated Ketones
    public static Material Perfluoro2Methyl3Pentanone;
    public static Material WarmPerfluoro2Methyl3Pentanone;

    // Fuels
    public static Material LOX;

    public static void init() {
        SuSyElementMaterials.init();
        SuSyFirstDegreeMaterials.init();
        SuSySecondDegreeMaterials.init();
        SuSyOrganicChemistryMaterials.init();
        SuSyHighDegreeMaterials.init();
        SuSyUnknownCompositionMaterials.init();
    }

    public static void removeFlags() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasFlag(MaterialFlags.DECOMPOSITION_BY_ELECTROLYZING))
                removeFlag(MaterialFlags.DECOMPOSITION_BY_ELECTROLYZING, material);
        }
    }


    public static void changeProperties() {
        // removeProperty(PropertyKey.ORE, Materials.Graphite);

        removeProperty(PropertyKey.ORE, Materials.Soapstone);
        removeProperty(PropertyKey.ORE, Materials.Quartzite);
        removeProperty(PropertyKey.ORE, Materials.Mica);
        removeProperty(PropertyKey.FLUID_PIPE, Materials.Lead);
        Materials.Lead.setProperty(PropertyKey.FLUID_PIPE, new FluidPipeProperties(1200, 8, true, true, false, false));

        // Add dusts and fluids for elements that do not have them
        Materials.Iodine.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Scandium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Germanium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Selenium.setProperty(PropertyKey.DUST, new DustProperty());

        Materials.Bromine.setProperty(PropertyKey.FLUID,
                new FluidProperty(FluidStorageKeys.LIQUID, new FluidBuilder()));

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

        Materials.CalciumChloride.setProperty(PropertyKey.FLUID,
                new FluidProperty(SusyFluidStorageKeys.MOLTEN, new FluidBuilder()));

        Materials.MagnesiumChloride.setProperty(PropertyKey.FLUID,
                new FluidProperty(SusyFluidStorageKeys.MOLTEN, new FluidBuilder()));

        Materials.RockSalt.setProperty(PropertyKey.FLUID,
                new FluidProperty(SusyFluidStorageKeys.MOLTEN, new FluidBuilder()));

        Materials.Salt.setProperty(PropertyKey.FLUID,
                new FluidProperty(SusyFluidStorageKeys.MOLTEN, new FluidBuilder()));

        Materials.SodiumHydroxide.setProperty(PropertyKey.FLUID,
                new FluidProperty(SusyFluidStorageKeys.MOLTEN, new FluidBuilder()));

        Materials.Sodium.setProperty(PropertyKey.FLUID,
                new FluidProperty(SusyFluidStorageKeys.MOLTEN, new FluidBuilder()));

        Materials.Phosphorus.setProperty(PropertyKey.INGOT, new IngotProperty());
        Materials.Phosphorus.setProperty(PropertyKey.FLUID,
                new FluidProperty(SusyFluidStorageKeys.MOLTEN, new FluidBuilder().temperature(317)));
        Materials.Phosphorus.setMaterialRGB(0xfffed6);

        Materials.HydrochloricAcid.setFormula("(H2O)(HCl)", true);

        Materials.HydrofluoricAcid.setFormula("(H2O)(HF)", true);

        removeProperty(PropertyKey.FLUID, Materials.Dimethyldichlorosilane);
        Materials.Dimethyldichlorosilane.setProperty(PropertyKey.FLUID,
                new FluidProperty(FluidStorageKeys.LIQUID, new FluidBuilder()));

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

        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {

            DustProperty dustProperty = material.getProperty(PropertyKey.DUST);
            if (dustProperty != null) {

                FluidProperty fluidProperty = material.getProperty(PropertyKey.FLUID);
                if (fluidProperty != null) {

                    FluidStorageKey fluidState = fluidProperty.getPrimaryKey();
                    if (fluidState == FluidStorageKeys.LIQUID) {

                        removeFluidKey(FluidStorageKeys.LIQUID, material);
                        fluidProperty.enqueueRegistration(SusyFluidStorageKeys.MOLTEN, new FluidBuilder());
                    }
                }
            }
        }

        // Exceptions (Could probably condense)
        removeFluidKey(SusyFluidStorageKeys.MOLTEN, SusyMaterials.Latex);
        SusyMaterials.Latex.getProperty(PropertyKey.FLUID).enqueueRegistration(FluidStorageKeys.LIQUID, new FluidBuilder());
        removeFluidKey(SusyFluidStorageKeys.MOLTEN, Materials.Concrete);
        Materials.Concrete.getProperty(PropertyKey.FLUID).enqueueRegistration(FluidStorageKeys.LIQUID, new FluidBuilder());
        removeFluidKey(SusyFluidStorageKeys.MOLTEN, Materials.Ice);
        Materials.Ice.getProperty(PropertyKey.FLUID).enqueueRegistration(FluidStorageKeys.LIQUID, new FluidBuilder());
    }

    private static void removeFluidKey(FluidStorageKey key, Material material) {
        FluidProperty fluidProperty = material.getProperty(PropertyKey.FLUID);
        if (fluidProperty == null) return;

        try {
            Field storageField = FluidProperty.class.getDeclaredField("storage");
            storageField.setAccessible(true);
            FluidStorageImpl storage = (FluidStorageImpl) storageField.get(fluidProperty);

            Field mapField = FluidStorageImpl.class.getDeclaredField("map");
            mapField.setAccessible(true);
            // noinspection unchecked
            Map<FluidStorageKey, Fluid> map = (Map<FluidStorageKey, Fluid>) mapField.get(storage);
            map.keySet().removeIf(k -> k == key);

            Field toRegField = FluidStorageImpl.class.getDeclaredField("toRegister");
            toRegField.setAccessible(true);
            // noinspection unchecked
            Map<FluidStorageKey, FluidBuilder> toReg = (Map<FluidStorageKey, FluidBuilder>) toRegField.get(storage);

            if (toReg != null) {
                toReg.keySet().removeIf(k -> k == key);
            }

            if (key == fluidProperty.getPrimaryKey()) {
                fluidProperty.setPrimaryKey(null);
            }

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed removing FluidStorageKey", e);
        }
    }

    private static void removeProperty(PropertyKey<?> key, Material material) {
        Map<PropertyKey<?>, IMaterialProperty> map = null;
        try {
            Field field = MaterialProperties.class.getDeclaredField("propertyMap");
            field.setAccessible(true);
            // noinspection unchecked
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
            // noinspection unchecked
            set = (HashSet<MaterialFlag>) field.get(field2.get(material));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            SusyLog.logger.error("Failed to reflect material flag hashset", e);
        }
        if (set != null) {
            set.remove(flag);
        }
    }
}
