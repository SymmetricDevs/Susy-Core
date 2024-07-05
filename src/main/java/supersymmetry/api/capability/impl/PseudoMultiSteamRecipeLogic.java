package supersymmetry.api.capability.impl;

import gregtech.api.capability.impl.RecipeLogicSteam;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.IFluidTank;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.PseudoMultiSteamMachineMetaTileEntity;
import supersymmetry.api.recipes.properties.PseudoMultiProperty;

public class PseudoMultiSteamRecipeLogic extends RecipeLogicSteam {
    private final PseudoMultiSteamMachineMetaTileEntity pmsMTE;

    public PseudoMultiSteamRecipeLogic(PseudoMultiSteamMachineMetaTileEntity tileEntity, RecipeMap recipeMap, boolean isHighPressure, IFluidTank steamFluidTank, double conversionRate) {
        super(tileEntity, recipeMap, isHighPressure, steamFluidTank, conversionRate);
        this.pmsMTE = tileEntity;
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        if (pmsMTE.getTargetBlockState() == null) return false; //if world was remote or null
        //if no property was given don't check if state matches
        return !recipe.hasProperty(PseudoMultiProperty.getInstance()) || recipe.getProperty(PseudoMultiProperty.getInstance(), null)
                .getValidBlockStates().contains(pmsMTE.getTargetBlockState()) && super.checkRecipe(recipe);
    }

    @Override
    public boolean canProgressRecipe() {
        //recipe stalled due to valid block removal will complete on world reload
        return previousRecipe == null || !previousRecipe.hasProperty(PseudoMultiProperty.getInstance()) ||
                previousRecipe.getProperty(PseudoMultiProperty.getInstance(), null).getValidBlockStates()
                        .contains(pmsMTE.getTargetBlockState()) && super.canProgressRecipe();
    }

    @Override
    public void onFrontFacingSet(EnumFacing newFrontFacing) {
        super.onFrontFacingSet(newFrontFacing);
        if (getVentingSide() == pmsMTE.getFrontFacing() || getVentingSide() == pmsMTE.getFrontFacing().getOpposite()) {
            setVentingSide(newFrontFacing.rotateY());
        }
    }
}
