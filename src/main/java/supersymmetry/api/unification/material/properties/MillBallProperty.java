package supersymmetry.api.unification.material.properties;

import com.github.bsideup.jabel.Desugar;

import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PropertyKey;

@Desugar
public record MillBallProperty(int durability) implements IMaterialProperty {

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(PropertyKey.DUST);
    }
}
