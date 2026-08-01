package supersymmetry.common.entities;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * The rocket built and launched by the lunar launch complex. Lunar escape velocity is low enough that the vehicle can
 * be much smaller than the Soyuz, so this shares all of that rocket's behaviour (blueprint, cargo, fuel, flight and
 * success calculation) and only shrinks the hull to fit the ICBM model.
 */
public class EntityLunarRocket extends EntityBlueprintRocket {

    /** A single engine bell on the centreline, unlike the Soyuz's four boosters. */
    private static final double[][] ENGINE_OFFSETS = { { 0, 0 } };

    public EntityLunarRocket(World worldIn) {
        super(worldIn);
    }

    public EntityLunarRocket(World worldIn, double x, double y, double z, float rotationYaw) {
        super(worldIn, x, y, z, rotationYaw);
    }

    public EntityLunarRocket(World worldIn, Vec3d pos, float rotationYaw) {
        super(worldIn, pos.x, pos.y, pos.z, rotationYaw);
    }

    // The model is 35 blocks tall with a radius of 1.5.
    @Override
    protected float getRocketWidth() {
        return 3F;
    }

    @Override
    protected float getRocketHeight() {
        return 35F;
    }

    @Override
    protected double getCollisionRadius() {
        return 1.6;
    }

    @Override
    protected double getModelRadius() {
        return 1.6;
    }

    @Override
    protected double getBoardingWindowMin() {
        return 28;
    }

    @Override
    protected double getBoardingWindowMax() {
        return 33.5;
    }

    @Override
    public double getMountedYOffset() {
        return 29D;
    }

    @Override
    protected double[][] getEngineOffsets() {
        return ENGINE_OFFSETS;
    }

    @Override
    protected float getExplosionStrength() {
        return 40; // Smaller than the Soyuz, but still needs to cover a passenger sat at 29 blocks
    }
}
