package supersymmetry.common.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import supersymmetry.api.space.PlanetGrowthPolicy;
import supersymmetry.common.world.atmosphere.AtmosphereUtils;

/** Resolves the local environment only after a planet policy would otherwise deny growth. */
public final class PlanetGrowthRules {

    private PlanetGrowthRules() {}

    public static boolean suppressRandomTick(World world, BlockPos pos, IBlockState state) {
        PlanetGrowthPolicy policy = PlanetGrowthPolicy.forDimension(world.provider.getDimension());
        if (!policy.suppressRandomTick(state.getBlock())) {
            return false;
        }
        return !allowsAtmosphereGrowth(policy, world, pos, state);
    }

    public static boolean suppressBonemeal(EntityPlayer player, World world, BlockPos pos, IBlockState state) {
        PlanetGrowthPolicy policy = PlanetGrowthPolicy.forDimension(world.provider.getDimension());
        if (!policy.suppressBonemeal(player)) {
            return false;
        }
        return !allowsAtmosphereGrowth(policy, world, pos, state);
    }

    private static boolean allowsAtmosphereGrowth(PlanetGrowthPolicy policy, World world, BlockPos pos,
                                                  IBlockState state) {
        // The clarifier uses this same query and threshold; saved atmosphere data is server-owned.
        // A fake world without MapStorage cannot establish a local atmosphere.
        return policy.requiresAtmosphere() && !world.isRemote && world.getMapStorage() != null &&
                AtmosphereUtils.isPosOxygenated(getAtmospherePosition(pos, state), world);
    }

    static BlockPos getAtmospherePosition(BlockPos pos, IBlockState state) {
        // Full soil blocks do not belong to an air region. Plants occupy the air cell itself.
        return state.isFullCube() ? pos.up() : pos;
    }
}
