package supersymmetry.api.capability.impl;

import net.minecraftforge.fluids.FluidStack;

import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.recipes.Recipe;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntitySUSYLargeTurbine;

public class SuSyTurbineRecipeLogic extends MultiblockFuelRecipeLogic {

    public SuSyTurbineRecipeLogic(MetaTileEntitySUSYLargeTurbine tileEntity) {
        super(tileEntity);
    }

    public FluidStack getInputFluidStack() {
        // Previous Recipe is always null on first world load, so try to acquire a new recipe
        if (previousRecipe == null) {
            Recipe recipe = findRecipe(Integer.MAX_VALUE, getInputInventory(), getInputTank());

            return recipe == null ? null : getInputTank().drain(
                    new FluidStack(recipe.getFluidInputs().get(0).getInputFluidStack().getFluid(), Integer.MAX_VALUE),
                    false);
        }
        FluidStack fuelStack = previousRecipe.getFluidInputs().get(0).getInputFluidStack();
        return getInputTank().drain(new FluidStack(fuelStack.getFluid(), Integer.MAX_VALUE), false);
    }
}
