package supersymmetry.api.recipes.logic;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.IVoidable;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.recipes.logic.ParallelLogic.*;

public class SuSyParallelLogic {

    /**
     * Similar to default parallel logic, but it neither multiplies the EUt nor the duration.
     */
    public static RecipeBuilder<?> pureParallelRecipe(@NotNull Recipe currentRecipe, @NotNull RecipeMap<?> recipeMap, @NotNull IItemHandlerModifiable importInventory, @NotNull IMultipleTankHandler importFluids, @NotNull IItemHandlerModifiable exportInventory, @NotNull IMultipleTankHandler exportFluids, int parallelAmount, long maxVoltage, @NotNull IVoidable voidable) {
        int multiplierByInputs = getMaxRecipeMultiplier(currentRecipe, importInventory, importFluids, parallelAmount);
        if (multiplierByInputs == 0) {
            return null;
        } else {
            RecipeBuilder<?> recipeBuilder = recipeMap.recipeBuilder().EUt(0);
            boolean voidItems = voidable.canVoidRecipeItemOutputs();
            boolean voidFluids = voidable.canVoidRecipeFluidOutputs();
            int parallelizable = limitByOutputMerging(currentRecipe, exportInventory, exportFluids, multiplierByInputs, voidItems, voidFluids);
            int recipeEUt = currentRecipe.getEUt();
            if (recipeEUt != 0) {
                if (parallelizable != 0) {
                    recipeBuilder.append(currentRecipe, Math.min(parallelizable, multiplierByInputs), false);
                    // The change: the recipeEUt is reset to the original value, along with a loss of checks to prevent the EUt from getting too high
                    recipeBuilder.EUt(currentRecipe.getEUt());
                }
            } else if (parallelizable > 0) {
                recipeBuilder.append(currentRecipe, parallelizable, false);
            }

            return recipeBuilder;
        }
    }

}
