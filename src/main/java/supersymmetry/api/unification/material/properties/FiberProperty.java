package supersymmetry.api.unification.material.properties;

import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.info.MaterialFlags;
import supersymmetry.api.unification.material.info.SuSyMaterialFlags;


public class FiberProperty implements IMaterialProperty {

    // For generating wet fibers
    private boolean solutionSpun;

    // To prevent a fluid and a HR-fiber from coinciding
    private boolean heatResistant;

    public FiberProperty(boolean solutionSpun, boolean heatResistant) {
        this.solutionSpun = solutionSpun;
        this.heatResistant = heatResistant;
    }

    // Default constructor
    public FiberProperty() {
        this(true, true);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (properties.hasProperty(PropertyKey.FLUID) && this.heatResistant) { throw new IllegalStateException("Material " + properties.getMaterial() + " has both Fluid and Heat Resistant Fiber Property, which is not allowed!"); }
        
        if (this.solutionSpun) { properties.getMaterial().addFlags(SuSyMaterialFlags.GENERATE_WET_FIBER); }
        properties.getMaterial().addFlags(MaterialFlags.GENERATE_FINE_WIRE, MaterialFlags.DISABLE_DECOMPOSITION, MaterialFlags.NO_UNIFICATION);
    }
}
