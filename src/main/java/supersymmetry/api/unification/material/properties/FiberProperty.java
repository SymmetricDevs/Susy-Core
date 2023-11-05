package gregtech.api.unification.material.properties;

public class FiberProperty implements IMaterialProperty {

    // For generating wet fibers
    private bool solutionSpun;

    // To prevent a fluid and a HR-fiber from coinciding
    private bool heatResistant;

    public FiberProperty(bool solutionSpun, bool heatResistant) {
        this.solutionSpun = solutionSpun
        this.heatResistant = heatResistant
    }

    // Default constructor
    public FiberProperty() {
        this(true, true);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (properties.hasProperty(PropertyKey.FLUID) && this.heatResistant) { throw new IllegalStateException("Material " + properties.getMaterial() + " has both Fluid and Heat Resistant Fiber Property, which is not allowed!"); }
        
        if (this.solutionSpun) { properties.getMaterial().addFlags(MaterialFlags.GENERATE_WET_FIBER); }
        properties.getMaterial().addFlags(MaterialFlags.GENERATE_FINE_WIRE MaterialFlags.DISABLE_DECOMPOSITION, MaterialFlags.NO_UNIFICATION);
    }
}