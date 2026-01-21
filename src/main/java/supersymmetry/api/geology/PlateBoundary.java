package supersymmetry.api.geology;

public class PlateBoundary {

    public Plate a;
    public Plate b;
    // This means that plateA is subduing under plateB
    public boolean subduing;
    // Unit vector pointing in direction of boundary, clockwise from a to b
    public Vec2d boundaryDirection;
    public double relativeMotion;
    public Type type;
    // Pointing from a to b

    public Vec2d d;

    public Vec2d cornerA;
    public Vec2d cornerB;
    public double length;

    public double noiseOffset;

    public PlateBoundary(Plate a, Plate b) {
        this.a = a;
        this.b = b;

        Vec2d dirA = a.motion;
        Vec2d dirB = b.motion;

        this.d = b.center.sub(a.center);

        this.boundaryDirection = this.d.perp().normalize();

        this.relativeMotion = dirB.sub(dirA).dot(this.d.normalize());

        this.type = Type.CONVERGING;

        this.setNeighbours(PlateMap.MIN_PLATE_DISTANCE / 2);

        this.length = cornerA.distance(cornerB);

        Vec2d m = this.a.center.add(this.b.center).scale(0.5);
        this.noiseOffset = PlateMap.instance.noise.noise2d(m.x, m.y);
    }

    public double distanceSquaredToBoundary(Vec2d P) {
        Vec2d m = this.a.center.add(this.b.center).scale( 1/2);

        Vec2d p = P.sub(m);

        Vec2d closest = this.boundaryDirection.project(p);

        double angle = closest.distance(cornerA) / this.length * Math.PI * 2;

        double noiseX = Math.cos(angle) + cornerA.x;
        double noiseZ = Math.sin(angle) + cornerB.y;

        Vec2d normal = this.d.normalize();

        // Apply perpendicular noise displacement
        double n = PlateMap.instance.noise.noise2d(noiseX, noiseZ) - this.noiseOffset;
        double offset = n * 100.0f;

        normal = normal.scale(offset);

        Vec2d perturbed = normal.add(closest);

        return perturbed.distanceSquared(P);
    }

    /**
     * @param x
     * @param y
     * @return Which plate the point is closest to after noise is applied. Null if on boundary.
     */
    public Plate closerPlate(double x, double y, double threshold) {
        Vec2d P = new Vec2d(x,y);
        Vec2d m = this.a.center.add(this.b.center).scale( 0.5);

        Vec2d p = P.sub(m);

        Vec2d closest = this.boundaryDirection.project(p).add(m);

        double angle = closest.distance(cornerA) / this.length * Math.PI * 2;

        Vec2d normal = this.d.normalize();

        double noiseX = Math.cos(angle) + m.x;
        double noiseZ = Math.sin(angle) + m.y;

        double primary = PlateMap.instance.noise.noise2d(noiseX, noiseZ) - this.noiseOffset;

        noiseX = - Math.cos(angle) + m.x;
        noiseZ = - Math.sin(angle) + m.y;

        primary += PlateMap.instance.noise.noise2d(noiseX, noiseZ) - this.noiseOffset;

        noiseX = Math.cos(angle) * this.length / 200. + m.x;
        noiseZ = Math.sin(angle) * this.length / 200. + m.y;

        double secondary = PlateMap.instance.noise.noise2d(noiseX, noiseZ) - this.noiseOffset;

        noiseX = - Math.cos(angle) * this.length / 200. + m.x;
        noiseZ = - Math.sin(angle) * this.length / 200. + m.y;

        secondary += PlateMap.instance.noise.noise2d(noiseX, noiseZ) - this.noiseOffset;

        noiseX = Math.cos(angle) * this.length / 50. + m.x;
        noiseZ = Math.sin(angle) * this.length / 50. + m.y;

        double tertiary = PlateMap.instance.noise.noise2d(noiseX, noiseZ) - this.noiseOffset;

        noiseX = - Math.cos(angle) * this.length / 50. + m.x;
        noiseZ = - Math.sin(angle) * this.length / 50. + m.y;

        secondary += PlateMap.instance.noise.noise2d(noiseX, noiseZ) - this.noiseOffset;

        // Apply perpendicular noise displacement
        double offset = 0; //primary * 160 + secondary * 16 + tertiary * 4;

        normal = normal.scale(offset);

        Vec2d perturbed = normal.add(closest);

        if(perturbed.distanceSquared(P) <= threshold * threshold) {
            return null;
        }

        perturbed = normal.add(P);

        return perturbed.distanceSquared(a.center) < perturbed.distanceSquared(b.center) ? a : b;
    }

    public void setNeighbours(int step) {
        Vec2d center = this.a.center.add(this.b.center).scale( 0.5d);
        Vec2d offset = this.boundaryDirection.scale(step);

        Vec2d pos = center.add(offset);

        Plate currentPlate = null;

        do {
            currentPlate = PlateMap.instance.getPlateAt(pos.x, pos.y);
            pos = pos.add(offset);
        } while (currentPlate == a || currentPlate == b);

        this.cornerA = Vec2d.circumcenter(a.center, b.center, currentPlate.center);

        offset = this.boundaryDirection.scale(-step);

        pos = center.add(offset);

        currentPlate = null;

        do {
            currentPlate = PlateMap.instance.getPlateAt(pos.x, pos.y);
            pos = pos.add(offset);
        } while (currentPlate == a || currentPlate == b);

        this.cornerB = Vec2d.circumcenter(a.center, b.center, currentPlate.center);
    }

    enum Type{
        DIVERGING,
        CONVERGING,
        TRANSFORM
    }
}
