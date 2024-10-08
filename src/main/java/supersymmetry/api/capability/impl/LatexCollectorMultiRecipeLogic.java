package supersymmetry.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.LatexCollectorMultiMachineMetaTileEntity;
import supersymmetry.api.recipes.properties.LatexCollectorMultiProperty;

import java.util.function.Supplier;

public class LatexCollectorMultiRecipeLogic extends RecipeLogicEnergy {

    private final LatexCollectorMultiMachineMetaTileEntity pmMTE;

    public LatexCollectorMultiRecipeLogic(LatexCollectorMultiMachineMetaTileEntity tileEntity, RecipeMap recipeMap, Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap, energyContainer);
        this.pmMTE = tileEntity;
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        if (pmMTE.getTargetBlockState() == null) return false; //if world was remote or null
        return !recipe.hasProperty(LatexCollectorMultiProperty.getInstance()) || recipe.getProperty(LatexCollectorMultiProperty.getInstance(), null)
                .getValidBlockStates().contains(pmMTE.getTargetBlockState()) && super.checkRecipe(recipe);
    }

    @Override
    public boolean canProgressRecipe() {
        return previousRecipe == null || !previousRecipe.hasProperty(LatexCollectorMultiProperty.getInstance()) ||
                previousRecipe.getProperty(LatexCollectorMultiProperty.getInstance(), null).getValidBlockStates()
                        .contains(pmMTE.getTargetBlockState()) && super.canProgressRecipe();
    }
}
