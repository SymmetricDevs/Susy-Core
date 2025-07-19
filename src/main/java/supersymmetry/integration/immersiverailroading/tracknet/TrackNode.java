package supersymmetry.integration.immersiverailroading.tracknet;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TrackNode {

    private Vec3d position;
    public List<TrackSection> trackSections;
    private HashMap<TrackSection, List<TrackSection>> possibleExits;
    public NodeType type;

    public TrackNode(Vec3d position, NodeType type) {
        this.trackSections = new ArrayList<>();
        this.position = position;
        this.type = type;
    }

    public TrackNode(Vec3d position) {
        this(position, NodeType.END);
    }

    public void setPosition(Vec3d position) {
        this.position = position;
    }

    public boolean mergeable() {
        return this.type.equals(NodeType.END);
    }

    public Vec3d getPosition() {
        return this.position;
    }

    public enum NodeType {
        END,
        SIGNAL,
        SWITCH,
        MERGE,
        STATION
    }
}
