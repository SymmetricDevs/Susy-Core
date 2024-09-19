package supersymmetry.api.capability.impl;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.BiomeMultiMachineMetaTileEntity;
import supersymmetry.api.recipes.properties.BiomeMultiProperty;
import supersymmetry.api.recipes.properties.BiomeMultiPropertyList;

import java.util.function.Supplier;

import static codechicken.lib.util.ClientUtils.getWorld;
import static gregtech.api.capability.GregtechDataCodes.WORKABLE_ACTIVE;

public class BiomeMultiRecipeLogic extends RecipeLogicEnergy {

    public BiomeMultiRecipeLogic(BiomeMultiMachineMetaTileEntity tileEntity, RecipeMap recipeMap, Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap, energyContainer);
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
