package supersymmetry.api.space;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class CelestialObject {

    private double mass;
    private double posT;
    private double posX;
    private double posY;
    private double posZ;

    private CelestialObject parentBody;
    private CelestialBodyType celestialBodyType;

    private ArrayList<CelestialObject> childBodies;

    public CelestialObject(double mass, double posT, double posX, double posY, double posZ, @Nullable CelestialObject parentBody, CelestialBodyType celestialBodyType) {
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

    public ArrayList<CelestialObject> getChildBodies() {
        return childBodies;
    }

}
