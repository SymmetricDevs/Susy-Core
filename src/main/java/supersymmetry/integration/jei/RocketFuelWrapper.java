package supersymmetry.integration.jei;

import java.util.stream.Collectors;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;

public class RocketFuelWrapper implements IRecipeWrapper {

    private final RocketFuelEntry entry;

    public RocketFuelWrapper(RocketFuelEntry entry) {
        this.entry = entry;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.FLUID, entry.getComposition().stream()
                .map((t) -> t.getFirst().getFluid(t.getSecond()))
                .collect(Collectors.toList()));
    }
}
