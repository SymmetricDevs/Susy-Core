package supersymmetry.api.capability.impl;

import gregtech.api.capability.impl.RecipeLogicSteam;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import net.minecraftforge.fluids.IFluidTank;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.PseudoMultiSteamMachineMetaTileEntity;
import supersymmetry.api.recipes.properties.PseudoMultiProperty;

public class PseudoMultiSteamRecipeLogic extends RecipeLogicSteam {
    private final PseudoMultiSteamMachineMetaTileEntity pmsMTE;

    public PseudoMultiSteamRecipeLogic(PseudoMultiSteamMachineMetaTileEntity tileEntity, RecipeMap recipeMap, boolean isHighPressure, IFluidTank steamFluidTank, double conversionRate) {
        super(tileEntity, recipeMap, isHighPressure, steamFluidTank, conversionRate);
        pmsMTE = tileEntity;
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        if (this.pmsMTE.getTargetBlockState() == null) return false; //if world was remote or null
        return recipe.getProperty(PseudoMultiProperty.getInstance(), null)
                .getValidBlockStates().contains(this.pmsMTE.getTargetBlockState()) && super.checkRecipe(recipe);
    }

    @Override
    public boolean canProgressRecipe() {
        //recipe stalled due to valid block removal will complete on world reload (
        return this.previousRecipe == null || this.previousRecipe.getProperty(PseudoMultiProperty.getInstance(), null).getValidBlockStates()
                .contains(this.pmsMTE.getTargetBlockState()) && super.canProgressRecipe();
    }
}
