package supersymmetry.common.item.behavior;

import java.awt.*;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.impl.GTFluidHandlerItemStack;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GradientUtil;

public class HydrogenPoweredDroneBehavior implements IItemDurabilityManager, IItemCapabilityProvider, IItemBehaviour {

    private static final IFilter<FluidStack> HYDROGEN_FILTER = new IFilter<>() {

        @Override
        public boolean test(@NotNull FluidStack fluidStack) {
            return fluidStack.isFluidEqual(new FluidStack(Materials.Hydrogen.getFluid(), 1));
        }

        @Override
        public int getPriority() {
            return IFilter.whitelistLikePriority();
        }
    };

    public final int maxCapacity;
    private final Pair<Color, Color> durabilityBarColors;

    public HydrogenPoweredDroneBehavior(int internalCapacity) {
        this.maxCapacity = internalCapacity;
        this.durabilityBarColors = GradientUtil.getGradient(0x0097CE, 10);
    }

    @Override
    public double getDurabilityForDisplay(@NotNull ItemStack itemStack) {
        IFluidHandlerItem fluidHandlerItem = itemStack
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandlerItem == null) return 0;
        IFluidTankProperties fluidTankProperties = fluidHandlerItem.getTankProperties()[0];
        FluidStack fluidStack = fluidTankProperties.getContents();
        return fluidStack == null ? 0 : (double) fluidStack.amount / (double) fluidTankProperties.getCapacity();
    }

    @Nullable
    @Override
    public Pair<Color, Color> getDurabilityColorsForDisplay(ItemStack itemStack) {
        return durabilityBarColors;
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        return new GTFluidHandlerItemStack(itemStack, maxCapacity)
                .setFilter(HYDROGEN_FILTER);
    }
}
