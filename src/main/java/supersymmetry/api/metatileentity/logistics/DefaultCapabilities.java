package supersymmetry.api.metatileentity.logistics;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DefaultCapabilities {

    private static final Object2ObjectArrayMap<Capability<?>, ?> DEFAULT_CAPABILITIES = new Object2ObjectArrayMap<>();

    static {
        // Item
        addCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new ItemStackHandler(1) {

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return stack;
            }

            @NotNull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }
        }));

        // Fluid
        addCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidTank(10000) {

            @Override
            public int fill(FluidStack resource, boolean doFill) {
                return 0;
            }

            @Override
            @Nullable
            public FluidStack drainInternal(int maxDrain, boolean doDrain) {
                return null;
            }
        }));

        // GTEU
        addCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER.cast(IEnergyContainer.DEFAULT));
    }

    public static boolean hasCapability(@NotNull Capability<?> capability) {
        return DEFAULT_CAPABILITIES.containsKey(capability);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getCapability(@NotNull Capability<T> capability) {
        return (T) DEFAULT_CAPABILITIES.getOrDefault(capability, null);
    }

    public static <T> void addCapability(@NotNull Capability<T> capability, @NotNull T value) {
        DEFAULT_CAPABILITIES.put(capability, capability.cast(value));
    }
}
