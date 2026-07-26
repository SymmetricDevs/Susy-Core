package supersymmetry.common.world.atmosphere;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import supersymmetry.api.metatileentity.multiblock.FluidRenderRecipeMapMultiBlock;
import supersymmetry.api.recipes.properties.AtmosphereProperty;

public class AtmosphereUtils {

    public static BlockPos[] neighbors(BlockPos pos) {
        BlockPos[] result = new BlockPos[6];
        for (int i = 0; i < 6; i++) {
            result[i] = pos.offset(EnumFacing.VALUES[i]);
        }
        return result;
    }

    public static boolean isPosOxygenated(BlockPos pos, World world) {
        return AtmosphereWorldData.get(world)
                .getGraph()
                .getOxygenation(pos) >= 0.1;
    }
}
