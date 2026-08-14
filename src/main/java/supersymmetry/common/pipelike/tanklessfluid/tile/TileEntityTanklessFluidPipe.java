package supersymmetry.common.pipelike.tanklessfluid.tile;

import java.lang.ref.WeakReference;
import java.util.EnumMap;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.Nullable;

import gregtech.api.pipenet.block.material.TileEntityMaterialPipeBase;
import gregtech.api.pipenet.tile.IPipeTile;
import lombok.val;
import supersymmetry.api.unification.material.properties.TanklessFluidPipeProperties;
import supersymmetry.common.pipelike.tanklessfluid.TanklessFluidPipeType;
import supersymmetry.common.pipelike.tanklessfluid.net.FluidNetHandler;
import supersymmetry.common.pipelike.tanklessfluid.net.TanklessFluidPipeNet;
import supersymmetry.common.pipelike.tanklessfluid.net.WorldTanklessFluidPipeNet;

public class TileEntityTanklessFluidPipe
                                         extends
                                         TileEntityMaterialPipeBase<TanklessFluidPipeType, TanklessFluidPipeProperties> {

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
    public boolean supportsTicking() {
        return false;
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
}
