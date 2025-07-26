package supersymmetry.api.geology;

import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Plate {

    public double cx;
    public double cz;
    public Type type;
    public Vec3d motion;
    public Map<Plate, PlateBoundary> boundaries = new HashMap<>();


    public Plate(double cx, double cz, boolean isContinental, Vec3d motion) {
        this.cx = cx;
        this.cz = cz;
        this.type = isContinental ? Type.CONTINENTAL : Type.OCEANIC;
        this.motion = motion;
    }

    public PlateBoundary getOrCreateBoundary(Plate other) {
        if (boundaries.containsKey(other)) return boundaries.get(other);
        PlateBoundary boundary = new PlateBoundary(this, other);
        boundaries.put(other, boundary);
        other.boundaries.put(this, boundary);
        return boundary;
    }

    public PlateBoundary getBoundaryWith(Plate other) {
        return boundaries.get(other);
    }

    public Collection<PlateBoundary> getAllBoundaries() {
        return boundaries.values();
    }

    public enum Type{
        CONTINENTAL,
        OCEANIC;
    }

    public double distanceSquared(double x, double z) {
        return Math.pow(this.cx - x, 2) + Math.pow(this.cz - z, 2);
    }

}
