package supersymmetry.api.space;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import supersymmetry.api.util.Quaternion;

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
        double z = r * (sinO * cosWnu + cosO * sinWnu * cosI);
        double y = r * sinWnu * sinI;

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

    public Vec3d computeOrbitalNormal() {
        double cosO = Math.cos(longitudeOfAscendingNodeRad);
        double sinO = Math.sin(longitudeOfAscendingNodeRad);
        double cosI = Math.cos(inclinationRad);
        double sinI = Math.sin(inclinationRad);
        return new Vec3d(sinO * sinI, cosI, -cosO * sinI);
    }

    public static Vec3d rotateAboutAxis(Vec3d v, Vec3d axis, double angle) {
        return Quaternion.fromAxisAngle(axis, angle).rotatePoint(v);
    }

    public static Vec3d rotateToLocalFrame(Vec3d v, Vec3d localUp) {
        Vec3d target = new Vec3d(0, 1, 0);
        double cosA = localUp.dotProduct(target);
        double sinA = Math.sqrt(Math.max(0.0, 1.0 - cosA * cosA));
        if (sinA < 1e-12) {
            if (cosA < 0) return new Vec3d(-v.x, -v.y, -v.z);
            return v;
        }
        return Quaternion.fromAxisAngle(localUp.crossProduct(target), Math.acos(cosA)).rotatePoint(v);
    }

    public static Vec3d toViewDir(Vec3d v, Vec3d localUp) {
        return normalizeSafe(rotateToLocalFrame(v, localUp));
    }

    // Vec3d normalize returns 0 when .length is < 0.001, even tho f64 precision is ~15 digits
    // this took 4 hours to debug
    public static Vec3d normalizeSafe(Vec3d v) {
        double len = v.length();
        if (len < 1e-40) return Vec3d.ZERO;
        return v.scale(1.0 / len);
    }

    public static Vec3d surfacePointToLocalUp(double posX, double posZ, double planetRadius) {
        double scale = 400000.0 * planetRadius;
        double phi = posX * Math.PI / scale;
        double theta = posZ * Math.PI / scale;

        return new Vec3d(
                Math.sin(theta) * Math.cos(phi),
                Math.cos(theta),
                Math.sin(theta) * Math.sin(phi));
    }

    public static double computeSolarAltitude(CelestialObject ground, Vec3d localUp, double worldTime) {
        Star sun = ground.findPrimaryStar();
        if (sun == null) return Double.NaN;
        Vec3d sunPos = computeAbsolutePosition(sun, worldTime);
        Vec3d groundPos = computeAbsolutePosition(ground, worldTime);
        Vec3d relative = sunPos.subtract(groundPos);
        double distAU = relative.length();
        if (distAU < 1e-15) return Double.NaN;
        return relative.dotProduct(localUp) / distAU;
    }

    public static Vec3d getLocalUp(CelestialObject ground, double x, double z, double worldTime) {
        Vec3d up = surfacePointToLocalUp(x, z, ground.getRadius());
        Vec3d axis = ground.getRotationAxis();
        if (axis == null) return up;
        return rotateAboutAxis(up, axis, -ground.getRotationAngle(worldTime));
    }

    public static float getSunBrightness(CelestialObject ground, Vec3d localUp, double worldTime) {
        double solarAltitude = computeSolarAltitude(ground, localUp, worldTime);
        if (Double.isNaN(solarAltitude)) return 0.0f;
        return (float) MathHelper.clamp(solarAltitude * 4.0, 0.0, 1.0);
    }

    public static boolean isEclipse(CelestialObject ground, double worldTime) {
        CelestialObject sun = ground.findPrimaryStar();
        if (sun == null) return false;

        Vec3d sunPos = computeAbsolutePosition(sun, worldTime);
        Vec3d groundPos = computeAbsolutePosition(ground, worldTime);
        Vec3d toSun = sunPos.subtract(groundPos);
        double distToSun = toSun.length();
        if (distToSun < 1e-15) return false;
        Vec3d toSunDir = toSun.normalize();
        double angularRadiusSun = sun.getRadiusAU() / distToSun;

        for (CelestialObject child : ground.getChildBodies()) {
            if (isOccluding(child, groundPos, toSunDir, distToSun, angularRadiusSun, worldTime))
                return true;
        }

        CelestialObject parent = ground.getParentBody();
        if (parent != null && !(parent instanceof Star)) {
            if (isOccluding(parent, groundPos, toSunDir, distToSun, angularRadiusSun, worldTime))
                return true;
        }

        return false;
    }

    private static boolean isOccluding(
                                       CelestialObject occluder,
                                       Vec3d observerPos,
                                       Vec3d toSunDir,
                                       double distToSun,
                                       double angularRadiusSun,
                                       double worldTime) {
        Vec3d oPos = computeAbsolutePosition(occluder, worldTime);
        Vec3d toOccluder = oPos.subtract(observerPos);
        double distToOccluder = toOccluder.length();
        if (distToOccluder < 1e-15) return false;

        double projDist = toOccluder.dotProduct(toSunDir);
        if (projDist <= 0 || projDist >= distToSun) return false;

        Vec3d perp = toOccluder.subtract(toSunDir.scale(projDist));
        double distPerp = perp.length();

        double angularRadiusOccluder = occluder.getRadiusAU() / distToOccluder;
        double angularSeparation = distPerp / distToOccluder;

        return angularSeparation < angularRadiusOccluder + angularRadiusSun;
    }
}
