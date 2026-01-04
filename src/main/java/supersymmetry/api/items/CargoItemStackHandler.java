package supersymmetry.api.items;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.NotNull;

import gregtech.api.GTValues;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.util.ItemStackHashStrategy;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import supersymmetry.api.util.SuSyUtility;

public class CargoItemStackHandler implements IItemHandler {

    private final int maxVolume;
    private final int maxWeight;
    private int currentVolume = 0;
    private int currentWeight = 0;

    private boolean loading = true;
    private static final ItemStackHashStrategy STACK_STRATEGY = ItemStackHashStrategy.comparingAllButCount();
    private final ObjectOpenCustomHashSet<List<ItemStack>> cargo = new ObjectOpenCustomHashSet<>(
            new CargoHashStrategy());

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
            return (int) (info.getMaterials().stream().mapToLong((stack) -> stack.material.getMass() * stack.amount)
                    .sum() /
                    (GTValues.M / 36));
        }
        return 12; // TODO
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
