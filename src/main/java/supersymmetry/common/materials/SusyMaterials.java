package supersymmetry.common.materials;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.GTLog;

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
    }

    private static void changeProperties() {
        removeProperty(PropertyKey.ORE, Materials.Graphite);
        removeProperty(PropertyKey.ORE, Materials.Soapstone);
        removeProperty(PropertyKey.ORE, Materials.Quartzite);
        removeProperty(PropertyKey.ORE, Materials.Mica);
    }

    private static void removeProperty(PropertyKey<?> key, Material material) {
        Map<PropertyKey<? extends IMaterialProperty<?>>, IMaterialProperty<?>> map = null;
        try {
            Field field = MaterialProperties.class.getDeclaredField("propertyMap");
            field.setAccessible(true);
            //noinspection unchecked
            map = (Map<PropertyKey<? extends IMaterialProperty<?>>, IMaterialProperty<?>>) field.get(material.getProperties());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            GTLog.logger.error("Failed to reflect material property map", e);
        }
        if (map != null) {
            map.remove(key);
        }
    }
}
