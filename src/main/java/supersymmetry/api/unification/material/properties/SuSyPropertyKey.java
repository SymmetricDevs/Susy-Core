package gregtech.api.unification.material.properties;

import gregtech.api.unification.material.properties.PropertyKey;

public class SuSyPropertyKey<T extends IMaterialProperty> {
    public static final PropertyKey<FiberProperty> FIBER = new PropertyKey<>("fiber", FiberProperty.class);
}