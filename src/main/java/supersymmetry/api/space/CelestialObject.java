package supersymmetry.api.space;

import java.util.List;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CelestialObject {

    private String translationKey;

    private double mass;
    private double posT;
    private double posX;
    private double posY;
    private double posZ;

    private CelestialObject parentBody;
    private CelestialBodyType celestialBodyType;

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

    double getMass() {
        return mass;
    }

    double getPosT() {
        return posT;
    }

    double getPosX() {
        return posX;
    }

    double getPosY() {
        return posY;
    }

    double getPosZ() {
        return posZ;
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
}
