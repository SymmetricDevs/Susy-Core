package supersymmetry.api.util;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class DataStorageLoader extends NotifiableItemStackHandler implements IItemHandlerModifiable {
    private ItemStack dataStorage = ItemStack.EMPTY;
    private boolean locked = false;
    private final Predicate<ItemStack> acceptableTypes;
    protected MetaTileEntity mte; // If GTItemStackHandler ever makes its mte's accessible, remove this

    public DataStorageLoader(MetaTileEntity mte, Predicate<ItemStack> predicate) {
        super(mte,1,mte,false);
        this.mte = mte;
        acceptableTypes = predicate;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        return dataStorage;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (locked || slot != 0 || !acceptableTypes.test(stack) || stack.getCount() < 1 || !dataStorage.isEmpty()) {
            return stack;
        } else {
            // if empty slot, take 1
            // if no empty slot, deny
            ItemStack ret = stack.copy();
            ret.setCount(stack.getCount()-1);
            if (!simulate) {
                dataStorage = stack.copy();
                dataStorage.setCount(1);
            }
            return ret;
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (locked || amount > 1) {
            return ItemStack.EMPTY;
        } else {
            ItemStack ret = dataStorage;
            if (!simulate) {
                dataStorage = ItemStack.EMPTY;
            }
            return ret;
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    public void setLocked(boolean locked) {
        if (this.locked != locked) {
            this.locked = locked;

            mte.markDirty();
            if (mte.getWorld() != null && !mte.getWorld().isRemote) {
                mte.writeCustomData(GregtechDataCodes.LOCK_OBJECT_HOLDER, buf -> buf.writeBoolean(locked));
            }
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void clearNBT() {
        if (dataStorage.hasTagCompound()) {
            dataStorage.setTagCompound(new NBTTagCompound());
        }
    }

    public void addToCompound(Function<NBTTagCompound, NBTTagCompound> consumer) {
        dataStorage.setTagCompound(consumer.apply(dataStorage.getTagCompound()));
    }

    public void mutateItem(String key, String value) {
        if (!dataStorage.hasTagCompound()) {
            dataStorage.setTagCompound(new NBTTagCompound());
        }
        dataStorage.getTagCompound().setTag(key,new NBTTagString(value)); // do not worry about warning
    }

    public void setImageType(int id) {
        if (!dataStorage.hasTagCompound()) {
            dataStorage.setTagCompound(new NBTTagCompound());
        }
        dataStorage.setItemDamage(id);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (slot == 0) {
            dataStorage = stack;
        }
    }
}
