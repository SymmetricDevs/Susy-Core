package supersymmetry.loaders.recipes;

import gregtech.api.recipes.GTRecipeHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import supersymmetry.api.recipes.SuSyRecipeMaps;

import static gregtech.api.GTValues.LV;
import static gregtech.api.GTValues.V;
import static gregtech.api.unification.material.Materials.Creosote;

public class BoilerRecipes {
    public static void init() {
        SuSyRecipeMaps.BOILER_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.Wood)
                .duration(160)
                .EUt((int) V[LV])
                .buildAndRegister();

        SuSyRecipeMaps.BOILER_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.Charcoal)
                .duration(1600)
                .EUt((int) V[LV])
                .buildAndRegister();

        SuSyRecipeMaps.BOILER_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.Coal)
                .duration(1984)
                .EUt((int) V[LV])
                .buildAndRegister();

        GTRecipeHandler.removeAllRecipes(RecipeMaps.SEMI_FLUID_GENERATOR_FUELS);
        RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.recipeBuilder()
                .fluidInputs(Creosote.getFluid(1000))
                .duration(1920)
                .EUt((int) V[LV])
                .buildAndRegister();
    }
}
