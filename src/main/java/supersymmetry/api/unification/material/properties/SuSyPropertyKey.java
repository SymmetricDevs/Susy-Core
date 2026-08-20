package supersymmetry.api.unification.material.properties;

import gregtech.api.unification.material.properties.PropertyKey;

public class SuSyPropertyKey {

    public static final PropertyKey<FiberProperty> FIBER = new PropertyKey<>("fiber", FiberProperty.class);
    public static final PropertyKey<MillBallProperty> MILL_BALL = new PropertyKey<>("mill_ball",
            MillBallProperty.class);
    public static final PropertyKey<SolidRocketFuelProperty> SOLID_ROCKET_FUEL = new PropertyKey<>(
            "solid_rocket_fuel", SolidRocketFuelProperty.class);
}
