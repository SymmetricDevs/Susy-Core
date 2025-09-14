package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockFuelRecipeLogic;
import gregtech.api.metatileentity.multiblock.FuelMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.TextComponentUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.IRotationSpeedHandler;
import supersymmetry.api.util.SuSyUtility;
import supersymmetry.common.materials.SusyMaterials;

import java.util.List;

public abstract class RotationGeneratorController extends FuelMultiblockController implements IRotationSpeedHandler {

    private int speed = 0;
    protected int maxSpeed;
    protected int accel;
    protected int decel;
    private boolean sufficientFluids;
    protected FluidStack lubricantStack;
    private SuSyUtility.Lubricant lubricantInfo;

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

            if (recipeMapWorkable.isWorking()) {
                tanks.drain(new FluidStack(lubricantStack.getFluid(), lubricantInfo.amount_required), true);
                speed += getRotationAcceleration();
            } else {
                speed -= getRotationDeceleration();
            }

            speed = Math.min(speed, maxSpeed);
            speed = Math.max(speed, 0);
        }
    }

    protected void updateSufficientFluids() {
        // Check lubricant levels
        if (lubricantStack == null) {
            sufficientFluids = false;
            return;
        }

        lubricantInfo = lubricantStack == null ? null : SuSyUtility.lubricants.get(lubricantStack.getFluid().getName());
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
        return data;
    }

    @Override
    // Retrieve temperature from NBT data
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.speed = data.getInteger("Speed");
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

    public class SuSyTurbineRecipeLogic extends MultiblockFuelRecipeLogic {

        public SuSyTurbineRecipeLogic(MetaTileEntitySUSYLargeTurbine tileEntity) {
            super(tileEntity);
        }

        public FluidStack getInputFluidStack() {
            // Previous Recipe is always null on first world load, so try to acquire a new recipe
            if (previousRecipe == null) {
                Recipe recipe = findRecipe(Integer.MAX_VALUE, getInputInventory(), getInputTank());

                return recipe == null ? null : getInputTank().drain(new FluidStack(recipe.getFluidInputs().get(0).getInputFluidStack().getFluid(), Integer.MAX_VALUE), false);
            }
            FluidStack fuelStack = previousRecipe.getFluidInputs().get(0).getInputFluidStack();
            return getInputTank().drain(new FluidStack(fuelStack.getFluid(), Integer.MAX_VALUE), false);
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

            if (lubricantStack != null) {
                return (int) (baseDuration * lubricantInfo.boost);
            }

            return baseDuration;
        }
    }
}
