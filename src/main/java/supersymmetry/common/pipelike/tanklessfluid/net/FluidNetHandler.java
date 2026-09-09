package supersymmetry.common.pipelike.tanklessfluid.net;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.common.covers.CoverFluidFilter;
import gregtech.common.covers.CoverFluidRegulator;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.FluidFilterMode;
import lombok.Getter;
import lombok.val;
import supersymmetry.common.pipelike.tanklessfluid.tile.TileEntityTanklessFluidPipe;

public class FluidNetHandler implements IFluidHandler {

    private static final IFluidTankProperties[] FAKE_TANK_PROPERTIES = new IFluidTankProperties[] {
            new FluidTankProperties(null, Integer.MAX_VALUE, true, false) };

    @Getter
    private TanklessFluidPipeNet net;
    private TileEntityTanklessFluidPipe pipe;
    @Getter
    @Nullable private final EnumFacing facing;
    private int simulatedTransfers = 0;
    private final FluidTank testHandler = new FluidTank(Integer.MAX_VALUE);

    public FluidNetHandler(TanklessFluidPipeNet net, TileEntityTanklessFluidPipe pipe, @Nullable EnumFacing facing) {
        this.net = net;
        this.pipe = pipe;
        this.facing = facing;
    }

    public void updateNetwork(TanklessFluidPipeNet net) {
        this.net = net;
    }

    public void updatePipe(TileEntityTanklessFluidPipe pipe) {
        this.pipe = pipe;
    }

    private void copyTransferred() {
        simulatedTransfers = pipe.getTransferredFluids();
    }

    @Override
    public int fill(@Nullable FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) {
            return 0;
        }
        if (net == null || pipe == null || pipe.isInvalid() || pipe.isFaceBlocked(facing)) {
            return 0;
        }

        copyTransferred();
        // noinspection DataFlowIssue
        val pipeCover = pipe.getCoverableImplementation().getCoverAtSide(facing);
        val tileCover = getCoverOnNeighbour(this.pipe, facing);

        boolean pipePump = pipeCover instanceof CoverPump, tilePump = tileCover instanceof CoverPump;
        // abort if there are two pumps
        if (pipePump && tilePump) {
            return 0;
        }

        if (tileCover != null && !checkFillCover(tileCover, false, resource)) {
            return 0;
        }

