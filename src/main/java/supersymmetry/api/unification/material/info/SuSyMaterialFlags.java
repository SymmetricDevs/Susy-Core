package supersymmetry.api.unification.material.info;

import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.properties.PropertyKey;

public class SuSyMaterialFlags {

    public static MaterialFlag GENERATE_CATALYST_PELLET = (new MaterialFlag.Builder("generate_catalyst_bed"))
            .requireProps(new PropertyKey[]{PropertyKey.DUST})
            .build();

    public static MaterialFlag GENERATE_CATALYST_BED = (new MaterialFlag.Builder("generate_catalyst_bed"))
            .requireProps(new PropertyKey[]{PropertyKey.DUST})
            .requireFlags(GENERATE_CATALYST_PELLET)
            .build();

    public static MaterialFlag GENERATE_SIFTED = (new MaterialFlag.Builder("generate_sifted"))
            .requireProps(new PropertyKey[]{PropertyKey.ORE})
            .build();

    public static MaterialFlag GENERATE_FLOTATED = (new MaterialFlag.Builder("generate_flotated"))
            .requireProps(new PropertyKey[]{PropertyKey.ORE})
            .build();

    public static MaterialFlag GENERATE_CONCENTRATE = (new MaterialFlag.Builder("generate_concentrate"))
            .requireProps(new PropertyKey[]{PropertyKey.ORE})
            .build();

    public static MaterialFlag GENERATE_SLURRIES = (new MaterialFlag.Builder("generate_slurries"))
            .requireProps(new PropertyKey[]{PropertyKey.ORE})
            .build();

    public SuSyMaterialFlags(){
    }
}
