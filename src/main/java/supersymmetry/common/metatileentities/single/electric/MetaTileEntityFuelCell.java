package supersymmetry.common.metatileentities.single.electric;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleGeneratorMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.SusyLog;
import supersymmetry.api.capability.impl.SuSyFluidFilters;

import net.minecraft.util.ResourceLocation;
import supersymmetry.api.gui.SusyGuiTextures;

import java.util.List;
import java.util.function.Function;

public class MetaTileEntityFuelCell extends SimpleGeneratorMetaTileEntity {

    private int currentTemperature = 25;
    private int thresholdTemperature;
    private int maxTemperature;

    private FluidTank hotGasTank;

    // Initialization
    public MetaTileEntityFuelCell(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                  ICubeRenderer renderer, int tier,
                                  Function<Integer, Integer> tankScalingFunction,
                                  int maxTemperature, int thresholdTemperature) {
        super(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction, false);
        this.thresholdTemperature = thresholdTemperature;
        this.maxTemperature = maxTemperature;
    }

    @Override
    // MetaTileEntity creation
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFuelCell(metaTileEntityId, workable.getRecipeMap(), renderer, getTier(),
            getTankScalingFunction(), maxTemperature, thresholdTemperature);
    }

    @Override
    // Handle fluid imports
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
    // Override recipe logic
    protected FuelCellRecipeLogic createWorkable(RecipeMap<?> recipeMap) {
        return new FuelCellRecipeLogic(this, recipeMap, energyContainer);
    }

    @Override
    // Save temperature to NBT data
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("CurrentTemperature", currentTemperature);
        return data;
    }

    @Override
    // Retrieve temperature from NBT data
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.currentTemperature = data.getInteger("CurrentTemperature");
    }

    @Override
    // Add operating temperature information to tooltip
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("susy.machine.fuel_cell.tooltip", thresholdTemperature));
    }

    @Override
    // Update working state based on temperature
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            updateTemperature();
            if (getOffsetTimer() % 20 == 0 ) {
                if (currentTemperature >= thresholdTemperature)
                    if (!workable.isActive()) workable.setWorkingEnabled(true);
                if (currentTemperature <= thresholdTemperature)
                    workable.setWorkingEnabled(false);
            }
        }
    }

    // Temperature logic handling based on recipe activity and hot gas preheating
    private void updateTemperature() {
        if (getOffsetTimer() % 20 == 0) {
            if (currentTemperature < thresholdTemperature) {
                FluidStack hotGasStack = hotGasTank.drain(5, false);
                if (hotGasStack != null && hotGasStack.amount == 5) {
                    hotGasTank.drain(5, true);
                    currentTemperature += 2;
                } else {
                    if (currentTemperature > 25) {
                        currentTemperature -= 1;
                    }
                }
            } else {
                if (workable.isWorking()) {
                    currentTemperature += 2;
                } else {
                    if (currentTemperature > 25) {
                        currentTemperature -= 1;
                    }
                }
            }
        }
    }

    @Override
    // Create GUI template for the fuel cell
    protected ModularUI.Builder createGuiTemplate(EntityPlayer player) {
        RecipeMap<?> workableRecipeMap = workable.getRecipeMap();
        int yOffset = 15;
        int thresholdYOffset = (int) ((1 - (thresholdTemperature / (maxTemperature * 1.0))) * 54) + 19;

        ModularUI.Builder builder;
        builder = workableRecipeMap.createUITemplateNoOutputs(workable::getProgressPercent, importItems,
                exportItems, importFluids, exportFluids, yOffset);
        builder.widget(new LabelWidget(6, 6, getMetaFullName()))
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, yOffset);
        builder.widget(new ProgressWidget(this::getTemperaturePercent, 124, 21, 10, 54)
                .setProgressBar(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(true),
                        GuiTextures.PROGRESS_BAR_BOILER_HEAT,
                        ProgressWidget.MoveType.VERTICAL)
                .setHoverTextConsumer(list -> list.add(new TextComponentTranslation(I18n.format("susy.gui.temperature_celcius", currentTemperature, maxTemperature))))
        );
        builder.widget(new ImageWidget(134, thresholdYOffset, 6, 5, SusyGuiTextures.ARROW));
        builder.widget(new TankWidget(hotGasTank, 110, 21, 10, 54)
                .setBackgroundTexture(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(true)));

        return builder;
    }

    public int getTemperature() {
        return currentTemperature;
    }

    public double getTemperaturePercent() {
        return (currentTemperature - 25) / ((maxTemperature - 25) * 1.0);
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
