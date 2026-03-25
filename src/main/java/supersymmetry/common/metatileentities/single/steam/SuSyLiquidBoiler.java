package supersymmetry.common.metatileentities.single.steam;

import java.util.Collections;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import gregtech.api.GTValues;
import gregtech.api.capability.IFilter;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.steam.boiler.SteamBoiler;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import supersymmetry.api.capability.impl.SuSyBoilerLogic;

public class SuSyLiquidBoiler extends SteamBoiler {

    private static final Object2IntMap<Fluid> BOILER_FUEL_TO_CONSUMPTION = new Object2IntOpenHashMap<>();
    private static boolean initialized;

    private static final IFilter<FluidStack> FUEL_FILTER = new IFilter<>() {

        @Override
        public boolean test(@NotNull FluidStack fluidStack) {
            return !fluidStack.getFluid().isGaseous() && SuSyBoilerLogic.BOILER_FUEL.test(fluidStack);
        }

        @Override
        public int getPriority() {
            return IFilter.whitelistPriority(RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.getRecipeList().size());
        }
    };

    private FluidTank fuelFluidTank;

    public SuSyLiquidBoiler(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, isHighPressure, Textures.LAVA_BOILER_OVERLAY);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SuSyLiquidBoiler(metaTileEntityId, isHighPressure);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        FluidTankList superHandler = super.createImportFluidHandler();
        this.fuelFluidTank = new FilteredFluidHandler(16000).setFilter(FUEL_FILTER);
        return new FluidTankList(false, superHandler, fuelFluidTank);
    }

    @Override
    protected void tryConsumeNewFuel() {
        FluidStack fluid = fuelFluidTank.getFluid();
        if (fluid == null || fluid.tag != null) { // fluid with nbt tag cannot match normal fluids
            return;
        }
        Recipe fluidFuelRecipe = RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.findRecipe(
                GTValues.V[GTValues.MAX], NonNullList.create(), Collections.singletonList(fluid));
        if (fluidFuelRecipe == null) {
            return;
        }
        int consumption = fluidFuelRecipe.getFluidInputs().get(0).getInputFluidStack().amount;
        if (consumption > 0 && fuelFluidTank.getFluidAmount() >= consumption) {
            fuelFluidTank.drain(consumption, true);
            // 1920 = 96L/t (1A LV, default for recipes) * 20t/s
            int burnTime = fluidFuelRecipe.getDuration() * 1920 / this.getBaseSteamOutput();
            burnTime = SuSyCoalBoiler.modifyBurnTime(burnTime, isHighPressure);
            setFuelMaxBurnTime(burnTime);
        }
    }

    @Override
    protected int getCooldownInterval() {
        return isHighPressure ? 40 : 45;
    }

    @Override
    protected int getCoolDownRate() {
        return 1;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createUITemplate(entityPlayer)
                .widget(new TankWidget(fuelFluidTank, 119, 26, 10, 54)
                        .setBackgroundTexture(GuiTextures.PROGRESS_BAR_BOILER_EMPTY.get(isHighPressure)))
                .build(getHolder(), entityPlayer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick() {
        if (this.isActive()) {
            VanillaParticleEffects.RANDOM_LAVA_SMOKE.runEffect(this);
            if (ConfigHolder.machines.machineSounds && GTValues.RNG.nextDouble() < 0.1) {
                BlockPos pos = getPos();
                getWorld().playSound(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                        SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }

    @Override
    protected int getBaseSteamOutput() {
        // 48/96 L per tick
        return isHighPressure ? 1920 : 960;
    }
}
