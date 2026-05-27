package supersymmetry.common.util;

import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;

public class FireSuppressantProperty implements IMaterialProperty {

    public static final PropertyKey<FireSuppressantProperty> FIRE_SUPPRESSANT =
            new PropertyKey<>("fire_suppressant", FireSuppressantProperty.class);

    private final int blocksPerTick;

    public FireSuppressantProperty() {
        this.blocksPerTick = 1;
    }

    public FireSuppressantProperty(int blocksPerTick) {
        this.blocksPerTick = blocksPerTick;
    }

    public int getBlocksPerTick() {
        return blocksPerTick;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {}
}
