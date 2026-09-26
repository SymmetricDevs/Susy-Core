package supersymmetry.common.world.atmosphere;

import java.util.Collection;

import gregtech.api.cover.CoverHolder;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import net.minecraft.block.material.Material;
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
        return !DimensionBreathabilityHandler.isInDepressurizationHazard(world) ||
                AtmosphereWorldData.get(world).getGraph().getOxygenation(pos) >= 0.1;
    }

    /**
     * Whether air can occupy / flow through the given position. Positions below the world are treated as solid.
     */
    public static boolean isPassable(World world, BlockPos pos) {
        return pos.getY() >= 0 && (!world.getBlockState(pos).isFullCube() &&
                (!(world.getTileEntity(pos) instanceof IPipeTile tile) || !isCovered(tile)));
    }

    public static boolean isCovered(IPipeTile pipe) {
        if (pipe == null) {
            return false;
        }
        for (EnumFacing side: EnumFacing.VALUES) {
            if (pipe.getCoverableImplementation().getCoverAtSide(side) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether the given position is open to vacuum (air there escapes to space).
     */
    public static boolean isExposedToSky(World world, BlockPos pos) {
        return pos.getY() >= world.getHeight() || world.canSeeSky(pos);
    }

    /**
     * Whether the block at the given position blocks air flow but is not air-tight (e.g. sand, regolith).
     */
    public static boolean isPorous(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.SAND;
    }

    /**
     * Whether air reaching the given position escapes to vacuum: either the position is passable and exposed to
     * the sky, or it is a porous block touching such a position. The position must be loaded.
     */
    public static boolean isVent(World world, BlockPos pos) {
        if (pos.getY() >= world.getHeight())
            return true;
        if (pos.getY() < 0)
            return false;
        if (isPassable(world, pos))
            return world.canSeeSky(pos);
        if (!isPorous(world, pos))
            return false;
        for (BlockPos nb : neighbors(pos)) {
            if (nb.getY() >= world.getHeight())
                return true;
            if (world.isBlockLoaded(nb) && isPassable(world, nb) && world.canSeeSky(nb))
                return true;
        }
        return false;
    }

    public static int[] toIntArray(Collection<BlockPos> positions) {
        int[] coords = new int[positions.size() * 3];
        int i = 0;
        for (BlockPos pos : positions) {
            coords[i++] = pos.getX();
            coords[i++] = pos.getY();
            coords[i++] = pos.getZ();
        }
        return coords;
    }

    public static void fromIntArray(int[] coords, Collection<BlockPos> out) {
        for (int i = 0; i + 2 < coords.length; i += 3) {
            out.add(new BlockPos(coords[i], coords[i + 1], coords[i + 2]));
        }
    }
}
