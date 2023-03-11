package supersymmetry.api.recipe;

import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.init.Items;
import supersymmetry.common.materials.SusyMaterials;

public class VulcanizationRecipes {
    public static void init(){
        SuSyRecipeMaps.VULCANIZATION_RECIPES.recipeBuilder()
                .duration(30)
                .fluidInputs(Materials.AceticAcid.getFluid(200))
                .input(OrePrefix.dust, SusyMaterials.Latex)
                .output(Items.POTATO)
                .EUt(32)
                .buildAndRegister();
    }
}
