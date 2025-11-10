package supersymmetry.common.metatileentities.single.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.capability.impl.NotifiableFilteredFluidHandler;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.metatileentities.electric.MetaTileEntitySingleCombustion;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.capability.impl.SuSyFluidFilters;
import supersymmetry.api.util.SuSyUtility;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SuSyMetaTileEntitySingleCombustion extends MetaTileEntitySingleCombustion {

    private int workCounter;
    private boolean isFull;

    private SuSyUtility.Lubricant lubricant;
    private SuSyUtility.Coolant coolant;

    private boolean sufficientFluids;

    private FluidTank lubricantTank;
    private FluidTank coolantTank;

    private FluidTankList displayedTankList;

    public SuSyMetaTileEntitySingleCombustion(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                              ICubeRenderer renderer, int tier,
                                              Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, tankScalingFunction);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SuSyMetaTileEntitySingleCombustion(metaTileEntityId, recipeMap, renderer, this.getTier(), this.getTankScalingFunction());
    }

    @Override
    // Handle fluid imports
    protected FluidTankList createImportFluidHandler() {
        if (workable == null) return new FluidTankList(false);
        FluidTank[] fluidImports = new FluidTank[workable.getRecipeMap().getMaxFluidInputs() + 2];
        FluidTank[] displayedTanks = new FluidTank[workable.getRecipeMap().getMaxFluidInputs()];
        for (int i = 0; i < fluidImports.length - 2; i++) {
            NotifiableFluidTank filteredFluidHandler = new NotifiableFluidTank(
                    this.getTankScalingFunction().apply(this.getTier()), this, false);
            fluidImports[i] = filteredFluidHandler;
            displayedTanks[i] = filteredFluidHandler;
        }

        this.lubricantTank = new NotifiableFilteredFluidHandler(1000, this, false).setFilter(SuSyFluidFilters.LUBRICANT);
        fluidImports[fluidImports.length - 2] = lubricantTank;

        this.coolantTank = new NotifiableFilteredFluidHandler(1000, this, false).setFilter(SuSyFluidFilters.COOLANT);
        fluidImports[fluidImports.length - 1] = coolantTank;

        this.displayedTankList = new FluidTankList(false, displayedTanks);
        return new FluidTankList(false, fluidImports);
    }

    @Override
    // Override recipe logic
    protected CombustionRecipeLogic createWorkable(RecipeMap<?> recipeMap) {
        return new CombustionRecipeLogic(this, recipeMap, () -> this.energyContainer);
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            updateSufficientFluids();
            isFull = energyContainer.getEnergyStored() - energyContainer.getEnergyCapacity() == 0;

            if (workable.isWorking() && !isFull) workCounter += 1;
            if (workCounter == 600) {
                workCounter = 0;

                lubricantTank.drain((int) (lubricant.amount_required * Math.pow(4, getTier() - 1)), true);
                coolantTank.drain((int) (coolant.amount_required * Math.pow(4, getTier() - 1)), true);
            }
        }
    }

    protected void updateSufficientFluids() {
        // Check coolant & lubricant levels, activity
        FluidStack lubricantStack = lubricantTank.drain(Integer.MAX_VALUE, false);
        FluidStack coolantStack = coolantTank.drain(Integer.MAX_VALUE, false);

        lubricant = lubricantStack == null ? null : SuSyUtility.lubricants.get(lubricantStack.getFluid().getName());
        coolant = coolantStack == null ? null : SuSyUtility.coolants.get(coolantStack.getFluid().getName());

        if (lubricant == null || coolant == null) {
            sufficientFluids = false;
            return;
        }

        sufficientFluids = lubricantStack.amount >= lubricant.amount_required && coolantStack.amount >= coolant.amount_required;
    }

    @Override
    // Create GUI template for the combustion generator
    protected ModularUI.Builder createGuiTemplate(EntityPlayer player) {
        RecipeMap<?> workableRecipeMap = workable.getRecipeMap();
        int yOffset = 15;

        ModularUI.Builder builder;
        builder = workableRecipeMap.createUITemplateNoOutputs(workable::getProgressPercent, importItems,
                exportItems, displayedTankList, exportFluids, yOffset);
        builder.widget(new LabelWidget(6, 6, getMetaFullName()))
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, yOffset);
        builder.widget(new TankWidget(lubricantTank, 110, 21, 10, 54)
                .setBackgroundTexture(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(true)));
        builder.widget(new TankWidget(coolantTank, 124, 21, 10, 54)
                .setBackgroundTexture(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(true)));
        builder.widget(new ImageWidget(152, 63 + yOffset, 17, 17,
                GTValues.XMAS.get() ? GuiTextures.GREGTECH_LOGO_XMAS : GuiTextures.GREGTECH_LOGO)
                .setIgnoreColor(true));

        return builder;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("susy.machine.combustion_generator.tooltip"));
    }

    private class CombustionRecipeLogic extends FuelRecipeLogic {

        public CombustionRecipeLogic(SuSyMetaTileEntitySingleCombustion metaTileEntity, RecipeMap<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
            super(metaTileEntity, recipeMap, energyContainer);
        }

        @Override
        public boolean checkRecipe(@NotNull Recipe recipe) {
            return sufficientFluids;
        }

        @Override
        public boolean isWorking() {
            return sufficientFluids && super.isWorking();
        }

        @Override
        public int getMaxProgress() {
            int baseDuration = super.getMaxProgress();

            if (lubricant != null) {
                return (int) (baseDuration * lubricant.boost);
            }

            return baseDuration;
        }
    }
}
