package supersymmetry.common.world.atmosphere;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class BlockPosUtil {

    public static BlockPos[] neighbors(BlockPos pos) {
        BlockPos[] result = new BlockPos[6];
        for (int i = 0; i < 6; i++) {
            result[i] = pos.offset(EnumFacing.VALUES[i]);
        }
        return result;
    }
}
