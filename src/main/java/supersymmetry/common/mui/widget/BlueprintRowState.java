package supersymmetry.common.mui.widget;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.util.DataStorageLoader;

public class BlueprintRowState {

    public final List<DataStorageLoader> slots;
    public final int[] validMultiplierValues;
    public boolean shortView = false;
    public int multiplierIndex = 0;

    public BlueprintRowState(List<DataStorageLoader> slots, int[] validMultiplierValues) {
        this.slots = slots;
        this.validMultiplierValues = validMultiplierValues;
    }

    public int getMultiplier() {
        return validMultiplierValues[multiplierIndex];
    }

    /**
     * Returns null on INVALID_CARD; empty list is a legitimate "no components" result.
     */
    public List<AbstractComponent<?>> materializeComponents() {
        if (shortView) {
            ItemStack firstItem = slots.isEmpty() ? ItemStack.EMPTY : slots.get(0).getStackInSlot(0);
            NBTTagCompound firstnbt = firstItem.hasTagCompound() ? firstItem.getTagCompound() : null;
            if (firstnbt == null || !firstnbt.hasKey("name")) return null;
            AbstractComponent<?> proto = AbstractComponent.getComponentFromName(firstnbt.getString("name"));
            if (proto == null) return null;
            Optional<?> templateOpt = proto.readFromNBT(firstnbt);
            if (!templateOpt.isPresent()) return null;
            @SuppressWarnings("unchecked")
            AbstractComponent<?> template = (AbstractComponent<?>) templateOpt.get();
            int count = validMultiplierValues[multiplierIndex];
            return Stream.generate(() -> template).limit(count).collect(Collectors.toList());
        } else {
            return slots.stream()
                    .map(s -> s.getStackInSlot(0))
                    .filter(ItemStack::hasTagCompound)
                    .map(ItemStack::getTagCompound)
                    .filter(t -> t.hasKey("name"))
                    .map(t -> {
                        AbstractComponent<?> proto = AbstractComponent.getComponentFromName(t.getString("name"));
                        if (proto == null) return Optional.<AbstractComponent<?>>empty();
                        return proto.readFromNBT(t);
                    })
                    .filter(Optional::isPresent)
                    .map(opt -> (AbstractComponent<?>) opt.get())
                    .collect(Collectors.toList());
        }
    }

    public NBTTagCompound writeStateToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("shortView", shortView);
        tag.setInteger("multiplierIndex", multiplierIndex);
        NBTTagList slotList = new NBTTagList();
        for (int i = 0; i < slots.size(); i++) {
            ItemStack stack = slots.get(i).getStackInSlot(0);
            if (!stack.isEmpty()) {
                NBTTagCompound slotTag = new NBTTagCompound();
                slotTag.setInteger("slot", i);
                stack.writeToNBT(slotTag);
                slotList.appendTag(slotTag);
            }
        }
        tag.setTag("slots", slotList);
        return tag;
    }

    public void readStateFromNBT(NBTTagCompound tag) {
        shortView = tag.getBoolean("shortView");
        int idx = tag.getInteger("multiplierIndex");
        multiplierIndex = (idx >= 0 && idx < validMultiplierValues.length) ? idx : 0;
        for (DataStorageLoader slot : slots) {
            slot.setStackInSlot(0, ItemStack.EMPTY);
        }
        NBTTagList slotList = tag.getTagList("slots", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < slotList.tagCount(); i++) {
            NBTTagCompound slotTag = slotList.getCompoundTagAt(i);
            int slot = slotTag.getInteger("slot");
            if (slot >= 0 && slot < slots.size()) {
                slots.get(slot).setStackInSlot(0, new ItemStack(slotTag));
            }
        }
    }
}
