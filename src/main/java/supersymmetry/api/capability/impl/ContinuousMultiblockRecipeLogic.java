package supersymmetry.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.util.GTUtility;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.builders.logic.SuSyOverclockingLogic;
import supersymmetry.api.recipes.catalysts.CatalystInfo;
import supersymmetry.api.recipes.properties.CatalystProperty;
import supersymmetry.api.recipes.properties.CatalystPropertyValue;

import javax.annotation.Nonnull;

import static gregtech.api.GTValues.ULV;

public class ContinuousMultiblockRecipeLogic extends MultiblockRecipeLogic {
    private CatalystInfo catalystInfo;
    private int requiredCatalystTier;

    public ContinuousMultiblockRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity);
    }

    public ContinuousMultiblockRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
        super(tileEntity, hasPerfectOC);
    }

    // This will be made part of the MTE eventually.
    // TODO: Make this part of a new MTE for catalysts. Make the continuous machines extend that MTE.
    public void tryFindCatalystInfo(@Nonnull Recipe recipe) {
        this.catalystInfo = null;
        this.requiredCatalystTier = CatalystInfo.NO_TIER;

        if (recipe.hasProperty(CatalystProperty.getInstance())) {
            CatalystPropertyValue property = recipe.getProperty(CatalystProperty.getInstance(), null);

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

                    if (info != null) {
                        if (this.catalystInfo == null) {
                            this.catalystInfo = info;
                        } else if (this.catalystInfo.compareTo(info) > 0) {
                            this.catalystInfo = info;
                        }
                    }
                }
            }

            // keep catalyst tier at NO_TIER unless info is found
            if (this.catalystInfo != null) {
                this.requiredCatalystTier = property.getTier();
            }
        }
    }

    @Override
    protected boolean prepareRecipe(Recipe recipe) {
        recipe = Recipe.trimRecipeOutputs(recipe, this.getRecipeMap(), this.metaTileEntity.getItemOutputLimit(), this.metaTileEntity.getFluidOutputLimit());

        calculateOverclockLimit(recipe);
        recipe = findParallelRecipe(
                this,
                recipe,
                getInputInventory(),
                getInputTank(),
                getOutputInventory(),
                getOutputTank(),
                getMaxParallelVoltage(),
                getParallelLimit());

        if (recipe != null && this.setupAndConsumeRecipeInputs(recipe, this.getInputInventory())) {
            this.setupRecipe(recipe);
            return true;
        }

        return false;
    }

    @Override
    protected void trySearchNewRecipeCombined() {
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
    protected int[] runOverclockingLogic(@NotNull IRecipePropertyStorage propertyStorage, int recipeEUt, long maxVoltage, int duration, int amountOC) {
        // apply maintenance penalties
        Tuple<Integer, Double> maintenanceValues = getMaintenanceValues();

        double[] overclock = null;
        if (maintenanceValues.getSecond() != 1.0)

            overclock = runContinuousOverclockingLogic(
                    Math.abs(recipeEUt),
                    maxVoltage,
                    (int) Math.round(duration * maintenanceValues.getSecond()),
                    amountOC
            );

        if (overclock == null)
            overclock = runContinuousOverclockingLogic(
                    Math.abs(recipeEUt),
                    maxVoltage,
                    duration,
                    amountOC
            );

        if (maintenanceValues.getFirst() > 0)
            overclock[1] = (overclock[1] * (1 + 0.1 * maintenanceValues.getFirst()));

        return new int[] {(int) overclock[0], overclock[1] <= 1 ? 1 : (int) overclock[1]};
    }

    protected double[] runContinuousOverclockingLogic(int recipeEUt, long maxVoltage, int duration, int amountOC) {
        if (requiredCatalystTier != CatalystInfo.NO_TIER && catalystInfo != null) {
            return SuSyOverclockingLogic.continuousCatalystOverclockingLogic(
                    recipeEUt,
                    maxVoltage,
                    duration,
                    amountOC,
                    catalystInfo,
                    requiredCatalystTier,
                    getOverclockingDurationDivisor(),
                    getOverclockingVoltageMultiplier()
            );
        } else {
            return SuSyOverclockingLogic.continuousOverclockingLogic(
                    recipeEUt,
                    maxVoltage,
                    duration,
                    amountOC,
                    this.getOverclockingDurationDivisor(),
                    this.getOverclockingVoltageMultiplier()
            );
        }
    }

    @Override
    protected int[] performOverclocking(@Nonnull Recipe recipe) {
        int recipeTier = GTUtility.getTierByVoltage(recipe.getEUt());
        int maximumTier = getOverclockForTier(getMaximumOverclockVoltage());

        // The maximum number of overclocks is determined by the difference between the tier the recipe is running at,
        // and the maximum tier that the machine can overclock to.
        int numberOfOCs = maximumTier - recipeTier;
        if (recipeTier == ULV) numberOfOCs--; // no ULV overclocking

        // Usually this would cancel overclocking here if num of OCs is 0 or less.
        // Since we need to take catalyst overclocks into account, we have to continue even if numberOfOCs is 0 or less.


        // This will perform the actual overclocking
        return runOverclockingLogic(recipe.getRecipePropertyStorage(), recipe.getEUt(), getMaximumOverclockVoltage(), recipe.getDuration(), numberOfOCs);
    }

    protected void calculateOverclockLimit(Recipe recipe) {
        int recipeTier = GTUtility.getTierByVoltage(recipe.getEUt());
        int maximumTier = getOverclockForTier(getMaximumOverclockVoltage());

        // The maximum number of overclocks is determined by the difference between the tier the recipe is running at,
        // and the maximum tier that the machine can overclock to.
        int numberOfOCs = maximumTier - recipeTier;
        if (recipeTier == ULV) numberOfOCs--; // no ULV overclocking

        double parallelLimitDouble = 1 / runContinuousOverclockingLogic(recipe.getEUt(), getMaximumOverclockVoltage(), recipe.getDuration(), numberOfOCs)[1];

        setParallelLimit(parallelLimitDouble <= 1 ? 1 : (int) parallelLimitDouble);
    }

    protected boolean checkCanOverclock(int recipeEUt) {
        if (!isAllowOverclocking()) return false;

        // Check if the voltage to run at is higher than the recipe, and that it is not ULV tier

        // The maximum tier that the machine can overclock to
        int overclockTier = getOverclockForTier(getMaximumOverclockVoltage());
        // If the maximum tier that the machine can overclock to is ULV, return false.
        // There is no overclocking allowed in ULV
        // TODO apply catalyst info bonuses in the dedicated pre-oc phase in a future CEu Update
        if (overclockTier < GTValues.LV) return false;
        int recipeTier = GTUtility.getTierByVoltage(recipeEUt);

        // Do overclock if the overclock tier is greater than the recipe tier or the catalyst tier is higher than the recipe catalyst tier
        return overclockTier > recipeTier ||
                (catalystInfo != null && catalystInfo.getTier() > requiredCatalystTier);
    }

}
