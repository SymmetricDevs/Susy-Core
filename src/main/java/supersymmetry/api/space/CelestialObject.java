package supersymmetry.api.space;

import static supersymmetry.common.rocketry.SuccessCalculation.ESCAPE_VELOCITY_CONSTANT;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import net.minecraft.util.math.Vec3d;

import org.jspecify.annotations.Nullable;

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

    private Vec3d rotationAxis;
    private double rotationPeriodTicks;

    private List<CelestialObject> childBodies = new ObjectArrayList<>();
    private List<CelestialFeature> features = new ArrayList<>();

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

    @Nullable public CelestialObject getParentBody() {
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

    public void addFeature(CelestialFeature feature) {
        features.add(feature);
    }

    public List<CelestialFeature> getFeatures() {
        return features;
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

    public Star findPrimaryStar() {
        return Stream.iterate(this, Objects::nonNull, CelestialObject::getParentBody)
                .filter(Star.class::isInstance)
                .map(Star.class::cast)
                .findFirst()
                .orElse(null);
    }

    public Vec3d getRotationAxis() {
        return rotationAxis;
    }

    public CelestialObject setRotationAxis(Vec3d rotationAxis) {
        this.rotationAxis = rotationAxis;
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
}
