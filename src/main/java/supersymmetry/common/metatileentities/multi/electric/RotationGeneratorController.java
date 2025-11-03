package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.metatileentity.multiblock.IProgressBarMultiblock;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.metatileentities.multi.electric.generator.LargeTurbineWorkableHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.IRotationSpeedHandler;
import supersymmetry.api.util.SuSyUtility;
import supersymmetry.common.materials.SusyMaterials;

import java.util.List;

public abstract class RotationGeneratorController extends FuelMultiblockController implements IRotationSpeedHandler {

    private int lubricantCounter = 0;
    private int speed = 0;

    protected int maxSpeed;
    protected int accel;
    protected int decel;

    private boolean sufficientFluids;
    private boolean isFull;

    protected FluidStack lubricantStack;
    protected SuSyUtility.Lubricant lubricantInfo;

    public RotationGeneratorController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int tier, int maxSpeed, int accel, int decel) {
        super(metaTileEntityId, recipeMap, tier);
        this.recipeMapWorkable = new MultiblockFuelRecipeLogic(this);
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

            if (recipeMapWorkable.isWorking()) {
                speed += getRotationAcceleration();
                lubricantCounter += speed;
            } else {
                speed -= getRotationDeceleration();
            }

            speed = Math.min(speed, maxSpeed);
            speed = Math.max(speed, 0);

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
        return data;
    }

    @Override
    // Retrieve temperature from NBT data
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.speed = data.getInteger("Speed");
        this.lubricantCounter = data.getInteger("LubricantCounter");
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
        if (!isFull) {
            return ((SuSyTurbineRecipeLogic) recipeMapWorkable).getMaxParallelVoltage();
        } else {
            return 0L;
        }
    }

    public class SuSyTurbineRecipeLogic extends MultiblockFuelRecipeLogic {

        private MetaTileEntitySUSYLargeTurbine tileEntity;
        private int proposedEUt;

        protected boolean voidEnergy = false;

        public SuSyTurbineRecipeLogic(MetaTileEntitySUSYLargeTurbine tileEntity) {
            super(tileEntity);
            this.tileEntity = tileEntity;
        }

        @Override
        public boolean checkRecipe(@NotNull Recipe recipe) {
            // Hack to get the recipeEUt early
            proposedEUt = recipe.getEUt();
            return sufficientFluids;
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
            long euToDraw = scaleProduction(recipeEUt); // Will be negative
            long resultEnergy = getEnergyStored() - euToDraw;
            if (resultEnergy >= 0L && resultEnergy <= getEnergyCapacity()) {
                if (!simulate) getEnergyContainer().changeEnergy(-euToDraw); // So this is positive
            }
            // Turbine voids excess fuel to keep spinning in any case.
            return voidEnergy;
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
            return Math.max(scaleProduction(((GTValues.V[tileEntity.getTier()]) * 16)), proposedEUt);
        }
    }
}
