package supersymmetry.common.faction;

import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockTripWire;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.state.IBlockState;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class FactionTrapHazardEvaluator {
    public static PathNodeType getHazardNodeType(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);

        if (state.getBlock() instanceof BlockBasePressurePlate) {
            return PathNodeType.BLOCKED;
        }

        if (state.getBlock() instanceof BlockTripWire ||
                state.getBlock() instanceof BlockTripWireHook) {
            return PathNodeType.BLOCKED;
        }

        BlockPos posBelow = pos.down();

        IBlockState stateBelow = world.getBlockState(posBelow);
        if (stateBelow.getBlock() instanceof BlockTrapDoor) {
            return PathNodeType.BLOCKED;
        }
        return null;
    }
}
