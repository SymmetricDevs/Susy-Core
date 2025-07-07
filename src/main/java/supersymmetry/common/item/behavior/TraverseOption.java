package supersymmetry.common.item.behavior;

import gregtech.api.pipenet.tile.IPipeTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import static supersymmetry.common.item.behavior.TraverseOption.Lambdas.*;

public enum TraverseOption {

    CONNECTING(FIND_TO_CONNECT, CONNECTOR),
    DISCONNECTING(FIND_CONNECTED, DISCONNECTOR),
    BLOCKING(FIND_CONNECTED, BLOCKER),
    UNBLOCKING(FIND_CONNECTED, UNBLOCKER),
    // TODO: Painter
    ;

    private final PathFinder pathFinder;
    private final PipeOperator pipeOperator;

    TraverseOption(PathFinder pathFinder, PipeOperator pipeOperator) {
        this.pathFinder = pathFinder;
        this.pipeOperator = pipeOperator;
    }

    public EnumFacing findNext(EnumFacing from, IPipeTile<?, ?> pipe) {
        return pathFinder.findNext(from, pipe);
    }

    public void operate(EnumFacing from, IPipeTile<?, ?> self, IPipeTile<?, ?> other, boolean reverse) {
        pipeOperator.operate(from, self, other, reverse);
    }

    @FunctionalInterface
    private interface PathFinder {
        EnumFacing findNext(EnumFacing from, IPipeTile<?, ?> pipe);
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
            EnumFacing ret = null;
            int count = 0;

            for (EnumFacing facing : EnumFacing.values()) {
                if (facing == from) continue;
                TileEntity other = pipe.getNeighbor(facing);
                if (other instanceof IPipeTile<?, ?> otherPipe
                        && pipe.getClass().isAssignableFrom(other.getClass())) {
                    if (ret == null && otherPipe.getConnections() == 0) {
                        ret = facing;
                    }
                    count++;
                }
            }

            /// Note that ret can still be null even if count == 0
            return count == 1 ? ret : null;
        };

        /// Returns the other facing this pipe is connected to, for DISCONNECTING operation type
        /// Returns null if:
        ///   1) The pipe is not connected to any other side
        ///   2) The pipe is connected to more than 1 side among other sides
        static final PathFinder FIND_CONNECTED = (from, pipe) -> {
            int former = from.getIndex();
            int next = -1;
            int connections = pipe.getConnections();
            int count = 0;

            for (int current = 0; current < 6; current++) {
                if (current == former) continue;
                if ((connections & 1 << current) > 0) {
                    count++;
                    if (count > 1) {
                        return null;
                    } else {
                        next = current;
                    }
                }
            }
            return next != -1 ? EnumFacing.byIndex(next) : null;
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
