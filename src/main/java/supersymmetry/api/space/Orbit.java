package supersymmetry.api.space;

import net.minecraft.util.math.Vec3d;

public class Orbit {

    public static final double EARTH_RADIUS_AU = 0.0000425;

    private static final int MAX_KEPLER_ITERATIONS = 64;
    private static final double KEPLER_TOLERANCE = 1e-12;

    public final double semiMajorAxisAU;
    public final double eccentricity;
    public final double inclinationRad;
    public final double longitudeOfAscendingNodeRad;
    public final double argumentOfPeriapsisRad;
    public final double meanAnomalyAtEpochRad;
    public final long epochTicks;
    public final long periodTicks;

    public Orbit(double semiMajorAxisAU, double eccentricity, double inclinationRad,
                 double longitudeOfAscendingNodeRad, double argumentOfPeriapsisRad,
                 double meanAnomalyAtEpochRad, long epochTicks, long periodTicks) {
        this.semiMajorAxisAU = semiMajorAxisAU;
        this.eccentricity = eccentricity;
        this.inclinationRad = inclinationRad;
        this.longitudeOfAscendingNodeRad = longitudeOfAscendingNodeRad;
        this.argumentOfPeriapsisRad = argumentOfPeriapsisRad;
        this.meanAnomalyAtEpochRad = meanAnomalyAtEpochRad;
        this.epochTicks = epochTicks;
        this.periodTicks = periodTicks;
    }

    public Vec3d computeRelativePosition(double worldTime) {
        double M = meanAnomalyAtEpochRad + 2.0 * Math.PI * (worldTime - epochTicks) / periodTicks;
        M %= 2.0 * Math.PI;

        double E = solveKepler(M, eccentricity);

        double r = semiMajorAxisAU * (1.0 - eccentricity * Math.cos(E));
        double denom = 1.0 - eccentricity * Math.cos(E);
        double cosNu = (Math.cos(E) - eccentricity) / denom;
        double sinNu = Math.sqrt(1.0 - eccentricity * eccentricity) * Math.sin(E) / denom;

        double nu = Math.atan2(sinNu, cosNu);
        double cosWnu = Math.cos(argumentOfPeriapsisRad + nu);
        double sinWnu = Math.sin(argumentOfPeriapsisRad + nu);

        double cosO = Math.cos(longitudeOfAscendingNodeRad);
        double sinO = Math.sin(longitudeOfAscendingNodeRad);
        double cosI = Math.cos(inclinationRad);
        double sinI = Math.sin(inclinationRad);

        double x = r * (cosO * cosWnu - sinO * sinWnu * cosI);
        double y = r * (sinO * cosWnu + cosO * sinWnu * cosI);
        double z = r * sinWnu * sinI;

        return new Vec3d(x, y, z);
    }

    public static Vec3d computeAbsolutePosition(CelestialObject body, double worldTime) {
        if (body.getParentBody() == null) return Vec3d.ZERO;
        Vec3d parentPos = computeAbsolutePosition(body.getParentBody(), worldTime);
        Orbit orbit = CelestialOrbitRegistry.get(body);
        return orbit == null ? parentPos : parentPos.add(orbit.computeRelativePosition(worldTime));
    }

    private static double solveKepler(double M, double e) {
        double E = M;
        for (int i = 0; i < MAX_KEPLER_ITERATIONS; i++) {
            double dE = (E - e * Math.sin(E) - M) / (1.0 - e * Math.cos(E));
            E -= dE;
            if (Math.abs(dE) < KEPLER_TOLERANCE) break;
        }
        return E;
    }
}
