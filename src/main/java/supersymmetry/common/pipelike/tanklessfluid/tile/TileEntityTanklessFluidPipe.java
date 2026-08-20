package supersymmetry.common.pipelike.tanklessfluid.tile;

import java.lang.ref.WeakReference;
import java.util.EnumMap;

import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import lombok.Getter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.Nullable;

import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.pipenet.tile.IPipeTile;
import lombok.val;
import org.jspecify.annotations.NonNull;
import supersymmetry.api.capability.SuSyDataCodes;
import supersymmetry.api.pipelike.ConnectablePipe;
import supersymmetry.api.unification.material.properties.TanklessFluidPipeProperties;
import supersymmetry.common.pipelike.tanklessfluid.TanklessFluidPipeType;
import supersymmetry.common.pipelike.tanklessfluid.net.FluidNetHandler;
import supersymmetry.common.pipelike.tanklessfluid.net.TanklessFluidPipeNet;
import supersymmetry.common.pipelike.tanklessfluid.net.WorldTanklessFluidPipeNet;

public class TileEntityTanklessFluidPipe
        extends TileEntityMaterialPipeBase<TanklessFluidPipeType, TanklessFluidPipeProperties>
        implements ConnectablePipe {

    private static final String NBT_FLANGE_VISIBILITY = "FlangeVisibility";

    @Getter
    private int flangeVisibility = 0b111111;
    private final EnumMap<EnumFacing, FluidNetHandler> handlers = new EnumMap<>(EnumFacing.class);
    private FluidNetHandler defaultHandler;
    // the FluidNetHandler can only be created on the server, so we have an empty placeholder for the client
    private final IFluidHandler clientCapability = new FluidTank(0);
    private WeakReference<TanklessFluidPipeNet> currentPipeNet = new WeakReference<>(null);

    private int transferredFluids = 0;
    private long timer = 0;

    public long getWorldTime() {
        return hasWorld() ? getWorld().getTotalWorldTime() : 0L;
    }

    @Override
    public Class<TanklessFluidPipeType> getPipeTypeClass() {
        return TanklessFluidPipeType.class;
    }

    @Override
    public boolean canConnectWith(@NonNull IPipeTile<?, ?> other) {
        return other instanceof TileEntityFluidPipe;
    }

    @Override
    public boolean supportsTicking() {
        return false;
    }

    public boolean isFlangeVisible(EnumFacing side) {
        return (this.flangeVisibility & (1 << side.getIndex())) != 0;
    }

    public void toggleFlangeVisible(EnumFacing side) {
        if (getWorld().isRemote) {
            return;
        }
        boolean newVisible = !isFlangeVisible(side);
        setFlangeVisible(side, newVisible);
        TileEntity neighbor = getNeighbor(side);
        if (neighbor instanceof TileEntityTanklessFluidPipe neighborPipe &&
                neighborPipe.isConnected(side.getOpposite())) {
            neighborPipe.setFlangeVisible(side.getOpposite(), newVisible);
        }
    }

    public void setFlangeVisible(EnumFacing side, boolean visible) {
        if (getWorld().isRemote) {
            return;
        }
        int current = getFlangeVisibility();
        int mask = visible ? (current | (1 << side.getIndex())) : (current & ~(1 << side.getIndex()));
        setFlangeVisibility(mask);
    }

    private void setFlangeVisibility(int mask) {
        if (this.flangeVisibility == mask) {
            return;
        }
        this.flangeVisibility = mask;
        writeCustomData(SuSyDataCodes.UPDATE_FLANGE_VISIBILITY, buf -> buf.writeVarInt(mask));
        markDirty();
    }

    private void initHandlers() {
        val net = getPipeNet();
        if (net == null) {
            return;
        }
        for (val facing : EnumFacing.VALUES) {
            handlers.put(facing, new FluidNetHandler(net, this, facing));
        }
        defaultHandler = new FluidNetHandler(net, this, null);
    }

    @Nullable @Override
    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (world.isRemote) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(clientCapability);
            }
            if (handlers.isEmpty()) {
                initHandlers();
            }
            checkNetwork();
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(handlers.getOrDefault(facing, defaultHandler));
        }
        return super.getCapabilityInternal(capability, facing);
    }

    public void checkNetwork() {
        if (defaultHandler != null) {
            val current = getPipeNet();
            if (defaultHandler.getNet() != current) {
                defaultHandler.updateNetwork(current);
                for (val handler : handlers.values()) {
                    handler.updateNetwork(current);
                }
            }
        }
    }

    @Nullable public TanklessFluidPipeNet getPipeNet() {
        if (world == null || world.isRemote) {
            return null;
        }
        var currentPipeNet = this.currentPipeNet.get();
        if (currentPipeNet != null && currentPipeNet.isValid() && currentPipeNet.containsNode(getPipePos())) {
            return currentPipeNet; // if current net is valid and does contain position, return it
        }
        val worldNet = (WorldTanklessFluidPipeNet) getPipeBlock().getWorldPipeNet(getPipeWorld());
        currentPipeNet = worldNet.getNetFromPos(getPipePos());
        // noinspection ConstantValue
        if (currentPipeNet != null) {
            this.currentPipeNet = new WeakReference<>(currentPipeNet);
        }
        return currentPipeNet;
    }

    @Override
    public void transferDataFrom(IPipeTile<TanklessFluidPipeType, TanklessFluidPipeProperties> tileEntity) {
        super.transferDataFrom(tileEntity);
        val fluidPipe = (TileEntityTanklessFluidPipe) tileEntity;
        // take handlers from old pipe
        if (!fluidPipe.handlers.isEmpty()) {
            this.handlers.clear();
            for (val handler : fluidPipe.handlers.values()) {
                handler.updatePipe(this);
                this.handlers.put(handler.getFacing(), handler);
            }
        }
        if (fluidPipe.defaultHandler != null) {
            fluidPipe.defaultHandler.updatePipe(this);
            this.defaultHandler = fluidPipe.defaultHandler;
        }
        this.flangeVisibility = fluidPipe.flangeVisibility;
    }

    private void updateTransferredState() {
        long currentTime = getWorldTime();
        long dif = currentTime - this.timer;
        if (dif >= 20 || dif < 0) {
            this.transferredFluids = 0;
            this.timer = currentTime;
        }
    }

    public void addTransferredFluids(int amount) {
        updateTransferredState();
        this.transferredFluids += amount;
    }

    public int getTransferredFluids() {
        updateTransferredState();
        return this.transferredFluids;
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        this.handlers.clear();
    }

    @Override
    public @NonNull NBTTagCompound writeToNBT(@NonNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger(NBT_FLANGE_VISIBILITY, this.flangeVisibility);
        return compound;
    }

    @Override
    public void readFromNBT(@NonNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey(NBT_FLANGE_VISIBILITY)) {
            this.flangeVisibility = compound.getInteger(NBT_FLANGE_VISIBILITY);
        } else {
            this.flangeVisibility = 0b111111;
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeVarInt(getFlangeVisibility());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.flangeVisibility = buf.readVarInt();
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {
        super.receiveCustomData(discriminator, buf);
        if (discriminator == SuSyDataCodes.UPDATE_FLANGE_VISIBILITY && buf.isReadable()) {
            this.flangeVisibility = buf.readVarInt();
            scheduleChunkForRenderUpdate();
        }
    }
}
