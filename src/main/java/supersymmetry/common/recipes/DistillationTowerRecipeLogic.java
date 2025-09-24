package supersymmetry.common.recipes;

import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.GTTransferUtils;
import supersymmetry.api.metatileentity.multiblock.MetaTileEntityOrderedDT;

public class DistillationTowerRecipeLogic extends MultiblockRecipeLogic {

    public DistillationTowerRecipeLogic(RecipeMapMultiblockController tileEntity) {
        super(tileEntity);
    }

    public DistillationTowerRecipeLogic(RecipeMapMultiblockController tileEntity, boolean hasPerfectOC) {
        super(tileEntity, hasPerfectOC);
    }

    @Override
    protected void outputRecipeOutputs() {
        GTTransferUtils.addItemsToItemHandler(getOutputInventory(), false, itemOutputs);
        if (metaTileEntity instanceof MetaTileEntityOrderedDT tower)
            tower.getHandler().applyFluidToOutputs(fluidOutputs, true);
    }

    @Override
    protected boolean setupAndConsumeRecipeInputs(@NotNull Recipe recipe,
                                                  @NotNull IItemHandlerModifiable importInventory,
                                                  @NotNull IMultipleTankHandler importFluids) {
        this.overclockResults = calculateOverclock(recipe);

        modifyOverclockPost(overclockResults, recipe.getRecipePropertyStorage());

        if (!hasEnoughPower(overclockResults)) {
            return false;
        }

        IItemHandlerModifiable exportInventory = getOutputInventory();

        // We have already trimmed outputs and chanced outputs at this time
        // Attempt to merge all outputs + chanced outputs into the output bus, to prevent voiding chanced outputs
        if (!metaTileEntity.canVoidRecipeItemOutputs() &&
                !GTTransferUtils.addItemsToItemHandler(exportInventory, true, recipe.getAllItemOutputs())) {
            this.isOutputsFull = true;
            return false;
        }

        if (metaTileEntity instanceof MetaTileEntityOrderedDT tower) {
            // We have already trimmed fluid outputs at this time
            if (!metaTileEntity.canVoidRecipeFluidOutputs() &&
                    !tower.getHandler().applyFluidToOutputs(recipe.getAllFluidOutputs(), false)) {
                this.isOutputsFull = true;
                return false;
            }
        }

        this.isOutputsFull = false;
        if (recipe.matches(true, importInventory, importFluids)) {
            this.metaTileEntity.addNotifiedInput(importInventory);
            return true;
        }
        return false;
    }

    @Override
    protected IMultipleTankHandler getOutputTank() {
        if (metaTileEntity instanceof MetaTileEntityOrderedDT tower)
            return tower.getHandler().getFluidTanks();
        return super.getOutputTank();
    }
}
