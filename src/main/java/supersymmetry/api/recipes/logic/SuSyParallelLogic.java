package supersymmetry.api.recipes.logic;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.IVoidable;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
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
            // Three parameters: the output bus room, the number of outputs, and the max parallel modifier
            int parallelizable = Math.min(limitByOutputMerging(currentRecipe, exportInventory, exportFluids, multiplierByInputs, voidItems, voidFluids),
                    limitByOutputSize(currentRecipe, parallelAmount));
            parallelizable = Math.min(parallelizable, parallelAmount);
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

    // Limits parallelization by the size of the output.
    public static int limitByOutputSize(@NotNull Recipe recipe, int parallelAmount) {
        int itemCount = 0;
        for (ItemStack output : recipe.getOutputs()) {
            if (output != null) {
                itemCount += output.getCount();
            }
        }
        int fluidCount = 0;
        for (FluidStack output : recipe.getFluidOutputs()) {
            if (output != null) {
                fluidCount += output.amount;
            }
        }
        fluidCount += 143; // Round it up
        fluidCount /= 144;
        int totalCount = itemCount + fluidCount;
        return Math.abs((int)(parallelAmount / (long)totalCount));
    }

}
