package supersymmetry.integration.multistorage;

import static gregtech.api.capability.GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.capability.IEnergyContainer;
import shetiphian.multistorage.common.tileentity.TileEntityVaultBase;
import supersymmetry.api.metatileentity.logistics.DefaultCapabilities;

public class TileEntityVaultPowerGTEU extends TileEntityVaultBase implements IEnergyContainer {

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing side) {
        if (side != null && capability == CAPABILITY_ENERGY_CONTAINER) return true;
        return super.hasCapability(capability, side);
    }

    @Override
    @Nullable
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing side) {
        if (capability == CAPABILITY_ENERGY_CONTAINER) {
            IEnergyContainer delegate = findDelegate(side);
            if (delegate != null) {
                return CAPABILITY_ENERGY_CONTAINER.cast(delegate);
            }
            return CAPABILITY_ENERGY_CONTAINER.cast(DefaultCapabilities.getCapability(CAPABILITY_ENERGY_CONTAINER));
        }
        return super.getCapability(capability, side);
    }

    @Nullable
    protected IEnergyContainer findDelegate(@Nullable EnumFacing side) {
        if (side == null) return null;
        EnumFacing targetFacing = side.getOpposite();
        TileEntity te = getWorld().getTileEntity(getPos().offset(targetFacing));
        if (te == null || te instanceof TileEntityVaultPowerGTEU) return null;

        EnumFacing neighborSide = targetFacing.getOpposite();
        if (!te.hasCapability(CAPABILITY_ENERGY_CONTAINER, neighborSide)) return null;
        return te.getCapability(CAPABILITY_ENERGY_CONTAINER, neighborSide);
    }

    @Nullable
    protected IEnergyContainer findAnyDelegate() {
        if (world == null || pos == null) return null;
        for (EnumFacing side : EnumFacing.VALUES) {
            IEnergyContainer delegate = findDelegate(side);
            if (delegate != null) return delegate;
        }
        return null;
    }

    @Override
    public long acceptEnergyFromNetwork(@Nullable EnumFacing side, long voltage, long amperage) {
        if (side == null) return 0;
        IEnergyContainer delegate = findDelegate(side);
        if (delegate != null) {
            return delegate.acceptEnergyFromNetwork(side.getOpposite(), voltage, amperage);
        }
        return 0;
    }

    @Override
    public boolean inputsEnergy(@Nullable EnumFacing side) {
        if (side == null) return false;
        IEnergyContainer delegate = findDelegate(side);
        return delegate != null && delegate.inputsEnergy(side.getOpposite());
    }

    @Override
    public long changeEnergy(long differenceAmount) {
        return 0;
    }

    @Override
    public long getEnergyStored() {
        return 0;
    }

    @Override
    public long getEnergyCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getInputAmperage() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getInputVoltage() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isOneProbeHidden() {
        return true;
    }
}
