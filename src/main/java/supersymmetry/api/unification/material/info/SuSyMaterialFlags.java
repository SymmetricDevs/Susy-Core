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


    public SuSyMaterialFlags(){
    }
}
