package supersymmetry.api.capability.impl;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.NotNull;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.util.OverlayedFluidHandler;
import supersymmetry.api.capability.SuSyDataCodes;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityInductionFurnace;

/** Keeps the furnace's auxiliary cooling reaction from taking its current batch's output space. */
public class InductionFurnaceRecipeLogic extends MultiblockRecipeLogic {

    private static final String DELIVERY = "FurnaceDelivery";
    private final MetaTileEntityInductionFurnace furnace;
    private final List<FluidStack> pendingFluids = new ArrayList<>();
    private final List<ItemStack> pendingItems = new ArrayList<>();
    private boolean waitingForOutputs;

    public InductionFurnaceRecipeLogic(MetaTileEntityInductionFurnace furnace) {
        super(furnace);
        this.furnace = furnace;
    }

    @Override
    public void updateWorkable() {
        if (furnace.getWorld() != null && !furnace.getWorld().isRemote && workingEnabled && hasPendingOutputs()) {
            deliverPendingOutputs();
        }
        super.updateWorkable();
    }

    @Override
    protected boolean shouldSearchForRecipes() {
        return !hasPendingOutputs() && !furnace.hasPendingCoolant() && super.shouldSearchForRecipes();
    }

    @Override
    public boolean prepareRecipe(gregtech.api.recipes.Recipe recipe,
                                 net.minecraftforge.items.IItemHandlerModifiable items,
                                 gregtech.api.capability.IMultipleTankHandler fluids) {
        return !hasPendingOutputs() && !furnace.hasPendingCoolant() && super.prepareRecipe(recipe, items, fluids);
    }

    @Override
    protected void outputRecipeOutputs() {
        // The parent completion (including the configured SussyPatches overwrite)
        // runs once. Its ordinary clearing is safe after ownership moves here.
        if (fluidOutputs != null) {
            for (FluidStack output : fluidOutputs) pendingFluids.add(output.copy());
        }
        if (itemOutputs != null) {
            for (ItemStack output : itemOutputs) pendingItems.add(output.copy());
        }
        deliverPendingOutputs();
        furnace.markDirty();
    }

    private void deliverPendingOutputs() {
        boolean changed = deliverFluidOutputs(pendingFluids, getOutputTank(), furnace.canVoidRecipeFluidOutputs());
        for (int i = 0; i < pendingItems.size();) {
            ItemStack remaining = pendingItems.get(i);
            ItemStack result = ItemHandlerHelper.insertItemStacked(getOutputInventory(), remaining.copy(), false);
            if (result.getCount() != remaining.getCount()) changed = true;
            if (result.isEmpty() || furnace.canVoidRecipeItemOutputs()) {
                pendingItems.remove(i);
                changed = true;
            } else {
                pendingItems.set(i, result);
                i++;
            }
        }
        if (changed) furnace.markDirty();
        setWaitingForOutputs(hasPendingOutputs());
    }

    static boolean deliverFluidOutputs(List<FluidStack> pendingFluids, IFluidHandler destination, boolean canVoid) {
        boolean changed = false;
        for (int i = 0; i < pendingFluids.size();) {
            FluidStack remaining = pendingFluids.get(i);
            int accepted = destination.fill(remaining.copy(), true);
            if (accepted > 0) {
                remaining.amount -= accepted;
                changed = true;
            }
            if (remaining.amount <= 0 || canVoid) {
                pendingFluids.remove(i);
                changed = true;
            } else i++;
        }
        return changed;
    }

    public boolean hasPendingOutputs() {
        return !pendingFluids.isEmpty() || !pendingItems.isEmpty();
    }

    public boolean isWaitingForOutputs() {
        return waitingForOutputs;
    }

    public boolean canOutputCoolant(FluidStack coolant) {
        List<FluidStack> products = progressTime > 0 && fluidOutputs != null ? fluidOutputs : pendingFluids;
        return canOutputCoolant(getOutputTank(), coolant, products, furnace.canVoidRecipeFluidOutputs());
    }

    static boolean canOutputCoolant(gregtech.api.capability.IMultipleTankHandler destination, FluidStack coolant,
                                    List<FluidStack> products, boolean canVoid) {
        OverlayedFluidHandler overlay = new OverlayedFluidHandler(destination);
        if (overlay.insertFluid(coolant, coolant.amount) != coolant.amount) return false;
        if (canVoid) return true;
        for (FluidStack product : products) {
            if (overlay.insertFluid(product, product.amount) != product.amount) return false;
        }
        return true;
    }

    private void setWaitingForOutputs(boolean waiting) {
        if (waitingForOutputs != waiting) {
            waitingForOutputs = waiting;
            writeCustomData(SuSyDataCodes.UPDATE_FURNACE_DELIVERY, buf -> buf.writeBoolean(waiting));
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound data = super.serializeNBT();
        if (hasPendingOutputs()) {
            NBTTagCompound delivery = new NBTTagCompound();
            delivery.setInteger("Version", 1);
            NBTTagList fluids = new NBTTagList();
            for (FluidStack fluid : pendingFluids) fluids.appendTag(fluid.writeToNBT(new NBTTagCompound()));
            delivery.setTag("Fluids", fluids);
            NBTTagList items = new NBTTagList();
            for (ItemStack item : pendingItems) items.appendTag(item.writeToNBT(new NBTTagCompound()));
            delivery.setTag("Items", items);
            data.setTag(DELIVERY, delivery);
        }
        return data;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound data) {
        super.deserializeNBT(data);
        pendingFluids.clear();
        pendingItems.clear();
        NBTTagCompound delivery = data.getCompoundTag(DELIVERY);
        NBTTagList fluids = delivery.getTagList("Fluids", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fluids.tagCount(); i++) {
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(fluids.getCompoundTagAt(i));
            if (fluid != null && fluid.amount > 0) pendingFluids.add(fluid);
        }
        NBTTagList items = delivery.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < items.tagCount(); i++) {
            ItemStack item = new ItemStack(items.getCompoundTagAt(i));
            if (!item.isEmpty()) pendingItems.add(item);
        }
        waitingForOutputs = hasPendingOutputs();
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(waitingForOutputs);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        waitingForOutputs = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        if (dataId == SuSyDataCodes.UPDATE_FURNACE_DELIVERY) waitingForOutputs = buf.readBoolean();
        else super.receiveCustomData(dataId, buf);
    }
}
