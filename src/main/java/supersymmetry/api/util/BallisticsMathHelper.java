package supersymmetry.api.util;

import static java.lang.Math.sqrt;

import net.minecraft.util.math.BlockPos;

public class BallisticsMathHelper {

    /**
     * <a href="https://en.wikipedia.org/wiki/Euclidean_distance">Bro, it's just the Euclidean distance in 3D bro</a>
     *
     * @param x1 the launch X coordinate
     * @param y1 the launch Y coordinate
     * @param z1 the launch Z coordinate
     * @param x2 the target X coordinate
     * @param y2 the target Y coordinate
     * @param z2 the target Z coordinate
     * @return The Euclidean distance in 3D bro
     */
    public static double distanceSquared(double x1, double y1, double z1, double x2, double y2, double z2) {
        return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1);
    }

    /**
     * Computes the minimum velocity required to reach the target on an ideal ballistic trajectory
     * under constant gravitational acceleration.
     *
     * @param x1 the launch X coordinate
     * @param y1 the launch Y coordinate
     * @param z1 the launch Z coordinate
     * @param x2 the target X coordinate
     * @param y2 the target Y coordinate
     * @param z2 the target Z coordinate
     * @param a  the magnitude of gravitational acceleration in m*s^-2 (must be positive)
     * @return the minimum launch velocity necessary for a ballistic solution to exist
     */
    public static double minimumVelocity(double x1, double y1, double z1, double x2, double y2, double z2, double a) {
        double D = sqrt(distanceSquared(x1, y1, z1, x2, y2, z2));
        return sqrt(a * (D - y2 + y1));
    }

    /**
     * Computes the time of flight for an ideal ballistic trajectory between two
     * points under constant gravitational acceleration.
     * The coordinate system uses the positive Y-axis as the vertical direction.
     * Gravity acts in the negative Y direction with magnitude {@code g}. Air
     * resistance, the Earth's curvature, the Earth's rotation and General Relativity are ignored (for now 😈).
     * If {@code lowPath} is {@code true}, the lower-angle trajectory is used.
     * Otherwise, the higher-angle trajectory is used. If the specified launch
     * speed is insufficient to reach the target, this method returns {@code -1}.
     *
     * @param x1      the launch X coordinate
     * @param y1      the launch Y coordinate
     * @param z1      the launch Z coordinate
     * @param x2      the target X coordinate
     * @param y2      the target Y coordinate
     * @param z2      the target Z coordinate
     * @param v       the launch speed in m/s
     * @param a       the magnitude of gravitational acceleration in m*s^-2 (must be positive)
     * @param lowPath {@code true} for the lower-angle trajectory,
     *                {@code false} for the higher-angle trajectory
     * @return the flight time in seconds, or {@code -1} if no ballistic trajectory
     *         exists for the specified launch speed
     */
    public static double flightTime(double x1, double y1, double z1, double x2, double y2, double z2, double v,
                                    double a,
                                    boolean lowPath) {
        if (v < minimumVelocity(x1, y1, z1, x2, y2, z2, a)) return -1;
        double dy = y2 - y1;
        double term = a * dy + v * v;
        double disc = sqrt(term * term - a * a * distanceSquared(x1, y1, z1, x2, y2, z2));
        if (lowPath) {
            return sqrt(2 * (term - disc)) / a;
        } else {
            return sqrt(2 * (term + disc)) / a;
        }
    }

    /**
     * Computes the time of flight for an ideal ballistic trajectory between two
     * points under constant gravitational acceleration.
     * The coordinate system uses the positive Y-axis as the vertical direction.
     * Gravity acts in the negative Y direction with magnitude {@code g}. Air
     * resistance, the Earth's curvature, the Earth's rotation and General Relativity are ignored (for now 😈).
     * If {@code lowPath} is {@code true}, the lower-angle trajectory is used.
     * Otherwise, the higher-angle trajectory is used. If the specified launch
     * speed is insufficient to reach the target, this method returns {@code -1}.
     *
     * @param startPos  Launch position
     * @param targetPos Target position
     * @param a         the magnitude of gravitational acceleration in m*s^-2 (must be positive)
     * @param lowPath   {@code true} for the lower-angle trajectory,
     *                  {@code false} for the higher-angle trajectory
     * @return the flight time in seconds, or {@code -1} if no ballistic trajectory
     *         exists for the specified launch speed
     */
    public static double flightTime(BlockPos startPos, BlockPos targetPos, double v, double a, boolean lowPath) {
        return flightTime(
                startPos.getX(),
                startPos.getY(),
                startPos.getZ(),
                targetPos.getX(),
                targetPos.getY(),
                targetPos.getZ(),
                v,
                a,
                lowPath);
    }
}
