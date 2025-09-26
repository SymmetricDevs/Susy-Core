package supersymmetry.api.unification.material.info;

import gregicality.multiblocks.api.unification.GCYMMaterialFlags;
import gregicality.multiblocks.api.unification.properties.GCYMPropertyKey;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.PropertyKey;
import supersymmetry.api.unification.material.properties.SuSyPropertyKey;

public class SuSyMaterialFlags {

    public static final MaterialFlag GENERATE_CATALYST_PELLET = (new MaterialFlag.Builder("generate_catalyst_pellet"))
            .requireProps(new PropertyKey[] { PropertyKey.DUST })
            .build();

    public static final MaterialFlag GENERATE_CATALYST_BED = (new MaterialFlag.Builder("generate_catalyst_bed"))
            .requireProps(PropertyKey.DUST)
            .requireFlags(GENERATE_CATALYST_PELLET)
            .build();

    public static final MaterialFlag GENERATE_SIFTED = (new MaterialFlag.Builder("generate_sifted"))
            .requireProps(new PropertyKey[] { PropertyKey.ORE })
            .build();

    public static final MaterialFlag GENERATE_FLOTATED = (new MaterialFlag.Builder("generate_flotated"))
            .requireProps(new PropertyKey[] { PropertyKey.ORE })
            .build();

    public static final MaterialFlag GENERATE_CONCENTRATE = (new MaterialFlag.Builder("generate_concentrate"))
            .requireProps(new PropertyKey[] { PropertyKey.ORE })
            .build();

    public static final MaterialFlag GENERATE_FIBER = (new MaterialFlag.Builder("generate_fiber"))
            .requireProps(new PropertyKey[] { SuSyPropertyKey.FIBER })
            .build();

    public static final MaterialFlag GENERATE_WET_FIBER = (new MaterialFlag.Builder("generate_wet_fiber"))
            .requireProps(new PropertyKey[] { SuSyPropertyKey.FIBER })
            .build();

    public static final MaterialFlag GENERATE_THREAD = (new MaterialFlag.Builder("generate_thread"))
            .requireProps(new PropertyKey[] { SuSyPropertyKey.FIBER })
            .build();

    public static final MaterialFlag GENERATE_WET_DUST = (new MaterialFlag.Builder("generate_wet_dust"))
            .requireProps(new PropertyKey[] { PropertyKey.DUST })
            .build();

    public static final MaterialFlag HIP_PRESSED = (new MaterialFlag.Builder("hip_pressed"))
            .requireProps(PropertyKey.DUST)
            .requireFlags(MaterialFlags.NO_WORKING, MaterialFlags.NO_SMELTING)
            .build();

    public static final MaterialFlag SUPERALLOY = (new MaterialFlag.Builder("superalloy"))
            .requireProps(PropertyKey.DUST)
            .requireFlags(HIP_PRESSED)
            .build();

    public static final MaterialFlag CONTINUOUSLY_CAST = (new MaterialFlag.Builder("continuously_cast"))
            .requireProps(PropertyKey.DUST, PropertyKey.FLUID, GCYMPropertyKey.ALLOY_BLAST)
            .requireFlags(GCYMMaterialFlags.NO_ALLOY_BLAST_RECIPES)
            .build();

    public SuSyMaterialFlags() {}
}
