package supersymmetry.api.metatileentity.logistics;

import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;

public abstract class MetaTileEntityDelegator extends MetaTileEntity implements IDelegator {

    private final Map<EnumFacing, Map<Capability<?>, Object>> capabilityWrappers = new IdentityHashMap<>();
    private final ThreadLocal<EnumSet<CapabilityFamily>> activeCapabilities = new ThreadLocal<>();

    protected final Predicate<Capability<?>> capFilter;
    protected final int baseColor;

    public MetaTileEntityDelegator(ResourceLocation metaTileEntityId, Predicate<Capability<?>> capFilter,
                                   int baseColor) {
        super(metaTileEntityId);
        this.capFilter = capFilter;
        this.baseColor = baseColor;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        CapabilityFamily family = CapabilityFamily.from(capability);
        EnumSet<CapabilityFamily> active = activeCapabilities.get();
        if (family != null && active != null && active.contains(family)) return getWrappedCapability(capability, side);
        T delegatedCapability = getDelegatedCapability(capability, side);
        if (delegatedCapability == null) return getDefaultCapability(capability, side);
        if (family != null) return getWrappedCapability(capability, side);
        return delegatedCapability;
    }

    @SuppressWarnings("unchecked")
    private <T> T getWrappedCapability(Capability<T> capability, EnumFacing side) {
        synchronized (capabilityWrappers) {
            Map<Capability<?>, Object> sideWrappers = capabilityWrappers.computeIfAbsent(side,
                    ignored -> new IdentityHashMap<>());
            Object wrapper = sideWrappers.get(capability);
            if (wrapper == null) {
                wrapper = createWrapper(capability, side);
                sideWrappers.put(capability, wrapper);
            }
            return (T) wrapper;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createWrapper(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) new LockedItemHandler(side);
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (T) new LockedFluidHandler(side);
        if (capability == CapabilityEnergy.ENERGY) return (T) new LockedEnergyStorage(side);
        return (T) new LockedEnergyContainer(side);
    }

    private <T, R> T locked(EnumFacing side, CapabilityFamily family, T fallback, Function<? super R, T> call) {
        EnumSet<CapabilityFamily> active = activeCapabilities.get();
        if (active == null) {
            active = EnumSet.noneOf(CapabilityFamily.class);
            activeCapabilities.set(active);
        }
        if (!active.add(family)) return fallback;
        try {
            R target = resolveTarget(family.capability(), side);
            return target == null ? fallback : call.apply(target);
        } finally {
            active.remove(family);
            if (active.isEmpty()) activeCapabilities.remove();
        }
    }

    @SuppressWarnings("unchecked")
    private <R> R resolveTarget(Capability<?> capability, EnumFacing side) {
        Capability<Object> typedCapability = (Capability<Object>) capability;
        return (R) getDelegatedCapability(typedCapability, side);
    }

    private enum CapabilityFamily {

        ITEM,
        FLUID,
        FE,
        GTEU;

        @Nullable
        static CapabilityFamily from(Capability<?> capability) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return ITEM;
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return FLUID;
            if (capability == CapabilityEnergy.ENERGY) return FE;
            if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) return GTEU;
            return null;
        }

