package supersymmetry.api.capability.impl;

import gregtech.api.capability.impl.RecipeLogicSteam;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.IFluidTank;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.BiomeMultiSteamMachineMetaTileEntity;
import supersymmetry.api.recipes.properties.BiomeMultiProperty;

public class BiomeMultiSteamRecipeLogic extends RecipeLogicSteam {

    public BiomeMultiSteamRecipeLogic(BiomeMultiSteamMachineMetaTileEntity tileEntity, RecipeMap recipeMap, boolean isHighPressure, IFluidTank steamFluidTank, double conversionRate) {
        super(tileEntity, recipeMap, isHighPressure, steamFluidTank, conversionRate);
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        return super.checkRecipe(recipe);
    }

    @Override
    public boolean canProgressRecipe() {
        return super.canProgressRecipe();
    }
}
