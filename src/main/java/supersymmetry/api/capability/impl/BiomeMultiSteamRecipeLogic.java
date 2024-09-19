package supersymmetry.api.capability.impl;

import gregtech.api.capability.impl.RecipeLogicSteam;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.IFluidTank;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.BiomeMultiSteamMachineMetaTileEntity;
import supersymmetry.api.recipes.properties.BiomeMultiProperty;
import supersymmetry.api.recipes.properties.BiomeMultiPropertyList;

import static codechicken.lib.util.ClientUtils.getWorld;

public class BiomeMultiSteamRecipeLogic extends RecipeLogicSteam {

    public BiomeMultiSteamRecipeLogic(BiomeMultiSteamMachineMetaTileEntity tileEntity, RecipeMap recipeMap, boolean isHighPressure, IFluidTank steamFluidTank, double conversionRate) {
        super(tileEntity, recipeMap, isHighPressure, steamFluidTank, conversionRate);
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        return super.checkRecipe(recipe) && checkBiomeRequirement(recipe);
    }

    protected boolean checkBiomeRequirement(@NotNull Recipe recipe) {
        if (!recipe.hasProperty(BiomeMultiProperty.getInstance())) return true;
        Biome biome = getWorld().getBiome(this.getMetaTileEntity().getPos());
        return recipe.getProperty(BiomeMultiProperty.getInstance(), BiomeMultiPropertyList.EMPTY_LIST)
                .checkBiome(biome);
    }

    @Override
    public boolean canProgressRecipe() {
        return super.canProgressRecipe();
    }
}
