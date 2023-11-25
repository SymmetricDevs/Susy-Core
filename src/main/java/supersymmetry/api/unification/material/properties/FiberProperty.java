package supersymmetry.api.unification.material.properties;

import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.info.MaterialFlags;
import supersymmetry.api.unification.material.info.SuSyMaterialFlags;


public class FiberProperty implements IMaterialProperty {

    // For generating wet fibers
    public boolean solutionSpun;

    // To allow for fluid generation
    private boolean meltSpun;

    public FiberProperty(boolean solutionSpun, boolean meltSpun) {
        this.solutionSpun = solutionSpun;
        this.meltSpun = meltSpun;
    }

    // Default constructor
    public FiberProperty() {
        this(true, true);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (properties.hasProperty(PropertyKey.FLUID) && !this.meltSpun) { throw new IllegalStateException("Material " + properties.getMaterial() + " has both a fluid property and is not a melt spun fiber, which is not allowed!"); }
        if (this.solutionSpun) { properties.getMaterial().addFlags(SuSyMaterialFlags.GENERATE_WET_FIBER); }

        properties.getMaterial().addFlags(SuSyMaterialFlags.GENERATE_FIBER, SuSyMaterialFlags.GENERATE_THREAD, MaterialFlags.DISABLE_DECOMPOSITION);
    }
}
