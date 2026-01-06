package supersymmetry.api.items;

import gregtech.api.GTValues;
import gregtech.api.unification.Elements;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackHashStrategy;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.util.SuSyUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class CargoItemStackHandler implements IItemHandler, INBTSerializable<NBTTagCompound> {
    private int maxVolume;
    private int maxWeight;
    private int currentVolume = 0;
    private int currentWeight = 0;

    private boolean loading = true;
    private static final ItemStackHashStrategy STACK_STRATEGY = ItemStackHashStrategy.comparingAllButCount();
    private final ObjectOpenCustomHashSet<List<ItemStack>> cargo = new ObjectOpenCustomHashSet<>(new CargoHashStrategy());

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("cargoSize", cargo.size());
        nbt.setInteger("currentVolume", currentVolume);
        nbt.setInteger("currentWeight", currentWeight);
        nbt.setBoolean("loading", loading);
        nbt.setInteger("maxVolume", maxVolume);
        nbt.setInteger("maxWeight", maxWeight);

        List<List<ItemStack>> cargoList = new ArrayList<>(cargo);
        for (int i = 0; i < cargo.size(); i++) {
            List<ItemStack> itemType = cargoList.get(i);
            NBTTagCompound itemNbt = new NBTTagCompound();
            itemNbt.setInteger("size", itemType.size());
            for (int j = 0; j < itemType.size(); j++) {
                itemNbt.setTag("item" + j, itemType.get(j).serializeNBT());
            }
            nbt.setTag("entry" + i, itemNbt);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.maxVolume = nbt.getInteger("maxVolume");
        this.maxWeight = nbt.getInteger("maxWeight");
        this.currentVolume = nbt.getInteger("currentVolume");
        this.currentWeight = nbt.getInteger("currentWeight");
        this.loading = nbt.getBoolean("loading");
        this.cargo.clear();

        for (int i = 0; i < nbt.getInteger("cargoSize"); i++) {
            NBTTagCompound itemTag = nbt.getCompoundTag("item" + i);
            List<ItemStack> itemType = new ArrayList<>(itemTag.getInteger("size"));
            for (int j = 0; j < itemTag.getInteger("size"); j++) {
                itemType.add(new ItemStack(itemTag.getCompoundTag("item" + j)));
            }
            cargo.add(itemType);
        }
    }

    public void clear() {
        this.cargo.clear();
        this.currentVolume = 0;
        this.currentWeight = 0;
    }

    public boolean isEmpty() {
        return this.cargo.isEmpty();
    }

    public static class CargoHashStrategy implements Hash.Strategy<List<ItemStack>> {

        @Override
        public int hashCode(List<ItemStack> o) {
            if (o.isEmpty()) {
                throw new IllegalArgumentException("Cannot hash empty list");
            }
            return STACK_STRATEGY.hashCode(o.get(0));
        }

        @Override
        public boolean equals(List<ItemStack> a, List<ItemStack> b) {
            if (a == null || b == null) {
                return a == b;
            }
            if (a.isEmpty() || b.isEmpty()) {
                return a.isEmpty() && b.isEmpty();
            }
            return STACK_STRATEGY.equals(a.get(0), b.get(0));
        }
    }

    private List<ItemStack> getBucketForItem(ItemStack stack) {
        // Dummy list
        List<ItemStack> bucket = new ArrayList<>(1);
        bucket.add(stack);
        return cargo.get(bucket);
    }

    public CargoItemStackHandler(int maxVolume, int maxWeight) {
        this.maxVolume = maxVolume;
        this.maxWeight = maxWeight;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!loading) return stack;
        if (!SuSyUtility.isAllowedItemForSpace(stack)) return stack;
        int mass = getMassPerItem(stack);
        List<ItemStack> bucket = getBucketForItem(stack);
        int remainingWeight = maxWeight - currentWeight;
        int maxAddition = Math.min(stack.getCount(), remainingWeight / mass);
        maxAddition = Math.min(maxAddition, this.getSlotLimit(0));
        int overflowAmount = 0;
        boolean needsNewBucket = false;
        if (bucket == null) {
            bucket = new ArrayList<>(1);
            needsNewBucket = true;
            if (maxVolume <= currentVolume) {
                return stack;
            }
        }

        if (!needsNewBucket) {
            ItemStack currentStack = bucket.getLast();
            overflowAmount = currentStack.getCount() + stack.getCount() - currentStack.getMaxStackSize();
            if (overflowAmount > 0 && maxVolume <= currentVolume) {
                maxAddition = Math.min(maxAddition, currentStack.getMaxStackSize() - currentStack.getCount());
            }
        }

        if (!simulate) {
            if (overflowAmount > 0) {
                // Fill up the last bucket, and add a new one for overflow
                bucket.getLast().setCount(bucket.getLast().getMaxStackSize());
                bucket.add(ItemHandlerHelper.copyStackWithSize(stack, overflowAmount));
                currentVolume++;
            } else if (needsNewBucket) {
                // Just add the new cargo slot
                bucket.add(stack);
                cargo.add(bucket);
                currentVolume++;
            } else {
                // Add to the last bucket like normal
                bucket.getLast().setCount(bucket.getLast().getCount() + maxAddition);
            }
            currentWeight += maxAddition * mass;
        }
        return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - maxAddition);
    }

    public int getMassPerItem(ItemStack item) {
        // GTCEu mass info
        ItemMaterialInfo info = OreDictUnifier.getMaterialInfo(item);
        if (info != null) {
            return (int) (info.getMaterials().stream().mapToLong((stack) -> stack.material.getMass() * stack.amount).sum() /
                    (GTValues.M / 36));
        }
        return 98 * 36 * 4; // default mass times 36 times another fudge factor
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (loading) return ItemStack.EMPTY;
        if (cargo.isEmpty()) return ItemStack.EMPTY;
        // Get just any item
        List<ItemStack> bucket = cargo.iterator().next();
        ItemStack last = bucket.getLast();
        int actuallyRemoved = Math.min(last.getCount(), amount);
        if (!simulate) {
            int mass = getMassPerItem(last);
            int massRemoval = actuallyRemoved * mass;
            currentWeight -= massRemoval;
            if (actuallyRemoved == last.getCount()) {
                currentVolume--;
                bucket.removeLast();
                if (bucket.isEmpty()) {
                    cargo.remove(bucket);
                }
            } else {
                last.setCount(last.getCount() - actuallyRemoved);
            }
        }
        return ItemHandlerHelper.copyStackWithSize(last, actuallyRemoved);
    }

    public ItemStack getExposedStack() {
        if (cargo.isEmpty()) return ItemStack.EMPTY;
        return cargo.iterator().next().getLast();
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    public void stopLoading() {
        this.loading = false;
    }

    public boolean isFull() {
        return currentVolume >= maxVolume;
    }
    public boolean massTooHigh() {
        return currentWeight * 6 >= maxWeight * 5;
    }


}
