package supersymmetry.common.metatileentities.single.electric;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleGeneratorMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.impl.SuSyFluidFilters;

import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.function.Function;

public class MetaTileEntityFuelCell extends SimpleGeneratorMetaTileEntity {

    private int currentTemperature;
    private int thresholdTemperature;
    private int maxTemperature;

    private FluidTank hotGasTank;

    public MetaTileEntityFuelCell(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                  ICubeRenderer renderer, int tier,
                                  Function<Integer, Integer> tankScalingFunction,
                                  int maxTemperature, int thresholdTemperature) {
        super(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction, false);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFuelCell(metaTileEntityId, workable.getRecipeMap(), renderer, getTier(),
            getTankScalingFunction(), maxTemperature, thresholdTemperature);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        if (workable == null) return new FluidTankList(false);
        FluidTank[] fluidImports = new FluidTank[workable.getRecipeMap().getMaxFluidInputs() + 1];
        for (int i = 0; i < fluidImports.length - 1; i++) {
            NotifiableFluidTank filteredFluidHandler = new NotifiableFluidTank(
                    this.getTankScalingFunction().apply(this.getTier()), this, false);
            fluidImports[i] = filteredFluidHandler;
        }

        this.hotGasTank = new NotifiableFilteredFluidHandler(100, this, false).setFilter(SuSyFluidFilters.HOT_GAS);
        fluidImports[fluidImports.length - 1] = hotGasTank;

        return new FluidTankList(false, fluidImports);  
    }

    @Override
    protected FuelCellRecipeLogic createWorkable(RecipeMap<?> recipeMap) {
        return new FuelCellRecipeLogic(this, recipeMap, energyContainer);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            updateTemperature();
            if (getOffsetTimer() % 20 == 0 && currentTemperature >= thresholdTemperature && workable.isActive()) {

            }
        }
    }

    private void updateTemperature() {
        if (getOffsetTimer() % 20 == 0 && currentTemperature < thresholdTemperature) {
            FluidStack hotGasStack = hotGasTank.drain(5, false);
            if (hotGasStack != null && hotGasStack.amount == 5) {
                hotGasTank.drain(5, true);
                currentTemperature += 2;
            } else if (currentTemperature > 0){
                currentTemperature -= 1;
            }
        } else {
            if (workable.isWorking()) {
                currentTemperature += 2;
            } else {
                currentTemperature -= 1;
            }
        }
    }

    @Override
    protected ModularUI.Builder createGuiTemplate(EntityPlayer player) {
        RecipeMap<?> workableRecipeMap = workable.getRecipeMap();
        int yOffset = 0;

        FluidTankList recipeImportFluids = new FluidTankList(false, importFluids.getFluidTanks().subList(0, workable.getRecipeMap().getMaxFluidInputs()).toArray(new FluidTank[0]));

        ModularUI.Builder builder;
        builder = workableRecipeMap.createUITemplateNoOutputs(workable::getProgressPercent, importItems,
                exportItems, recipeImportFluids, exportFluids, yOffset);
        builder.widget(new LabelWidget(6, 6, getMetaFullName()))
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, yOffset);

        return builder;
    }

    public int getTemperature() {
        return currentTemperature;
    }

    private class FuelCellRecipeLogic extends RecipeLogicEnergy {

        public FuelCellRecipeLogic(MetaTileEntityFuelCell metaTileEntity, RecipeMap<?> recipeMap, IEnergyContainer energyContainer) {
            super(metaTileEntity, recipeMap, () -> energyContainer);
        }

        @Override
        public boolean checkRecipe(@NotNull Recipe recipe) {
            return currentTemperature >= thresholdTemperature;
        }
    }
}
