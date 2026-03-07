package supersymmetry.common.metatileentities.multi.electric;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;

public class MetaTileEntityGasTurbine extends MetaTileEntitySUSYLargeTurbine {

    public MetaTileEntityGasTurbine(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier, int maxSpeed,
                                    int accel, int decel, IBlockState casingState, IBlockState rotorState,
                                    ICubeRenderer casingRenderer, ICubeRenderer frontOverlay) {
        super(metaTileEntityId, recipeMap, tier, maxSpeed, accel, decel, casingState, rotorState, casingRenderer,
                frontOverlay);
        this.recipeMapWorkable = new GasTurbineRecipeLogic(this);
        this.recipeMapWorkable.setMaximumOverclockVoltage(GTValues.V[tier]);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityGasTurbine(metaTileEntityId, recipeMap, tier, maxSpeed, accel, decel, casingState,
                rotorState, casingRenderer, frontOverlay);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        // Different characters use common constraints. Copied from GCyM
        TraceabilityPredicate casingPredicate = states(this.casingState).setMinGlobalLimited(51)
                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));
        TraceabilityPredicate maintenance = abilities(MultiblockAbility.MAINTENANCE_HATCH).setMaxGlobalLimited(1);

        return FactoryBlockPattern.start()
                .aisle("GAAAAAAAO", "GAAAAAAAO", "G   A   O")
                .aisle("GAAAAAAAO", "IDDDDCCCF", "GAAAAAAAO")
                .aisle("GAAAAAAAO", "GSAAAAAAO", "G   A   O")
                .where('S', selfPredicate())
                .where('A', casingPredicate
                        .or(autoAbilities(false, false, false, false, false, false, false))
                        .or(maintenance))
                .where('O', casingPredicate
                        .or(autoAbilities(false, false, false, false, false, true, false))
                        .or(maintenance))
                .where('C', coilOrientation())
                .where('D', rotorOrientation())
                .where('F', abilities(MultiblockAbility.OUTPUT_ENERGY))
                .where('G', casingPredicate
                        .or(autoAbilities(false, false, false, false, true, false, false))
                        .or(maintenance))
                .where('I',
                        states(MetaBlocks.MULTIBLOCK_CASING
                                .getState(BlockMultiblockCasing.MultiblockCasingType.ENGINE_INTAKE_CASING)))
                .where(' ', any())
                .build();
    }

    public class GasTurbineRecipeLogic extends SuSyTurbineRecipeLogic {

        public GasTurbineRecipeLogic(MetaTileEntityGasTurbine tileEntity) {
            super(tileEntity);
        }

        @Override
        protected void outputRecipeOutputs() {
            // The super call just outputs items + fluids. Turbines have no item output,
            // and for Flue we have to only output the remainder.
            int remainder = getMaxProgress() % 20;
            int remainingFlue = (int) (fluePerSecond() * (remainder / 20.0));
            outputFlue(remainingFlue);
        }

        @Override
        protected void updateRecipeProgress() {
            if (canRecipeProgress && drawEnergy(recipeEUt, true)) {
                // as recipe starts with progress on 1 this has to be > only not => to compensate for it
                if (++progressTime > getMaxProgress()) {
                    completeRecipe();
                    return;
                }
                if (progressTime % 20 == 0) {
                    outputFlue(fluePerSecond());
                }
            }
        }

        private int fluePerSecond() {
            if (fluidOutputs.isEmpty()) {
                return 0;
            }
            return 20 * fluidOutputs.getFirst().amount / getMaxProgress();
        }

        private void outputFlue(int amount) {
            if (amount <= 0) {
                return;
            }
            FluidStack flueStack = fluidOutputs.getFirst().copy();
            flueStack.amount = amount;
            getOutputTank().fill(flueStack, true);
        }
    }
}
