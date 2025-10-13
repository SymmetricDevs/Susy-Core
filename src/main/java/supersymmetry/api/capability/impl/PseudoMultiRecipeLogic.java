package supersymmetry.api.capability.impl;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import supersymmetry.api.metatileentity.PseudoMultiMachineMetaTileEntity;
import supersymmetry.api.recipes.properties.PseudoMultiProperty;

public class PseudoMultiRecipeLogic extends RecipeLogicEnergy {

    private final PseudoMultiMachineMetaTileEntity pmMTE;

    public PseudoMultiRecipeLogic(PseudoMultiMachineMetaTileEntity tileEntity, RecipeMap recipeMap,
                                  Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap, energyContainer);
        this.pmMTE = tileEntity;
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        if (pmMTE.getTargetBlockState() == null) return false; // if world was remote or null
        return !recipe.hasProperty(PseudoMultiProperty.getInstance()) ||
                recipe.getProperty(PseudoMultiProperty.getInstance(), null)
                        .getValidBlockStates().contains(pmMTE.getTargetBlockState()) && super.checkRecipe(recipe);
    }

    @Override
    public boolean canProgressRecipe() {
        return previousRecipe == null || !previousRecipe.hasProperty(PseudoMultiProperty.getInstance()) ||
                previousRecipe.getProperty(PseudoMultiProperty.getInstance(), null).getValidBlockStates()
                        .contains(pmMTE.getTargetBlockState()) && super.canProgressRecipe();
    }
}
