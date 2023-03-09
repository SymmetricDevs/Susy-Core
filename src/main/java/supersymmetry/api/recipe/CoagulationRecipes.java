package supersymmetry.api.recipe;

import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.materials.SusyMaterials;

public class CoagulationRecipes {
    public static void init() {

        SuSyRecipeMaps.COAGULATION_RECIPES.recipeBuilder()
                .duration(1)
                .fluidInputs(SusyMaterials.Latex.getFluid(1000))
                .output(SuSyMetaItems.COAGULATED_LATEX)
                .EUt(1)
                .buildAndRegister();

    }

}

