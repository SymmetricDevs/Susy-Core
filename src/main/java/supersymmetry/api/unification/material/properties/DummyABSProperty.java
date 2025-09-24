package supersymmetry.api.unification.material.properties;

import org.jetbrains.annotations.NotNull;

import gregicality.multiblocks.api.recipes.alloyblast.AlloyBlastRecipeProducer;
import gregicality.multiblocks.api.unification.properties.AlloyBlastProperty;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.BlastProperty;

public class DummyABSProperty extends AlloyBlastProperty {

    public DummyABSProperty(int temperature) {
        super(temperature);
        this.setRecipeProducer(new AlloyBlastRecipeProducer() {

            @Override
            public void produce(@NotNull Material material, @NotNull BlastProperty blastProperty) {
                // Nothing!
            }
        });
    }

    public DummyABSProperty() {
        this(373);
    }
}
