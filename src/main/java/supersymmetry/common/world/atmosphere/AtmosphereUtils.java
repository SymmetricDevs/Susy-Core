package supersymmetry.common.world.atmosphere;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import supersymmetry.common.event.DimensionBreathabilityHandler;

public class AtmosphereUtils {

    public static BlockPos[] neighbors(BlockPos pos) {
        BlockPos[] result = new BlockPos[6];
        for (int i = 0; i < 6; i++) {
            result[i] = pos.offset(EnumFacing.VALUES[i]);
        }
        return result;
    }

    public static boolean isPosOxygenated(BlockPos pos, World world) {
        return !DimensionBreathabilityHandler.isInDepressurizationHazard(world) || AtmosphereWorldData.get(world).getGraph().getOxygenation(pos) >= 0.1;
    }
}
