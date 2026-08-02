package supersymmetry.api.space;

import static supersymmetry.common.rocketry.SuccessCalculation.ESCAPE_VELOCITY_CONSTANT;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.util.math.Vec3d;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CelestialObject {

    private String translationKey;

    private double mass; // Normalized by Earth's mass
    private double posT;
    private double posX;
    private double posY;
    private double posZ;
    private double radius = 1; // Normalized by Earth's radius

    private CelestialObject parentBody;
    private CelestialBodyType celestialBodyType;

    private Vec3d rotationAxisEcl;
    private double rotationPeriodTicks;

    private List<CelestialObject> childBodies = new ObjectArrayList<>();

    public CelestialObject(String translationKey, double posT, double posX, double posY, double posZ, double mass,
                           CelestialBodyType celestialBodyType, @Nullable CelestialObject parentBody) {
        this.translationKey = translationKey;
        this.mass = mass;
        this.posT = posT;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.parentBody = parentBody;
        this.celestialBodyType = celestialBodyType;

        if (parentBody != null) {
            parentBody.addChildBody(this);
        }
    }

    public double getMass() {
        return mass;
    }

    public double getPosT() {
        return posT;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public double getRadius() {
        return radius;
    }

    public double getEscapeVelocity() {
        return ESCAPE_VELOCITY_CONSTANT * Math.sqrt(mass / radius);
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getRadiusAU() {
        return radius * Orbit.EARTH_RADIUS_AU;
    }

    @Nullable
    public CelestialObject getParentBody() {
        return parentBody;
    }

    public CelestialBodyType getCelestialBodyType() {
        return celestialBodyType;
    }

    public void addChildBody(CelestialObject body) {
        childBodies.add(body);
    }

    public List<CelestialObject> getChildBodies() {
        return childBodies;
    }

    public String getTranslationKey() {
        return "susy." + translationKey;
    }

    public Planetoid getPlanetarySystem() {
        if (this.getParentBody() instanceof Star && this instanceof Planetoid) {
            return (Planetoid) this;
        } else if (this.getParentBody() != null) {
            return this.getParentBody().getPlanetarySystem();
        }
        return null;
    }

    public StarSystem getStarSystem() {
        if (this instanceof StarSystem) {
            return (StarSystem) this;
        } else if (this.getParentBody() != null) {
            return this.getParentBody().getStarSystem();
        }
        return null;
    }

    public static Star findPrimaryStar(CelestialObject body) {
        return Stream.iterate(body, Objects::nonNull, CelestialObject::getParentBody)
                .filter(Star.class::isInstance)
                .map(Star.class::cast)
                .findFirst()
                .orElse(null);
    }

    public static Vec3d surfacePointToLocalUp(double posX, double posZ, double planetRadius) {
        double scale = 400000.0 * planetRadius;
        double phi = posX * Math.PI / scale;
        double lat = posZ * Math.PI / scale;
        double theta = Math.PI / 2.0 - lat;

        return new Vec3d(
                Math.sin(theta) * Math.cos(phi),
                Math.sin(theta) * Math.sin(phi),
                Math.cos(theta));
    }

    public Vec3d getRotationAxisEcl() {
        return rotationAxisEcl;
    }

    public CelestialObject setRotationAxisEcl(Vec3d rotationAxisEcl) {
        this.rotationAxisEcl = rotationAxisEcl;
        return this;
    }

    public double getRotationPeriodTicks() {
        return rotationPeriodTicks;
    }

    public CelestialObject setRotationPeriodTicks(double rotationPeriodTicks) {
        this.rotationPeriodTicks = rotationPeriodTicks;
        return this;
    }

    public double getRotationAngle(double worldTime) {
        if (rotationPeriodTicks <= 0) return 0;
        double phase = worldTime % rotationPeriodTicks;
        return phase / rotationPeriodTicks * Math.PI * 2.0;
    }

    public static double computeSolarAltitude(Planetoid ground, Vec3d localUp, double worldTime) {
        Star sun = findPrimaryStar(ground);
        if (sun == null) return Double.NaN;
        Vec3d sunPos = Orbit.computeAbsolutePosition(sun, worldTime);
        Vec3d groundPos = Orbit.computeAbsolutePosition(ground, worldTime);
        Vec3d relativeEcl = sunPos.subtract(groundPos);
        double distAU = relativeEcl.length();
        if (distAU < 1e-15) return Double.NaN;
        return relativeEcl.dotProduct(localUp) / distAU;
    }

    public static boolean isSunAboveHorizon(Planetoid ground, Vec3d localUp, double worldTime) {
        double alt = computeSolarAltitude(ground, localUp, worldTime);
        return !Double.isNaN(alt) && alt > 0;
    }
}
