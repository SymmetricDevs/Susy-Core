package supersymmetry.api.geology;

import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Plate {

    public Vec2d center;
    public Type type;
    public Vec2d motion;
    public Map<Plate, PlateBoundary> boundaries = new HashMap<>();


    public Plate(double cx, double cz, boolean isContinental, Vec3d motion) {
        this.center = new Vec2d(cx, cz);
        this.type = isContinental ? Type.CONTINENTAL : Type.OCEANIC;
        this.motion = new Vec2d(motion.x, motion.z);
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
}
