package supersymmetry.api.rocketry;

import java.util.List;

import gregtech.api.recipes.ingredients.GTRecipeInput;

public interface AssemblyStep {

    List<GTRecipeInput> getRecipeInputs();

    double getAssemblyDuration();

    default double getElectrodeDamageFactor() {
        return getAssemblyDuration();
    }
}
