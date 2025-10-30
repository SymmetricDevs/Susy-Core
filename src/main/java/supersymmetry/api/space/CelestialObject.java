package supersymmetry.api.space;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

import javax.annotation.Nullable;

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
}
