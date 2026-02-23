package supersymmetry.common.world.gen;

import net.minecraft.block.state.IBlockState;

/**
 * A complex (large) impact crater with a flat floor and raised central peak region.
 * Diameter is typically 80 blocks or more.
 */
public class ComplexCrater extends CraterBase {

    private static final double FLAT_FLOOR_RADIUS = 0.3;

    public ComplexCrater(IBlockState stone, IBlockState breccia, IBlockState impactMelt, IBlockState impactEjecta) {
        super(stone, breccia, impactMelt, impactEjecta, 987654321L);
    }

    @Override
    protected int computeDepth(int radius) {
        return (int) (radius * 0.4);
    }

    /**
     * Flat-floored profile: fully excavated within the inner zone,
     * then shallowing toward the rim.
     */
    @Override
    protected int computeFloorDepth(int maxDepth, double normalizedDist) {
        if (normalizedDist < FLAT_FLOOR_RADIUS) {
            return maxDepth;
        }
        double outerFraction = (normalizedDist - FLAT_FLOOR_RADIUS) / (1.0 - FLAT_FLOOR_RADIUS);
        return (int) (maxDepth * (1 - Math.pow(outerFraction, 1.5)));
    }

    @Override
    protected int getMinCraterDiameter() {
        return 80;
    }

    @Override
    protected double getCraterProbability() {
        return 0.003;
    } // was 0.015, ~5x rarer
}
