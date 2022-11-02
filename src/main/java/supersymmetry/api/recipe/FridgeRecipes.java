package supersymmetry.api.recipe;

import net.minecraft.init.Items;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

public class FridgeRecipes {

    public static void init() {

        SuSyRecipeMaps.COOLING_RECIPES.recipeBuilder()
                .EUt(480)
                .duration(1)
                .input(Items.POTATO)
                .output(SuSyMetaTileEntities.MAGNETIC_REFRIGERATOR)
                .temperature(2)
                .buildAndRegister();

    }

}
