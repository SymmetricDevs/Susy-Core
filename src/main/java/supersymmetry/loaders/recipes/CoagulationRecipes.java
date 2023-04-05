package supersymmetry.loaders.recipes;

import gregtech.api.unification.ore.OrePrefix;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.materials.SusyMaterials;

public class CoagulationRecipes {
    public static void init() {

        SuSyRecipeMaps.COAGULATION_RECIPES.recipeBuilder()
                .duration(1)
                .fluidInputs(SusyMaterials.Latex.getFluid(1000))
                .output(OrePrefix.dust, SusyMaterials.Latex)
                .EUt(1)
                .buildAndRegister();

    }

}

