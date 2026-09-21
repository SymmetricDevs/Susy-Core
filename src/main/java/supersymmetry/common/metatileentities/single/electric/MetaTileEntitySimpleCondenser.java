package supersymmetry.common.metatileentities.single.electric;

import static gregtech.api.recipes.logic.OverclockingLogic.standardOverclockingLogic;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
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
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.util.GTUtility;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntitySimpleCondenser extends SimpleMachineMetaTileEntity {

    public MetaTileEntitySimpleCondenser(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.CONDENSER_RECIPES, SusyTextures.CONDENSER_OVERLAY, 1, true,
                GTUtility.largeTankSizeFunction);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySimpleCondenser(metaTileEntityId);
    }

    @Override
    protected RecipeLogicEnergy createWorkable(RecipeMap<?> recipeMap) {
        return new SimpleCondenserRecipeLogic(this, recipeMap, () -> energyContainer);
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
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        if (workable.getRecipeMap() != null && workable.getRecipeMap().getMaxFluidInputs() != 0) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity",
                    this.getTankScalingFunction().apply(getTier())));
            tooltip.add(I18n.format("susy.machine.simple_condenser.tooltip.info"));
        }
    }

    // TODO make this extend PrimitiveRecipeLogic in GT 2.9 (like that's ever happening)
    private static class SimpleCondenserRecipeLogic extends RecipeLogicEnergy {

        public SimpleCondenserRecipeLogic(MetaTileEntity metaTileEntity, RecipeMap<?> recipeMap,
                                          Supplier<IEnergyContainer> energyContainer) {
            super(metaTileEntity, recipeMap, energyContainer);
        }

        @NotNull @Override
        public MetaTileEntitySimpleCondenser getMetaTileEntity() {
            return (MetaTileEntitySimpleCondenser) super.getMetaTileEntity();
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
            return standardOverclockingLogic(1, getMaxVoltage(), recipeDuration, amountOC,
                    getOverclockingDurationDivisor(), getOverclockingVoltageMultiplier());
        }

        @Override
        public long getMaximumOverclockVoltage() {
            return GTValues.V[GTValues.LV];
        }

        @Override
        protected void updateRecipeProgress() {
            boolean isRegolith = true;
            for (int i = 0; i < 5; i++) {
                if (this.metaTileEntity.getWorld()
                        .getBlockState(this.metaTileEntity.getPos().offset(EnumFacing.DOWN, i + 1)).getBlock() !=
                        SuSyBlocks.REGOLITH) {
                    isRegolith = false;
                }
            }
            if (!isRegolith) {
                canRecipeProgress = false;
                decreaseProgress();
            } else {
                canRecipeProgress = true;
            }
            super.updateRecipeProgress();
        }
    }
}
