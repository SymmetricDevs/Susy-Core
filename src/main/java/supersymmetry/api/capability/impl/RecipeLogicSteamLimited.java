package supersymmetry.api.capability.impl;

import net.minecraftforge.fluids.IFluidTank;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.RecipeLogicSteam;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeMap;

public class RecipeLogicSteamLimited extends RecipeLogicSteam {

    public RecipeLogicSteamLimited(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, boolean isHighPressure,
                                   IFluidTank steamFluidTank, double conversionRate) {
        super(tileEntity, recipeMap, isHighPressure, steamFluidTank, conversionRate);
    }

    @Override
    public long getMaxVoltage() {
        return GTValues.V[GTValues.ULV];
    }
}
