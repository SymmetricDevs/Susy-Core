package supersymmetry.common.metatileentities.single.electric;

import static gregtech.api.recipes.logic.OverclockingLogic.standardOverclockingLogic;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import supersymmetry.api.metatileentity.multiblock.ICryogenicProvider;
import supersymmetry.api.metatileentity.multiblock.ICryogenicReceiver;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.properties.CryogenicEnvironmentProperty;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityBathCondenser extends SimpleMachineMetaTileEntity implements ICryogenicReceiver {

    private @Nullable ICryogenicProvider provider;

    public MetaTileEntityBathCondenser(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.BATH_CONDENSER, SusyTextures.BATH_CONDENSER_OVERLAY, 1, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBathCondenser(metaTileEntityId);
    }

    @Override
    protected RecipeLogicEnergy createWorkable(RecipeMap<?> recipeMap) {
        return new BathCondenserRecipeLogic(this, recipeMap, () -> energyContainer);
    }

    @Override
    protected void reinitializeEnergyContainer() {
        this.energyContainer = new EnergyContainerHandler(this, 0, 0, 0, 0, 0) {

            @Override
            public boolean isOneProbeHidden() {
                return true;
            }
        };
    }

    @Override
    public @Nullable ICryogenicProvider getCryogenicProvider() {
        return provider;
    }

    @Override
    public void setCryogenicProvider(@Nullable ICryogenicProvider cryogenicProvider) {
        this.provider = cryogenicProvider;
        if (this.provider == null && this.workable instanceof BathCondenserRecipeLogic logic) {
            logic.invalidate();
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        if (workable.getRecipeMap() != null && workable.getRecipeMap().getMaxFluidInputs() != 0) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity",
                    this.getTankScalingFunction().apply(getTier())));
        }
    }

    // TODO make this extend PrimitiveRecipeLogic in GT 2.9
    private static class BathCondenserRecipeLogic extends RecipeLogicEnergy {

        public BathCondenserRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap,
                                        Supplier<IEnergyContainer> energyContainer) {
            super(tileEntity, recipeMap, energyContainer);
        }

        @NotNull
        @Override
        public MetaTileEntityBathCondenser getMetaTileEntity() {
            return (MetaTileEntityBathCondenser) super.getMetaTileEntity();
        }

        @Override
        public boolean checkRecipe(@NotNull Recipe recipe) {
            if (super.checkRecipe(recipe)) {
                Boolean value = recipe.getProperty(CryogenicEnvironmentProperty.getInstance(), null);
                return value == null || !value || (getMetaTileEntity().getCryogenicProvider() != null &&
                        getMetaTileEntity().getCryogenicProvider().isStructureFormed());
            }

            return false;
        }

        @Override
        protected boolean canProgressRecipe() {
            if (super.canProgressRecipe()) {
                if (previousRecipe == null) return true;
                Boolean value = previousRecipe.getProperty(CryogenicEnvironmentProperty.getInstance(), null);
                return value == null || !value || (getMetaTileEntity().getCryogenicProvider() != null &&
                        getMetaTileEntity().getCryogenicProvider().isStructureFormed());
            }
            return false;
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
            return true; // spoof energy being drawn
        }

        @Override
        public long getMaxVoltage() {
            return GTValues.LV;
        }

        @Override
        protected int @NotNull [] runOverclockingLogic(@NotNull IRecipePropertyStorage propertyStorage, int recipeEUt,
                                                       long maxVoltage, int recipeDuration, int amountOC) {
            return standardOverclockingLogic(
                    1,
                    getMaxVoltage(),
                    recipeDuration,
                    amountOC,
                    getOverclockingDurationDivisor(),
                    getOverclockingVoltageMultiplier()

            );
        }

        @Override
        public long getMaximumOverclockVoltage() {
            return GTValues.V[GTValues.LV];
        }

        /**
         * Used to reset cached values in the Recipe Logic on structure deform
         */
        public void invalidate() {
            previousRecipe = null;
            progressTime = 0;
            maxProgressTime = 0;
            recipeEUt = 0;
            fluidOutputs = null;
            itemOutputs = null;
            setActive(false); // this marks dirty for us
        }
    }
}
