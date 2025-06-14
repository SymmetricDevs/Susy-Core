package supersymmetry.loaders.recipes;

import supersymmetry.api.recipes.SuSyRecipeMaps;

import static gregtech.api.unification.material.Materials.Air;

public class CoagulationRecipes {
    public static void init() {
        SuSyRecipeMaps.CLARIFIER.recipeBuilder()
                .fluidInputs(Air.getFluid(100))
                .fluidOutputs(Air.getFluid(100))
                .duration(200)
                .EUt(30)
                .buildAndRegister();
    }

}

