package supersymmetry.api.capability.impl;

import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.OverclockingLogic;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import supersymmetry.api.SusyLog;
import supersymmetry.api.recipes.builders.logic.SuSyOverclockingLogic;
import supersymmetry.api.recipes.catalysts.CatalystInfo;
import supersymmetry.api.recipes.properties.CatalystProperty;
import supersymmetry.api.recipes.properties.CatalystPropertyValue;

public class CatalystRecipeLogic extends RecipeLogicEnergy {

    protected @Nullable CatalystInfo catalystInfo;
    protected int requiredCatalystTier;

    public CatalystRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap,
                               Supplier<IEnergyContainer> energyContainer) {
        super(tileEntity, recipeMap, energyContainer);
    }

    protected void tryFindCatalystInfo(@NotNull Recipe recipe) {
        this.catalystInfo = null;
        this.requiredCatalystTier = CatalystInfo.NO_TIER;

        if (recipe.hasProperty(CatalystProperty.getInstance())) {
            CatalystPropertyValue property = recipe.getProperty(CatalystProperty.getInstance(), null);
            if (property == null) return;

            // If it is a non-tiered catalyst, no bonuses need to be calculated
            // We can safely skip the inventory scanning
            if (property.getTier() == CatalystInfo.NO_TIER) {
                return;
            }

            // find the best catalyst in the inventory, and use that
            for (int i = 0; i < getInputInventory().getSlots(); i++) {
                ItemStack is = getInputInventory().getStackInSlot(i);
                if (!is.isEmpty()) {
                    CatalystInfo info = property.getCatalystGroup().getCatalystInfos().get(is);

                    if (info != null && (this.catalystInfo == null || this.catalystInfo.compareTo(info) > 0)) {
                        this.catalystInfo = info;
                    }

                }
            }

            // keep catalyst tier at NO_TIER unless info is found
            if (this.catalystInfo != null) {
                SusyLog.logger.info("3r390r9");
                this.requiredCatalystTier = property.getTier();
            }
        }
    }

    @Override
    protected void trySearchNewRecipe() {
        long maxVoltage = getMaxVoltage();
        Recipe currentRecipe;
        IItemHandlerModifiable importInventory = getInputInventory();
        IMultipleTankHandler importFluids = getInputTank();

        // see if the last recipe we used still works
        if (checkPreviousRecipe()) {
            currentRecipe = this.previousRecipe;
            // If there is no active recipe, then we need to find one.
        } else {
            currentRecipe = findRecipe(maxVoltage, importInventory, importFluids);
        }
        // If a recipe was found, then inputs were valid. Cache found recipe.
        if (currentRecipe != null) {
            this.previousRecipe = currentRecipe;
            tryFindCatalystInfo(currentRecipe);
        }

        this.invalidInputsForRecipes = (currentRecipe == null);

        // proceed if we have a usable recipe.
        if (currentRecipe != null && checkRecipe(currentRecipe)) {
            prepareRecipe(currentRecipe);
        }
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe) {
        CatalystPropertyValue property = recipe.getProperty(CatalystProperty.getInstance(), null);
        if (property == null || property.getTier() == CatalystInfo.NO_TIER) {
            return super.checkRecipe(recipe);
        }

        if (catalystInfo == null) {
            return false;
        }

        return catalystInfo.getTier() >= property.getTier() && super.checkRecipe(recipe);
    }

    @Override
    protected int[] runOverclockingLogic(@NotNull IRecipePropertyStorage propertyStorage, int recipeEUt,
                                         long maxVoltage, int duration, int amountOC) {
        if (requiredCatalystTier != CatalystInfo.NO_TIER && catalystInfo != null) {
            return SuSyOverclockingLogic.catalystOverclockingLogic(
                    recipeEUt,
                    maxVoltage,
                    duration,
                    amountOC,
                    catalystInfo,
                    requiredCatalystTier,
                    getOverclockingDurationDivisor(),
                    getOverclockingVoltageMultiplier());
        } else {
            return OverclockingLogic.standardOverclockingLogic(
                    recipeEUt,
                    maxVoltage,
                    duration,
                    amountOC,
                    this.getOverclockingDurationDivisor(),
                    this.getOverclockingVoltageMultiplier());
        }
    }

    @Override
    protected void modifyOverclockPre(int @NotNull [] values, @NotNull IRecipePropertyStorage storage) {
        super.modifyOverclockPre(values, storage);
        if (catalystInfo != null) {
            values[0] = Math.min(1, (int) (values[0] * catalystInfo.getEnergyEfficiency()));
            values[1] = Math.min(1, (int) (values[1] * catalystInfo.getSpeedEfficiency()));
        }
    }
}
