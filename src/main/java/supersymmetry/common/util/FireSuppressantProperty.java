package supersymmetry.common.util;

import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;

public class FireSuppressantProperty implements IMaterialProperty {

    public static final PropertyKey<FireSuppressantProperty> FIRE_SUPPRESSANT =
            new PropertyKey<>("fire_suppressant", FireSuppressantProperty.class);

    @Override
    public void verifyProperty(MaterialProperties properties) {}
}
