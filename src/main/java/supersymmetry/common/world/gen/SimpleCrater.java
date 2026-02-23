package supersymmetry.common.world.gen;

import net.minecraft.block.state.IBlockState;

/**
 * A simple (small) impact crater with a bowl-shaped profile.
 * Diameter is typically below 80 blocks.
 */
public class SimpleCrater extends CraterBase {

    public SimpleCrater(IBlockState stone, IBlockState breccia, IBlockState impactMelt, IBlockState impactEjecta) {
        super(stone, breccia, impactMelt, impactEjecta, 0L);
    }

    @Override
    protected int computeDepth(int radius) {
        return (int) (radius * 0.6);
    }

    /**
     * Bowl-shaped profile: depth falls off as distance squared.
     */
    @Override
    protected int computeFloorDepth(int maxDepth, double normalizedDist) {
        return (int) (maxDepth * (1 - normalizedDist * normalizedDist));
    }
}
