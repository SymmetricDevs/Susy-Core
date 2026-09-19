package supersymmetry.common.pipelike.tanklessfluid.net;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import org.jetbrains.annotations.Nullable;

import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.util.GTUtility;
import gregtech.common.covers.CoverFluidFilter;
import gregtech.common.covers.CoverShutter;
import gregtech.common.covers.FluidFilterMode;
import lombok.val;
import supersymmetry.api.unification.material.properties.TanklessFluidPipeProperties;
import supersymmetry.common.pipelike.tanklessfluid.tile.TileEntityTanklessFluidPipe;

public class TanklessFluidNetWalker extends PipeNetWalker<TileEntityTanklessFluidPipe> {

    public static List<FluidRoutePath> createNetData(World world, BlockPos sourcePipe, EnumFacing faceToSourceHandler) {
        if (!(world.getTileEntity(sourcePipe) instanceof TileEntityTanklessFluidPipe)) {
            return null;
        }
        val walker = new TanklessFluidNetWalker(world, sourcePipe, 1, new ArrayList<>(), null);
        walker.sourcePipe = sourcePipe;
        walker.facingToHandler = faceToSourceHandler;
        walker.traversePipeNet();
        return walker.isFailed() ? null : walker.routes;
    }

    private TanklessFluidPipeProperties minProperties;
    private final List<FluidRoutePath> routes;
    private final List<Predicate<FluidStack>> filters = new ArrayList<>();
    private final EnumMap<EnumFacing, List<Predicate<FluidStack>>> nextFilters = new EnumMap<>(EnumFacing.class);
    private BlockPos sourcePipe;
    private EnumFacing facingToHandler;

    protected TanklessFluidNetWalker(World world, BlockPos sourcePipe, int distance, List<FluidRoutePath> routes,
                                     TanklessFluidPipeProperties properties) {
        super(world, sourcePipe, distance);
        this.routes = routes;
        this.minProperties = properties;
    }

    @Override
    protected PipeNetWalker<TileEntityTanklessFluidPipe> createSubWalker(World world, EnumFacing facingToNextPos,
                                                                         BlockPos nextPos, int walkedBlocks) {
        val walker = new TanklessFluidNetWalker(world, nextPos, walkedBlocks, routes, minProperties);
        walker.facingToHandler = facingToHandler;
        walker.sourcePipe = sourcePipe;
        walker.filters.addAll(filters);

        val moreFilters = nextFilters.get(facingToNextPos);
        if (moreFilters != null && !moreFilters.isEmpty()) {
            walker.filters.addAll(moreFilters);
        }
        return walker;
    }

    @Override
    protected void checkPipe(TileEntityTanklessFluidPipe pipeTile, BlockPos pos) {
        for (val filters : nextFilters.values()) {
            if (!filters.isEmpty()) {
                this.filters.addAll(filters);
            }
        }
        nextFilters.clear();

        val pipeProperties = pipeTile.getNodeData();
        if (minProperties == null) {
            minProperties = pipeProperties;
        } else {
            val old = minProperties;
            minProperties = new TanklessFluidPipeProperties(
                    Math.min(old.getMaxFluidTemperature(), pipeProperties.getMaxFluidTemperature()),
                    Math.min(old.getThroughput(), pipeProperties.getThroughput()),
                    old.isGasProof() && pipeProperties.isGasProof(),
                    old.isAcidProof() && pipeProperties.isAcidProof(),
                    old.isCryoProof() && pipeProperties.isCryoProof(),
                    old.isPlasmaProof() && pipeProperties.isPlasmaProof(),
                    old.getResistance() + pipeProperties.getResistance());
            for (val attribute : old.getContainedAttributes()) {
                if (pipeProperties.canContain(attribute)) {
                    minProperties.setCanContain(attribute, true);
                }
            }
        }
    }

    @Override
    protected void checkNeighbour(TileEntityTanklessFluidPipe pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour,
                                  @Nullable TileEntity neighbourTile) {
        if (neighbourTile == null ||
                (GTUtility.arePosEqual(pipePos, sourcePipe) && faceToNeighbour == facingToHandler)) {
            return;
        }
        val handler = neighbourTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                faceToNeighbour.getOpposite());
        if (handler != null) {
            val filters = new ArrayList<>(this.filters);
            val moreFilters = nextFilters.get(faceToNeighbour);
            if (moreFilters != null && !moreFilters.isEmpty()) {
                filters.addAll(moreFilters);
            }
            routes.add(new FluidRoutePath(pipeTile, faceToNeighbour, getWalkedBlocks(), minProperties, filters));
        }
    }

    @Override
    protected Class<TileEntityTanklessFluidPipe> getBasePipeClass() {
        return TileEntityTanklessFluidPipe.class;
    }

    @Override
    protected boolean isValidPipe(TileEntityTanklessFluidPipe currentPipe, TileEntityTanklessFluidPipe neighbourPipe,
                                  BlockPos pipePos,
                                  EnumFacing faceToNeighbour) {
        val thisCover = currentPipe.getCoverableImplementation().getCoverAtSide(faceToNeighbour);
        val neighbourCover = neighbourPipe.getCoverableImplementation().getCoverAtSide(faceToNeighbour.getOpposite());
        List<Predicate<FluidStack>> filters = new ArrayList<>();
        if (thisCover instanceof CoverShutter shutter) {
            filters.add(_ -> !shutter.isWorkingEnabled());
        } else if (thisCover instanceof CoverFluidFilter fluidFilter &&
                fluidFilter.getFilterMode() != FluidFilterMode.FILTER_FILL) {
                    filters.add(fluidFilter::testFluidStack);
                }
        if (neighbourCover instanceof CoverShutter shutter) {
            filters.add(_ -> !shutter.isWorkingEnabled());
        } else if (neighbourCover instanceof CoverFluidFilter fluidFilter &&
                fluidFilter.getFilterMode() != FluidFilterMode.FILTER_DRAIN) {
                    filters.add(fluidFilter::testFluidStack);
                }
        if (!filters.isEmpty()) {
            nextFilters.put(faceToNeighbour, filters);
        }
        return true;
    }
}