        return fillFirst(resource, !doFill);
    }

    public static boolean checkFillCover(Cover cover, boolean onPipe, FluidStack stack) {
        if (cover == null) return true;
        if (cover instanceof CoverFluidFilter filter) {
            return (filter.getFilterMode() != FluidFilterMode.FILTER_BOTH &&
                    (filter.getFilterMode() != FluidFilterMode.FILTER_FILL || !onPipe) &&
                    (filter.getFilterMode() != FluidFilterMode.FILTER_DRAIN || onPipe)) || filter.testFluidStack(stack);
        }
        return true;
    }

    public int fillFirst(@NonNull FluidStack resource, boolean simulate) {
        int insertedTotal = 0;
        val remaining = resource.copy();
        for (val route : net.getNetData(pipe.getPipePos(), facing)) {
            int inserted = fill(route, remaining, simulate);
            if (inserted < 0) {
                return 0; // a weakest pipe on this route was destroyed, refuse the fill
            }
            if (inserted > 0) {
                remaining.amount -= inserted;
                insertedTotal += inserted;
            }
            if (remaining.amount <= 0) {
                break;
            }
        }
        return insertedTotal;
    }

    public int fill(@NonNull FluidRoutePath routePath, @NonNull FluidStack stack, boolean simulate) {
        return fill(routePath, stack, simulate, false);
    }

    public int fill(@NonNull FluidRoutePath routePath, @NonNull FluidStack stack, boolean simulate,
                    boolean ignoreLimit) {
        @Range(from = 1, to = Integer.MAX_VALUE)
        int allowed = ignoreLimit ? stack.amount :
                checkTransferable(routePath.getProperties().getThroughput(), stack.amount, simulate);
        if (allowed == 0 || !routePath.matchesFilters(stack) || !routePath.getProperties().test(stack)) {
            return 0;
        }

        val pipeCover = routePath.getTargetPipe().getCoverableImplementation()
                .getCoverAtSide(routePath.getTargetFacing());
        val tileCover = getCoverOnNeighbour(routePath.getTargetPipe(), routePath.getTargetFacing());

        if (pipeCover != null) {
            testHandler.setFluid(stack.copy());
            val fluidHandler = pipeCover.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, testHandler);
            if (fluidHandler == null) {
                testHandler.setFluid(null);
                return 0;
            } else if (fluidHandler != testHandler) {
                // the cover gates how much of the fill it lets through
                val extracted = fluidHandler.drain(allowed, false);
                if (extracted == null || extracted.amount <= 0) {
                    testHandler.setFluid(null);
                    return 0;
                }
                allowed = Math.min(allowed, extracted.amount);
            }
            testHandler.setFluid(null);
        }

        val neighbourHandler = routePath.getHandler();
        if (neighbourHandler == null) {
            return 0;
        }
        if (pipeCover instanceof CoverFluidRegulator regulator &&
                regulator.getPumpMode() == CoverPump.PumpMode.EXPORT) {
            return fillOverRegulator(neighbourHandler, regulator, stack, simulate, allowed, ignoreLimit);
        }
        if (tileCover instanceof CoverFluidRegulator regulator &&
                regulator.getPumpMode() == CoverPump.PumpMode.IMPORT) {
            return fillOverRegulator(neighbourHandler, regulator, stack, simulate, allowed, ignoreLimit);
        }

        return fill(neighbourHandler, stack, simulate, allowed, ignoreLimit);
    }

    private int fillOverRegulator(IFluidHandler handler, CoverFluidRegulator regulator, @NonNull FluidStack stack,
                                  boolean simulate, @Range(from = 1, to = Integer.MAX_VALUE) int allowed,
                                  boolean ignoreLimit) {
        if (!regulator.getFluidFilterContainer().testFluidStack(stack)) {
            return 0;
        }
        return switch (regulator.getTransferMode()) {
            case KEEP_EXACT -> {
                int keep = getTransferRate(regulator, stack);
                int count = keep - countFluid(handler, stack);
                if (count <= 0) {
                    yield 0;
                }
                count = Math.min(allowed, Math.min(stack.amount, count));
                yield fill(handler, stack, simulate, count, ignoreLimit);
            }
            case TRANSFER_EXACT -> {
                int rate = getTransferRate(regulator, stack);
                int count = Math.min(allowed, Math.min(rate, stack.amount));
                if (count <= 0 || fill(handler, stack, true, count, ignoreLimit) != count) {
                    yield 0;
                }
                yield fill(handler, stack, simulate, count, ignoreLimit);
            }
            default -> fill(handler, stack, simulate, allowed, ignoreLimit);
        };
    }

    private int fill(@NonNull IFluidHandler handler, @NonNull FluidStack stack, boolean simulate,
                     @Range(from = 1, to = Integer.MAX_VALUE) int allowed, boolean ignoreLimit) {
        val toInsert = stack.copy();
        toInsert.amount = Math.min(allowed, stack.amount);
        int filled = handler.fill(toInsert, !simulate);
        if (filled > 0 && !ignoreLimit) {
            transfer(simulate, filled);
        }
        return filled;
    }

    private @Range(from = 0, to = Integer.MAX_VALUE) int getTransferRate(@NonNull CoverFluidRegulator regulator,
                                                                         @NonNull FluidStack stack) {
        val fluidFilter = regulator.getFluidFilterContainer().getFilterWrapper().getFluidFilter();
        if (fluidFilter != null) {
            return fluidFilter.getFluidTransferLimit(stack);
        }
        return regulator.getTransferAmount();
    }

    private @Range(from = 0, to = Integer.MAX_VALUE) int countFluid(@NonNull IFluidHandler handler,
                                                                    @NonNull FluidStack stack) {
        int count = 0;
        for (val tankProperties : handler.getTankProperties()) {
            val contents = tankProperties.getContents();
            if (contents != null && contents.isFluidEqual(stack)) {
                count += contents.amount;
            }
        }
        return count;
    }

    public @Nullable Cover getCoverOnNeighbour(TileEntityTanklessFluidPipe pipe, EnumFacing facing) {
        val tile = pipe.getNeighbor(facing);
        if (tile != null) {
            val coverHolder = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER,
                    facing.getOpposite());
            if (coverHolder == null) return null;
            return coverHolder.getCoverAtSide(facing.getOpposite());
        }
        return null;
    }

    private @Range(from = 0, to = Integer.MAX_VALUE) int checkTransferable(int rate, int amount, boolean simulate) {
        int used = simulate ? simulatedTransfers : pipe.getTransferredFluids();
        int windowRemaining = rate * 20 - used;
        // rate & amount should always be > 0, so the clamp bounds are valid
        return Math.clamp(windowRemaining, 0, Math.min(rate, amount));
    }

    private void transfer(boolean simulate, int amount) {
        if (simulate) {
            simulatedTransfers += amount;
        } else {
            pipe.addTransferredFluids(amount);
        }
    }

    @Nullable @Override
    public FluidStack drain(@Nullable FluidStack resource, boolean doDrain) {
        return null;
    }

    @Nullable @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return FAKE_TANK_PROPERTIES;
    }
}
