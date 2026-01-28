package supersymmetry.common.metatileentities.multi.electric;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.TextComponentUtil;
import supersymmetry.api.capability.IRotationSpeedHandler;
import supersymmetry.api.util.SuSyUtility;
import supersymmetry.common.materials.SusyMaterials;

public abstract class RotationGeneratorController extends FuelMultiblockController
                                                  implements IRotationSpeedHandler, ITieredMetaTileEntity {

    public final int tier;

    private int lubricantCounter = 0;
    private int speed = 0;

    protected int maxSpeed;
    protected int accel;
    protected int decel;

    private boolean sufficientFluids;
    private boolean isFull;

    protected FluidStack lubricantStack;
    protected SuSyUtility.Lubricant lubricantInfo;

    public RotationGeneratorController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier,
                                       int maxSpeed, int accel, int decel) {
        super(metaTileEntityId, recipeMap, tier);
        this.tier = tier;
        this.recipeMapWorkable = new SuSyTurbineRecipeLogic(this);
        this.recipeMapWorkable.setMaximumOverclockVoltage(GTValues.V[tier]);
        this.maxSpeed = maxSpeed;
        this.accel = accel;
        this.decel = decel;
    }

    @Override
    public int getRotationSpeed() {
        return this.speed;
    }

    @Override
    public int getMaxRotationSpeed() {
        return this.maxSpeed;
    }

    @Override
    public int getRotationAcceleration() {
        return this.accel;
    }

    @Override
    public int getRotationDeceleration() {
        return this.decel;
    }

    @Override
    public void update() {
        IMultipleTankHandler tanks = getInputFluidInventory();

        super.update();
        if (!getWorld().isRemote) {
            setLubricantStack(tanks);
            updateSufficientFluids();
            isFull = energyContainer.getEnergyStored() - energyContainer.getEnergyCapacity() == 0;

            if (recipeMapWorkable.isWorking() && ((SuSyTurbineRecipeLogic) recipeMapWorkable).tryDrawEnergy()) {
                speed += getRotationAcceleration();
                lubricantCounter += speed;
            } else {
                speed -= getRotationDeceleration();
            }

            speed = Math.min(speed, maxSpeed);
            speed = Math.max(speed, 0);

            ((SuSyTurbineRecipeLogic) recipeMapWorkable).doDrawEnergy();

            if (lubricantStack != null && lubricantCounter >= (600 * 3600)) {
                lubricantCounter = 0;
                tanks.drain(new FluidStack(lubricantStack.getFluid(), lubricantInfo.amount_required), true);
            }
        }
    }

    protected void updateSufficientFluids() {
        // Check lubricant levels
        if (lubricantStack == null) {
            sufficientFluids = false;
            return;
        }

        lubricantInfo = SuSyUtility.lubricants.get(lubricantStack.getFluid().getName());
        sufficientFluids = lubricantStack.amount >= lubricantInfo.amount_required;
    }

    private static final Material[] POSSIBLE_LUBRICANTS = {
            SusyMaterials.SupremeLubricant,
            SusyMaterials.PremiumLubricant,
            SusyMaterials.MidgradeLubricant,
            Materials.Lubricant,
            SusyMaterials.LubricatingOil
    };

    protected void setLubricantStack(IMultipleTankHandler tanks) {
        for (Material lubricant : POSSIBLE_LUBRICANTS) {
            FluidStack lubricantStack = tanks.drain(lubricant.getFluid(Integer.MAX_VALUE), false);
            if (lubricantStack != null) {
                this.lubricantStack = lubricantStack;
                return;
            }
        }

        this.lubricantStack = null;
    }

    @Override
    // Save temperature to NBT data
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("Speed", this.speed);
        data.setInteger("LubricantCounter", this.lubricantCounter);
        data.setBoolean("VoidingEnergy", ((SuSyTurbineRecipeLogic) recipeMapWorkable).getVoidingEnergy());
        return data;
    }

    @Override
    // Retrieve temperature from NBT data
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.speed = data.getInteger("Speed");
        this.lubricantCounter = data.getInteger("LubricantCounter");
        ((SuSyTurbineRecipeLogic) recipeMapWorkable).setVoidingEnergy(data.getBoolean("VoidingEnergy"));
    }

    @Override
    protected void addErrorText(List<ITextComponent> textList) {
        super.addErrorText(textList);
        if (isStructureFormed()) {
            if (lubricantStack == null || lubricantStack.amount == 0) {
                textList.add(TextComponentUtil.translationWithColor(TextFormatting.RED,
                        "gregtech.multiblock.large_combustion_engine.no_lubricant"));
            }
        }
    }

    @Override
    protected long getMaxVoltage() {
        if (!isFull && speed > 0) {
            return ((SuSyTurbineRecipeLogic) recipeMapWorkable).getActualVoltage();
        } else {
            return 0L;
        }
    }

    public class SuSyTurbineRecipeLogic extends MultiblockFuelRecipeLogic {

        private RotationGeneratorController tileEntity;
        private int proposedEUt;

        protected boolean voidEnergy = false;

        public SuSyTurbineRecipeLogic(RotationGeneratorController tileEntity) {
            super(tileEntity);
            this.tileEntity = tileEntity;
        }

        @Override
        public boolean checkRecipe(@NotNull Recipe recipe) {
            // Hack to get the recipeEUt early
            proposedEUt = recipe.getEUt();
            if (proposedEUt > getMaximumAllowedVoltage()) {
                return false;
            }
            return sufficientFluids;
        }

        @Override
        protected void updateRecipeProgress() {
            if (canRecipeProgress && drawEnergy(recipeEUt, true)) {
                // as recipe starts with progress on 1 this has to be > only not => to compensate for it
                if (++progressTime > maxProgressTime) {
                    completeRecipe();
                }
            }
        }

        public boolean getVoidingEnergy() {
            return this.voidEnergy;
        }

        protected void setVoidingEnergy(boolean mode) {
            this.voidEnergy = mode;
        }

        @Override
        public boolean isWorking() {
            return sufficientFluids && super.isWorking();
        }

        @Override
        protected boolean drawEnergy(int recipeEUt, boolean simulate) {
            long euToDraw = -getActualVoltage(); // Will be negative
            long resultEnergy = getEnergyStored() - euToDraw;
            if (resultEnergy >= 0L && resultEnergy <= getEnergyCapacity()) {
                if (!simulate) getEnergyContainer().changeEnergy(-euToDraw); // So this is positive
                return true;
            }
            // Turbine voids excess fuel to keep spinning in any case.
            return voidEnergy;
        }

        public boolean tryDrawEnergy() {
            return drawEnergy(proposedEUt, true); // replace recipeEUt with proposedEUt since if the recipe cannot be
                                                  // run but speed > 0, recipeEUt gets set to 0 but proposedEUt isn't
        }

        public boolean doDrawEnergy() {
            return drawEnergy(proposedEUt, false);
        }

        @Override
        public int getMaxProgress() {
            int baseDuration = super.getMaxProgress();
            if (lubricantStack != null) {
                return (int) (baseDuration * lubricantInfo.boost);
            }

            return baseDuration;
        }

        protected long scaleProduction(long production) {
            return (long) (production * speed / (double) maxSpeed);
        }

        @Override
        protected long getMaxParallelVoltage() {
            long maximumOutput = getMaximumAllowedVoltage();
            return Math.max(scaleProduction(maximumOutput),
                    Math.min(proposedEUt, maximumOutput));
        }

        public long getMaximumAllowedVoltage() {
            return Math.min((GTValues.V[tileEntity.getTier()]) * 16, getMaxVoltage());
        }

        protected long getActualVoltage() {
            return scaleProduction(-proposedEUt);
        }

        public int getCurrentParallel() {
            return this.parallelRecipesPerformed;
        }
    }
}