        Capability<?> capability() {
            return switch (this) {
                case ITEM -> CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
                case FLUID -> CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
                case FE -> CapabilityEnergy.ENERGY;
                case GTEU -> GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER;
            };
        }
    }

    private final class LockedItemHandler implements IItemHandler {

        private final EnumFacing side;

        LockedItemHandler(EnumFacing side) {
            this.side = side;
        }

        @Override
        public int getSlots() {
            return locked(side, CapabilityFamily.ITEM, 0, IItemHandler::getSlots);
        }

        @Override
        @NotNull
        public ItemStack getStackInSlot(int slot) {
            return locked(
                    side, CapabilityFamily.ITEM, ItemStack.EMPTY, (IItemHandler target) -> target.getStackInSlot(slot));
        }

        @Override
        @NotNull
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return locked(
                    side,
                    CapabilityFamily.ITEM,
                    stack,
                    (IItemHandler target) -> target.insertItem(slot, stack, simulate));
        }

        @Override
        @NotNull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return locked(
                    side,
                    CapabilityFamily.ITEM,
                    ItemStack.EMPTY,
                    (IItemHandler target) -> target.extractItem(slot, amount, simulate));
        }

        @Override
        public int getSlotLimit(int slot) {
            return locked(side, CapabilityFamily.ITEM, 0, (IItemHandler target) -> target.getSlotLimit(slot));
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return locked(
                    side,
                    CapabilityFamily.ITEM,
                    false,
                    (IItemHandler target) -> target.isItemValid(slot, stack));
        }
    }

    private final class LockedFluidHandler implements IFluidHandler {

        private final EnumFacing side;

        LockedFluidHandler(EnumFacing side) {
            this.side = side;
        }

        @Override
        @NotNull
        public IFluidTankProperties[] getTankProperties() {
            return locked(side, CapabilityFamily.FLUID, new IFluidTankProperties[0], IFluidHandler::getTankProperties);
        }

        @Override
        public int fill(@NotNull FluidStack resource, boolean doFill) {
            return locked(
                    side, CapabilityFamily.FLUID, 0, (IFluidHandler target) -> target.fill(resource, doFill));
        }

        @Override
        @Nullable
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return locked(
                    side, CapabilityFamily.FLUID, null, (IFluidHandler target) -> target.drain(resource, doDrain));
        }

        @Override
        @Nullable
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return locked(
                    side, CapabilityFamily.FLUID, null, (IFluidHandler target) -> target.drain(maxDrain, doDrain));
        }
    }

    private final class LockedEnergyStorage implements IEnergyStorage {

        private final EnumFacing side;

        LockedEnergyStorage(EnumFacing side) {
            this.side = side;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return locked(
                    side,
                    CapabilityFamily.FE,
                    0,
                    (IEnergyStorage target) -> target.receiveEnergy(maxReceive, simulate));
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return locked(
                    side,
                    CapabilityFamily.FE,
                    0,
                    (IEnergyStorage target) -> target.extractEnergy(maxExtract, simulate));
        }

        @Override
        public int getEnergyStored() {
            return locked(side, CapabilityFamily.FE, 0, IEnergyStorage::getEnergyStored);
        }

        @Override
        public int getMaxEnergyStored() {
            return locked(side, CapabilityFamily.FE, 0, IEnergyStorage::getMaxEnergyStored);
        }

        @Override
        public boolean canExtract() {
            return locked(side, CapabilityFamily.FE, false, IEnergyStorage::canExtract);
        }

        @Override
        public boolean canReceive() {
            return locked(side, CapabilityFamily.FE, false, IEnergyStorage::canReceive);
        }
    }

    private final class LockedEnergyContainer implements IEnergyContainer {

        private final EnumFacing side;

        LockedEnergyContainer(EnumFacing side) {
            this.side = side;
        }

        @Override
        public long acceptEnergyFromNetwork(EnumFacing facing, long voltage, long amperage) {
            return locked(
                    side,
                    CapabilityFamily.GTEU,
                    0L,
                    (IEnergyContainer target) -> target.acceptEnergyFromNetwork(facing, voltage, amperage));
        }

        @Override
        public boolean inputsEnergy(EnumFacing facing) {
            return locked(
                    side, CapabilityFamily.GTEU, false, (IEnergyContainer target) -> target.inputsEnergy(facing));
        }

        @Override
        public boolean outputsEnergy(EnumFacing facing) {
            return locked(
                    side, CapabilityFamily.GTEU, false, (IEnergyContainer target) -> target.outputsEnergy(facing));
        }

        @Override
        public long changeEnergy(long amount) {
            return locked(
                    side, CapabilityFamily.GTEU, 0L, (IEnergyContainer target) -> target.changeEnergy(amount));
        }

        @Override
        public long addEnergy(long amount) {
            return locked(
                    side, CapabilityFamily.GTEU, 0L, (IEnergyContainer target) -> target.addEnergy(amount));
        }

        @Override
        public long removeEnergy(long amount) {
            return locked(
                    side, CapabilityFamily.GTEU, 0L, (IEnergyContainer target) -> target.removeEnergy(amount));
        }

        @Override
        public long getEnergyCanBeInserted() {
            return locked(side, CapabilityFamily.GTEU, 0L, IEnergyContainer::getEnergyCanBeInserted);
        }

        @Override
        public long getEnergyStored() {
            return locked(side, CapabilityFamily.GTEU, 0L, IEnergyContainer::getEnergyStored);
        }

        @Override
        public long getEnergyCapacity() {
            return locked(side, CapabilityFamily.GTEU, 0L, IEnergyContainer::getEnergyCapacity);
        }

        @Override
        public long getOutputAmperage() {
            return locked(side, CapabilityFamily.GTEU, 0L, IEnergyContainer::getOutputAmperage);
        }

        @Override
        public long getOutputVoltage() {
            return locked(side, CapabilityFamily.GTEU, 0L, IEnergyContainer::getOutputVoltage);
        }

        @Override
        public long getInputAmperage() {
            return locked(side, CapabilityFamily.GTEU, 0L, IEnergyContainer::getInputAmperage);
        }

        @Override
        public long getInputVoltage() {
            return locked(side, CapabilityFamily.GTEU, 0L, IEnergyContainer::getInputVoltage);
        }

        @Override
        public long getInputPerSec() {
            return locked(side, CapabilityFamily.GTEU, 0L, IEnergyContainer::getInputPerSec);
        }

        @Override
        public long getOutputPerSec() {
            return locked(side, CapabilityFamily.GTEU, 0L, IEnergyContainer::getOutputPerSec);
        }

        @Override
        public boolean isOneProbeHidden() {
            return locked(side, CapabilityFamily.GTEU, false, IEnergyContainer::isOneProbeHidden);
        }
    }

    protected <T> T getDefaultCapability(Capability<T> capability, EnumFacing side) {
        return side != null && capFilter.test(capability) && DefaultCapabilities.hasCapability(capability) ?
                DefaultCapabilities.getCapability(capability) : super.getCapability(capability, side);
    }

    protected <T> T getDelegatedCapability(Capability<T> capability, EnumFacing side) {
        if (capability == null || !capFilter.test(capability) || side == null) return null;
        EnumFacing delegatingFacing = getDelegatingFacing(side);
        if (delegatingFacing == null) return null;
        TileEntity te = getWorld().getTileEntity(getPos().offset(delegatingFacing));
        if (te == null ||
                (te instanceof MetaTileEntityHolder holder && holder.getMetaTileEntity() instanceof IDelegator))
            return null;
        // TODO: make IDelegator a capability when Jet Wingsuit PR gets merged
        return te.getCapability(capability, delegatingFacing.getOpposite());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(this.getPaintingColorForRendering())));
        for (EnumFacing facing : EnumFacing.values()) {
            Textures.renderFace(renderState, translation, colouredPipeline, facing, Cuboid6.full, this.getBaseTexture(),
                    BlockRenderLayer.CUTOUT_MIPPED);
        }
    }

    @SideOnly(Side.CLIENT)
    protected TextureAtlasSprite getBaseTexture() {
        return Textures.PIPE_SIDE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("susy.machine.delegator.tooltip.non_recursion"));
    }

    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseTexture(), getPaintingColorForRendering());
    }

    @Override
    public int getDefaultPaintingColor() {
        return this.baseColor;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

}
