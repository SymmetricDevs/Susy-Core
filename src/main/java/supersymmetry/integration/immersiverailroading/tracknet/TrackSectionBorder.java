package supersymmetry.integration.immersiverailroading.tracknet;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class TrackSectionBorder {

    public Vec3d pos;
    private List<TrackSection> neighbours;

    public TrackSectionBorder(Vec3d pos) {
        this.pos = pos;
        this.neighbours = new ArrayList<>();
    }

    public void addNeighbour(TrackSection neighbour) {
        this.neighbours.add(neighbour);
    }

    public void move(Vec3d newPos) {
        this.pos = newPos;
    }



    public boolean closer(TrackSectionBorder other, Vec3d pos) {
        return this.pos.squareDistanceTo(pos) < other.pos.squareDistanceTo(pos);
    }


}
