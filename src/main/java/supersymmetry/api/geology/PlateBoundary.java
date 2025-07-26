package supersymmetry.api.geology;

import net.minecraft.util.math.Vec3d;

public class PlateBoundary {

    public Plate a;
    public Plate b;
    // This means that plateA is subduing under plateB
    public boolean subduing;
    public Vec3d boundaryDirection;
    public double relativeMotion;
    public Type type;

    public Vec3d d;

    public double cornerAx;
    public double cornerAz;
    public double cornerBx;
    public double cornerBz;
    public double length;


    public PlateBoundary(Plate a, Plate b) {
        this.a = a;
        this.b = b;

        Vec3d dirA = a.motion;
        Vec3d dirB = b.motion;

        this.d = new Vec3d(b.cx - a.cx, 0, b.cz- a.cz);

        this.boundaryDirection = new Vec3d(-d.z, 0, d.x).normalize();

        this.relativeMotion = dirB.subtract(dirA).dotProduct(new Vec3d(a.cx - b.cx, 0, a.cz - b.cz));

        this.type = Type.CONVERGING;

        this.setNeighbours(PlateMap.MIN_PLATE_DISTANCE / 2);

        this.length = Math.sqrt((this.cornerAx - this.cornerBx) * (this.cornerAx - this.cornerBx) + (this.cornerAz - this.cornerBz) * (this.cornerAz - this.cornerBz));
    }

    public double distanceSquaredToBoundary(int x, int z) {
        double mx = (a.cx + b.cx) / 2.0;
        double mz = (a.cz + b.cz) / 2.0;

        double px = x - mx;
        double pz = z - mz;

        // Projection scalar
        double t = px * this.boundaryDirection.x + pz * this.boundaryDirection.z;

        Vec3d closest = new Vec3d(mx + t * this.boundaryDirection.x,0 , mz + t * this.boundaryDirection.z);

        double angle = Math.sqrt(closest.squareDistanceTo(cornerAx, 0,cornerAz)) / this.length * Math.PI * 2;

        float noiseX = (float) Math.cos(angle);
        float noiseZ = (float) Math.sin(angle);

        Vec3d normal = this.d.normalize();

        // Apply perpendicular noise displacement
        float n = PlateMap.instance.noise.noise2f(noiseX, noiseZ);
        float offset = n * 100.0f;

        normal = normal.scale(offset);

        Vec3d perturbed = normal.add(closest);

        return perturbed.squareDistanceTo(new Vec3d(x, 0 ,z));
    }

    public boolean isNearBoundary(int x, int z, double threshold) {
        return distanceSquaredToBoundary(x,z) <= threshold * threshold;
    }

    public void setNeighbours(int step) {
        double x = d.x / 2 + a.cx;
        double z = d.x / 2 + a.cz;

        Vec3d pos = new Vec3d(x,0, z);
        Vec3d offset = this.boundaryDirection.scale(step);

        pos = pos.add(offset);

        Plate currentPlate = null;

        do {
            currentPlate = PlateMap.instance.getPlateAt(pos.x, pos.z);
            pos = pos.add(offset);
        } while (currentPlate == a || currentPlate == b);

        double[] center = findCenter(a, b, currentPlate);

        this.cornerAx = center[0];
        this.cornerAz = center[1];

        pos = new Vec3d(x,0, z);
        offset = this.boundaryDirection.scale(-step);

        pos = pos.add(offset);

        currentPlate = null;


        do {
            currentPlate = PlateMap.instance.getPlateAt(pos.x, pos.z);
            pos = pos.add(offset);
        } while (currentPlate == a || currentPlate == b);

        center = findCenter(a, b, currentPlate);

        this.cornerBx = center[0];
        this.cornerBz = center[1];

    }

    public double[] findCenter(Plate M, Plate N, Plate L) {
        // Extract coordinates
        double x1 = M.cx, z1 = M.cz;
        double x2 = N.cx, z2 = N.cz;
        double x3 = L.cx, z3 = L.cz;

        double midMNx = (x1 + x2) / 2;
        double midMNz = (z1 + z2) / 2;
        double midNLx = (x2 + x3) / 2;
        double midNLz = (z2 + z3) / 2;

        Double slopeMN = (x2 - x1) == 0 ? null : (z2 - z1) / (x2 - x1);
        Double slopeNL = (x3 - x2) == 0 ? null : (z3 - z2) / (x3 - x2);

        // Perpendicular slopes
        Double perpSlopeMN = (slopeMN == null) ? 0.0 :
                (slopeMN == 0) ? null : -1 / slopeMN;

        Double perpSlopeNL = (slopeNL == null) ? 0.0 :
                (slopeNL == 0) ? null : -1 / slopeNL;

        double centerX, centerZ;

        if (perpSlopeMN == null) {
            centerX = midMNx;
            centerZ = perpSlopeNL * centerX + (midNLz - perpSlopeNL * midNLx);
        } else if (perpSlopeNL == null) {
            centerX = midNLx;
            centerZ = perpSlopeMN * centerX + (midMNz - perpSlopeMN * midMNx);
        } else if (perpSlopeMN.equals(perpSlopeNL)) {
            throw new IllegalArgumentException("Points are collinear.");
        } else {
            double c1 = midMNz - perpSlopeMN * midMNx;
            double c2 = midNLz - perpSlopeNL * midNLx;

            centerX = (c2 - c1) / (perpSlopeMN - perpSlopeNL);
            centerZ = perpSlopeMN * centerX + c1;
        }

        return new double[] { centerX, centerZ };
    }

    enum Type{
        DIVERGING,
        CONVERGING,
        TRANSFORM
    }
}
