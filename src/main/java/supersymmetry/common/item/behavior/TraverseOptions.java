package supersymmetry.common.item.behavior;

import gregtech.api.pipenet.tile.IPipeTile;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;

import static supersymmetry.common.item.behavior.TraverseOptions.Lambdas.*;

public enum TraverseOptions implements ITraverseOption {

    CONNECTING(FIND_TO_CONNECT, CONNECTOR),
    DISCONNECTING(FIND_CONNECTED, DISCONNECTOR),
    BLOCKING(FIND_CONNECTED, BLOCKER),
    UNBLOCKING(FIND_CONNECTED, UNBLOCKER),
    ;

    public static final Int2ObjectArrayMap<ITraverseOption> COLORING = new Int2ObjectArrayMap<>(1 + EnumDyeColor.values().length);

    static {
        /// -1 for default color
        for (int i = -1; i <= EnumDyeColor.values().length; i++) {
            final int color = i;

            COLORING.put(color, new ITraverseOption() {

                @Override
                public List<EnumFacing> findNext(EnumFacing from, IPipeTile<?, ?> pipe) {
                    return FIND_ALL_CONNECTED.findNext(from, pipe);
                }

                @Override
                public void operate(EnumFacing from, IPipeTile<?, ?> self, IPipeTile<?, ?> other, boolean reverse) {
                    self.setPaintingColor(color);
                }
            });
        }
    }

    private final PathFinder pathFinder;
    private final PipeOperator pipeOperator;

    TraverseOptions(PathFinder pathFinder, PipeOperator pipeOperator) {
        this.pathFinder = pathFinder;
        this.pipeOperator = pipeOperator;
    }

    @Override
    public List<EnumFacing> findNext(EnumFacing from, IPipeTile<?, ?> pipe) {
        return pathFinder.findNext(from, pipe);
    }

    @Override
    public void operate(EnumFacing from, IPipeTile<?, ?> self, IPipeTile<?, ?> other, boolean reverse) {
        pipeOperator.operate(from, self, other, reverse);
    }

    @FunctionalInterface
    private interface PathFinder {
        List<EnumFacing> findNext(EnumFacing from, IPipeTile<?, ?> pipe);
    }

    @FunctionalInterface
    private interface PipeOperator {
        void operate(EnumFacing facingToOther, IPipeTile<?, ?> self, IPipeTile<?, ?> other, boolean reverse);
    }

    static class Lambdas {
        /// Returns the other facing this pipe has a neighbouring pipe to connect to, for CONNECTING operation type
        /// Returns null if:
        ///   1) The pipe is cannot find any pipe to connect
        ///   2) The pipe is connected to more than 1 side alr
        static final PathFinder FIND_TO_CONNECT = (from, pipe) -> {
            List<EnumFacing> ret = new ArrayList<>(1);

            for (EnumFacing facing : EnumFacing.values()) {
                if (facing == from) continue;
                TileEntity other = pipe.getNeighbor(facing);
                if (other instanceof IPipeTile<?, ?> otherPipe
                        && pipe.getClass().isAssignableFrom(other.getClass())
                        && otherPipe.getConnections() == 0) {
                    if (ret.isEmpty()) {
                        /// Adding the first candidate
                        ret.add(facing);
                    } else {
                        /// More than one candidate found, returning empty
                        ret.clear();
                        return ret;
                    }
                }
            }
            // Only one candidate found, return it
            return ret;
        };

        /// Returns the other facing this pipe is connected to, for DISCONNECTING operation type
        /// Returns null if:
        ///   1) The pipe is not connected to any other side
        ///   2) The pipe is connected to more than 1 side among other sides
        static final PathFinder FIND_CONNECTED = (from, pipe) -> {
            List<EnumFacing> ret = new ArrayList<>(1);

            for (EnumFacing facing : EnumFacing.values()) {
                if (facing == from) continue;
                if (pipe.isConnected(facing)) {
                    if (ret.isEmpty()) {
                        /// Adding the first candidate
                        ret.add(facing);
                    } else {
                        /// More than one candidate found, returning empty
                        ret.clear();
                        return ret;
                    }
                }
            }
            // Only one candidate found, return it
            return ret;
        };

        /// Returns *ALL* the other facings this pipe is connected to
        static final PathFinder FIND_ALL_CONNECTED = (from, pipe) -> {
            List<EnumFacing> ret = new ArrayList<>(5);
            for (EnumFacing facing : EnumFacing.values()) {
                if (facing == from) continue;
                if (pipe.isConnected(facing)) {
                    ret.add(facing);
                }
            }
            return ret;
        };


        /// Connects this pipe with the other
        static final PipeOperator CONNECTOR = (facingToOther, self, other, reverse) -> self.setConnection(facingToOther, true, false);

        /// Disconnects this pipe with the other
        static final PipeOperator DISCONNECTOR = (facingToOther, self, other, reverse) -> self.setConnection(facingToOther, false, false);

        /// Blocks this pipe with the other
        static final PipeOperator BLOCKER = (facingToOther, self, other, reverse) -> {
            if (reverse) {
                other.setFaceBlocked(facingToOther.getOpposite(), true);
            } else {
                self.setFaceBlocked(facingToOther, true);
            }
        };

        /// Blocks this pipe with the other
        static final PipeOperator UNBLOCKER = (facingToOther, self, other, reverse) -> {
            if (reverse) {
                other.setFaceBlocked(facingToOther.getOpposite(), false);
            } else {
                self.setFaceBlocked(facingToOther, false);
            }
        };
    }
}
