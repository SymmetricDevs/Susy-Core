package supersymmetry.integration.immersiverailroading.tracknet;

import net.minecraft.util.math.Vec3d;

import java.util.List;

public abstract class TrackSectionBorder {

    public Vec3d pos;

    public TrackSectionBorder(Vec3d pos) {
        this.pos = pos;
    }

    public void move(Vec3d newPos) {
        this.pos = newPos;
    }

    public boolean closer(TrackSectionBorder other, Vec3d pos) {
        return this.pos.squareDistanceTo(pos) < other.pos.squareDistanceTo(pos);
    }

    public abstract List<TrackSection> getNeighbours(TrackSection section);


}
