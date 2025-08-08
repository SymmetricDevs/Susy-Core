package supersymmetry.api.capability.impl;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import supersymmetry.common.metatileentities.storage.MetaTileEntityLockedCrate;

/// A custom item handler delegate that prevents any insertion or extraction,
/// but remains modifiable.
///
/// See [MetaTileEntityLockedCrate#initializeInventory()]
public class InaccessibleHandlerDelegate implements IItemHandlerModifiable {

    protected final IItemHandlerModifiable delegate;

    public InaccessibleHandlerDelegate(IItemHandlerModifiable delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        delegate.setStackInSlot(slot, stack);
    }

    @Override
    public int getSlots() {
        return delegate.getSlots();
    }

    @Override
    @NotNull
    public ItemStack getStackInSlot(int slot) {
        return delegate.getStackInSlot(slot);
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return delegate.getSlotLimit(slot);
    }
}
