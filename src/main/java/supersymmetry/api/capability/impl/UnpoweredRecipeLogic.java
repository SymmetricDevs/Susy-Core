package supersymmetry.api.capability.impl;

import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import org.jetbrains.annotations.NotNull;

public class UnpoweredRecipeLogic extends AbstractRecipeLogic {

    public UnpoweredRecipeLogic(MetaTileEntity metaTileEntity, RecipeMap<?> recipeMap) {
        super(metaTileEntity, recipeMap);
        setAllowOverclocking(false);
    }

    @Override
    protected long getEnergyInputPerSecond() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected long getEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected long getEnergyCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        return true;
    }

    @Override
    public long getMaxVoltage() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getInfoProviderEUt() {
        return 0;
    }
}
