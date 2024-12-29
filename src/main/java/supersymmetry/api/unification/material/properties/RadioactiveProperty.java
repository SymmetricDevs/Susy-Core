package supersymmetry.api.unification.material.properties;

import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;

public class RadioactiveProperty implements IMaterialProperty {

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST, true);
    }
}
