package supersymmetry.api.unification.material.properties;

import gregtech.api.unification.material.properties.PropertyKey;

public class SuSyPropertyKey {
    public static final PropertyKey<FiberProperty> FIBER = new PropertyKey<>("fiber", FiberProperty.class);
    public static final PropertyKey<CoolantProperty> COOLANT = new PropertyKey<>("coolant", CoolantProperty.class);
    public static final PropertyKey<FissionFuelProperty> FISSION_FUEL = new PropertyKey<>("fission_fuel", FissionFuelProperty.class);
}
